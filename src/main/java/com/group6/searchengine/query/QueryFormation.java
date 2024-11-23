package com.group6.searchengine.query;

import java.net.URL;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.QueryParserBase;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BoostQuery;
import org.apache.lucene.search.Query;

import com.group6.searchengine.analyzers.DateAnalyzer;
import com.group6.searchengine.analyzers.LowercaseAnalyzer;
import com.group6.searchengine.data.TopicData;

import edu.mit.jwi.Dictionary;
import edu.mit.jwi.item.ISynset;
import edu.mit.jwi.item.IWord;
import edu.mit.jwi.item.POS;

public class QueryFormation {

    private Map<String, Double> idfScores = new HashMap<>();
    private int totalDocuments;
    private final Dictionary dictionary;
    private  Map<String, Float> FIELD_BOOSTS = createFieldBoostMap();

    public enum QueryType {
        TITLE_BASED,
        DESCRIPTION_BASED,
        COMBINED_TITLE_DESCRIPTION,
        NARRATIVE_FOCUSED,
        BEST_INFO_WORDS,
        SYNONYM_EXPANDED,
        HYBRID,
        BOOLEAN_QUERY_BUILDER 
    }

    public QueryFormation(String wordNetPath) {
        try {
            @SuppressWarnings("deprecation")
            URL url = new URL("file", null, wordNetPath);
            dictionary = new Dictionary(url);
            dictionary.open();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to initialize WordNet dictionary", e);
        }
    }

    public List<Pair<String, Query>> generateQueries(List<TopicData> topics, QueryType queryType) {
        return topics.stream()
                .map(topic -> Pair.of(topic.getNumber(), generateQuery(topic, queryType)))
                .collect(Collectors.toList());
    }

    private Query generateQuery(TopicData topic, QueryType queryType) {
        try {
            switch (queryType) {
                case TITLE_BASED, DESCRIPTION_BASED, COMBINED_TITLE_DESCRIPTION, NARRATIVE_FOCUSED, BEST_INFO_WORDS, SYNONYM_EXPANDED, HYBRID -> {
                    return generateMultiFieldQuery(topic, queryType);
                }
                case BOOLEAN_QUERY_BUILDER -> {
                    return generateBooleanQuery(topic);
                }
                default -> throw new IllegalArgumentException("Unsupported QueryType: " + queryType);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error generating query for topic: " + topic.getNumber(), e);
        }
    }

    private Query generateMultiFieldQuery(TopicData topic, QueryType queryType) throws Exception {
        String[] fields = FIELD_BOOSTS.keySet().toArray(new String[0]);
        MultiFieldQueryParser queryParser = new MultiFieldQueryParser(fields, getQueryAnalyzer(), FIELD_BOOSTS);

        String queryText;
        switch (queryType) {
            case TITLE_BASED -> queryText = topic.getTitle().trim();
            case DESCRIPTION_BASED -> queryText = topic.getDescription().trim();
            case COMBINED_TITLE_DESCRIPTION -> queryText = truncateQuery(topic.getTitle().trim() + " " + topic.getDescription().trim(), 30);
            case NARRATIVE_FOCUSED -> queryText = truncateQuery(topic.getNarrative().trim(), 20);
            case BEST_INFO_WORDS -> queryText = extractKeyPhrases(topic);
            case SYNONYM_EXPANDED -> queryText = expandWithSynonyms(topic);
            case HYBRID -> queryText = hybridQueryFormation(topic);
            default -> throw new IllegalArgumentException("Unsupported QueryType: " + queryType);
        }

        try {
            return queryParser.parse(QueryParserBase.escape(queryText));
        } catch (Exception e) {
            System.err.println("Error parsing query: " + queryText + " for queryType: " + queryType);
            e.printStackTrace();
            throw e;
        }
    }

    private Query generateBooleanQuery(TopicData topic) throws Exception {
        BooleanQuery.Builder booleanQuery = new BooleanQuery.Builder();
    
        String[] fields = FIELD_BOOSTS.keySet().toArray(new String[0]);
        MultiFieldQueryParser queryParser = new MultiFieldQueryParser(fields, getQueryAnalyzer(), FIELD_BOOSTS);
    
        String title = topic.getTitle() != null ? topic.getTitle().trim() : "";
        String description = topic.getDescription() != null ? topic.getDescription().trim() : "";
        List<String> splitNarrative = splitNarrIntoRelNotRel(topic.getNarrative() != null ? topic.getNarrative().trim() : "");
    
        String relevantNarr = !splitNarrative.isEmpty() ? splitNarrative.get(0).trim() : "";
        String irrelevantNarr = splitNarrative.size() > 1 ? splitNarrative.get(1).trim() : "";
    
        if (!title.isEmpty()) {
            Query titleQuery = null;
            try {
                titleQuery = queryParser.parse(QueryParserBase.escape(title));
            } catch (Exception e) {
                System.err.println("Error parsing title: " + title);
            }
            if (titleQuery != null) {
                booleanQuery.add(new BoostQuery(titleQuery, 4.2f), BooleanClause.Occur.SHOULD);
            }
        }
    
        if (!description.isEmpty()) {
            Query descriptionQuery = null;
            try {
                descriptionQuery = queryParser.parse(QueryParserBase.escape(description));
            } catch (Exception e) {
                System.err.println("Error parsing description: " + description);
            }
            if (descriptionQuery != null) {
                booleanQuery.add(new BoostQuery(descriptionQuery, 1.9f), BooleanClause.Occur.SHOULD);
            }
        }
    
        if (!relevantNarr.isEmpty()) {
            Query relevantQuery = null;
            try {
                relevantQuery = queryParser.parse(QueryParserBase.escape(relevantNarr));
            } catch (Exception e) {
                System.err.println("Error parsing relevant narr: " + relevantNarr);
                e.printStackTrace(); 
            }
            if (relevantQuery != null) {
                booleanQuery.add(new BoostQuery(relevantQuery, 1.2f), BooleanClause.Occur.SHOULD);
            }
        }
    
        if (!irrelevantNarr.isEmpty()) {
            Query irrelevantQuery = null;
            try {
                irrelevantQuery = queryParser.parse(QueryParserBase.escape(irrelevantNarr));
            } catch (Exception e) {
                System.err.println("Error parsing irrelevant narr: " + irrelevantNarr);
            }
            if (irrelevantQuery != null) {
                // booleanQuery.add(irrelevantQuery, BooleanClause.Occur.FILTER);
                // booleanQuery.add(irrelevantQuery, BooleanClause.Occur.MUST_NOT);
                booleanQuery.add(new BoostQuery(irrelevantQuery, 0.01f), BooleanClause.Occur.SHOULD);
            }
        }
    
        if (booleanQuery.build().clauses().isEmpty()) {
            throw new IllegalArgumentException("No valid clauses in the boolean query.");
        }
    
        return booleanQuery.build();
    }    
    
    private String expandWithSynonyms(TopicData topic) {
        List<String> highImpactTerms = extractHighImpactTerms(topic, 0.5, Integer.MAX_VALUE); // No limit on term count
        return expandTermsWithSynonyms(highImpactTerms, 3, 30);
    }

    private String extractKeyPhrases(TopicData topic) {
        List<String> highImpactTerms = extractHighImpactTerms(topic, 0.5, 5); // Top 5 terms
        return expandTermsWithSynonyms(highImpactTerms, 3, 20);
    }

    private List<String> extractHighImpactTerms(TopicData topic, double idfThreshold, int maxTerms) {
        String combinedText = combineText(topic);
        Map<String, Double> tfidfScores = calculateTFIDFScores(tokenize(combinedText));

        return tfidfScores.entrySet().stream()
                .filter(entry -> idfScores.getOrDefault(entry.getKey(), 0.0) > idfThreshold)
                .sorted((e1, e2) -> Double.compare(e2.getValue(), e1.getValue()))
                .map(Map.Entry::getKey)
                .limit(maxTerms)
                .collect(Collectors.toList());
    }

    private String expandTermsWithSynonyms(List<String> terms, int maxSynonyms, int maxTokens) {
        Set<String> expandedQuery = new LinkedHashSet<>();

        for (String term : terms) {
            expandedQuery.add(term);
            expandedQuery.addAll(fetchRelevantSynonyms(term).stream()
                    .limit(maxSynonyms)
                    .collect(Collectors.toList()));
        }

        return truncateQuery(String.join(" ", expandedQuery), maxTokens);
    }

    private List<String> fetchRelevantSynonyms(String word) {
        List<String> synonyms = new ArrayList<>();
        try {
            POS[] posList = {POS.NOUN, POS.VERB};
            synchronized (dictionary) {
                for (POS pos : posList) {
                    var indexWord = dictionary.getIndexWord(word, pos);
                    if (indexWord != null) {
                        for (var wordID : indexWord.getWordIDs()) {
                            ISynset synset = dictionary.getWord(wordID).getSynset();
                            for (IWord synonym : synset.getWords()) {
                                if (!synonym.getLemma().equals(word)) {
                                    synonyms.add(synonym.getLemma());
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return synonyms.stream()
                .filter(syn -> idfScores.getOrDefault(syn, 0.5) > 0.5) // Keep relevant synonyms
                .distinct()
                .collect(Collectors.toList());
    }

    private String combineText(TopicData topic) {
        return String.join(" ",
                topic.getTitle().toLowerCase(),
                topic.getDescription().toLowerCase());
    }
    
    private Map<String, Double> calculateTFIDFScores(List<String> tokens) {
        Map<String, Integer> termFrequencies = new HashMap<>();
        for (String token : tokens) {
            termFrequencies.put(token, termFrequencies.getOrDefault(token, 0) + 1);
        }
    
        return termFrequencies.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue() * idfScores.getOrDefault(entry.getKey(), 0.0)
                ));
    }

    private String hybridQueryFormation(TopicData topic) {
        List<String> baseTokens = tokenizeAndClean(topic.getTitle() + " " + topic.getDescription());

        String synonymExpansion = expandTermsWithSynonyms(baseTokens, 2, 8);

        String nGrams = extractNGrams(baseTokens, 2, 3);

        String weightedQuery = weightAndCombineQueries(baseTokens, synonymExpansion, nGrams);

        return truncateQuery(weightedQuery, 30);
    }

    private String weightAndCombineQueries(List<String> baseTokens, String synonymExpansion, String nGrams) {
        String baseQuery = String.join(" ", baseTokens.stream()
                .flatMap(token -> Arrays.stream(new String[]{token, token}))
                .collect(Collectors.toList()));
    
        return baseQuery + " " + synonymExpansion + " " + nGrams;
    }
    
    private List<String> tokenizeAndClean(String text) {
        return tokenize(text).stream()
                .filter(token -> idfScores.getOrDefault(token, 0.0) > 1.0)
                .collect(Collectors.toList());
    }
    
    private String extractNGrams(List<String> tokens, int minGram, int maxGram) {
        Set<String> nGrams = new LinkedHashSet<>();
        for (int n = minGram; n <= maxGram; n++) {
            for (int i = 0; i <= tokens.size() - n; i++) {
                nGrams.add(String.join(" ", tokens.subList(i, i + n)));
            }
        }
        return String.join(" ", nGrams);
    }
    
    private String truncateQuery(String query, int maxTokens) {
        List<String> tokens = tokenize(query);
        return String.join(" ", tokens.subList(0, Math.min(tokens.size(), maxTokens)));
    }    
    
    public void calculateIDFScores(List<TopicData> topics) {
        Map<String, Integer> documentFrequency = new HashMap<>();
        totalDocuments = topics.size();

        for (TopicData topic : topics) {
            String combinedText = String.join(" ",
                    topic.getTitle().toLowerCase(),
                    topic.getDescription().toLowerCase(),
                    topic.getNarrative()).toLowerCase();

            Set<String> uniqueTerms = new HashSet<>(tokenize(combinedText));
            for (String term : uniqueTerms) {
                documentFrequency.put(term, documentFrequency.getOrDefault(term, 0) + 1);
            }
        }

        idfScores = documentFrequency.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> Math.log((double) totalDocuments / entry.getValue())
                ));
    }

    private List<String> tokenize(String text) {
        return Arrays.stream(text.toLowerCase().split("\\W+"))
                .filter(token -> !token.isEmpty() && token.length() > 2)
                .collect(Collectors.toList());
    }

    private static Map<String, Float> createFieldBoostMap() {
        Map<String, Float> fieldBoosts = new HashMap<>();
        fieldBoosts.put("title", 0.11f);
        fieldBoosts.put("text", 0.9f);
        fieldBoosts.put("abstract", 0.05f);
        fieldBoosts.put("date", 0.001f);
        fieldBoosts.put("author", 0.001f);
        fieldBoosts.put("language", 0.001f);
        fieldBoosts.put("region", 0.001f);
        fieldBoosts.put("usDept", 0.01f);
        fieldBoosts.put("agency", 0.01f);
        fieldBoosts.put("action", 0.01f);
        fieldBoosts.put("supplementary", 0.01f);
        fieldBoosts.put("type", 0.01f);
        fieldBoosts.put("graphic", 0.01f);
        fieldBoosts.put("profile", 0.001f);
        fieldBoosts.put("pub", 0.01f);
        fieldBoosts.put("section", 0.09f);
        return fieldBoosts;
    }

    private Analyzer getQueryAnalyzer() {
        Map<String, Analyzer> fieldAnalyzers = new HashMap<>();
        fieldAnalyzers.put("title", new EnglishAnalyzer());
        fieldAnalyzers.put("abstract", new EnglishAnalyzer());
        fieldAnalyzers.put("text", new EnglishAnalyzer());
        fieldAnalyzers.put("author", new LowercaseAnalyzer());
        fieldAnalyzers.put("date", new DateAnalyzer());
        fieldAnalyzers.put("language", new LowercaseAnalyzer());
        fieldAnalyzers.put("region", new LowercaseAnalyzer());
        fieldAnalyzers.put("section", new EnglishAnalyzer());
        fieldAnalyzers.put("type", new EnglishAnalyzer());
        fieldAnalyzers.put("graphic", new EnglishAnalyzer());
        fieldAnalyzers.put("usDept", new EnglishAnalyzer());
        fieldAnalyzers.put("agency", new EnglishAnalyzer());
        fieldAnalyzers.put("action", new EnglishAnalyzer());
        fieldAnalyzers.put("supplementary", new EnglishAnalyzer());
        fieldAnalyzers.put("profile", new LowercaseAnalyzer());
        fieldAnalyzers.put("pub", new EnglishAnalyzer());
    
        return new PerFieldAnalyzerWrapper(
            new EnglishAnalyzer(),
            fieldAnalyzers
        );
    }    

    private List<String> splitNarrIntoRelNotRel(String narrative) {
        StringBuilder relevantNarr = new StringBuilder();
        StringBuilder irrelevantNarr = new StringBuilder();
        List<String> splitNarrative = new ArrayList<>();

        BreakIterator bi = BreakIterator.getSentenceInstance();
        bi.setText(narrative);
        int index = 0;
        while (bi.next() != BreakIterator.DONE) {
            String sentence = narrative.substring(index, bi.current());
            if (!sentence.contains("not relevant") && !sentence.contains("irrelevant")) {
                relevantNarr.append(sentence.replaceAll(
                        "a relevant document identifies|a relevant document could|a relevant document may|a relevant document must|a relevant document will|to be relevant|relevant documents|must cite",
                        ""));
            } else {
                irrelevantNarr.append(sentence.replaceAll("not relevant|irrelevant", ""));
            }
            index = bi.current();
        }
        splitNarrative.add(relevantNarr.toString());
        splitNarrative.add(irrelevantNarr.toString());
        return splitNarrative;
    }

    public void setBoosts(Map<String, Float> dynamicBoosts) {
        if (dynamicBoosts != null && !dynamicBoosts.isEmpty()) {
            this.FIELD_BOOSTS = new HashMap<>(dynamicBoosts);
        } else {
            throw new IllegalArgumentException("Dynamic boosts cannot be null or empty.");
        }
    }

}
