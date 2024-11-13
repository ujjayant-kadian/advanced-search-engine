package com.group6.searchengine.search_engine;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.QueryParserBase;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.similarities.PerFieldSimilarityWrapper;
import org.apache.lucene.store.FSDirectory;

import com.group6.searchengine.query.QueryFormation;
import com.group6.searchengine.query.QueryFormation.QueryType;

public class SearchEngine {

    private static final String RESULTS_DIR = "results/";

    public void searchWithDifferentAnalyzers(String indexDir, List<Pair<String, String>> queries, QueryType queryType) throws Exception {
        String[] fields = {"title", "text", "abs"}; // Fields to search
    
        try (FSDirectory directory = FSDirectory.open(Paths.get(indexDir));
             DirectoryReader reader = DirectoryReader.open(directory)) {
    
            IndexSearcher searcher = new IndexSearcher(reader);
    
            // Perform searches with both BM25 and VSM scoring approaches
            performSearch(searcher, queries, fields, queryType, "BM25");
            performSearch(searcher, queries, fields, queryType, "VSM");
        }
    }

    private void performSearch(IndexSearcher searcher, List<Pair<String, String>> queriesWithNumbers, String[] fields, 
                           QueryFormation.QueryType queryType, String scoringApproach) throws Exception {
                            
        Map<String, BM25Similarity> fieldSpecificBM25 = Map.of(
            "title", new BM25Similarity(1.5f, 0.3f),
            "text", new BM25Similarity(1.2f, 0.75f),
            "abs", new BM25Similarity(1.7f, 0.5f)
        );

        if ("BM25".equalsIgnoreCase(scoringApproach)) {
            searcher.setSimilarity(new FieldSpecificBM25Similarity(fieldSpecificBM25));
        } else if ("VSM".equalsIgnoreCase(scoringApproach)) {
            searcher.setSimilarity(new ClassicSimilarity()); // Use VSM
        } else {
            throw new IllegalArgumentException("Unsupported scoring approach: " + scoringApproach);
        }

        File resultsDir = new File(RESULTS_DIR);
            if (!resultsDir.exists()) {
                resultsDir.mkdirs();
            }

        Map<String, Float> fieldBoosts = Map.of(
                "title", 2.0f,
                "text", 1.0f,
                "abs", 1.5f
            );

        try (BufferedWriter writer = new BufferedWriter(
                new FileWriter(RESULTS_DIR + queryType + "_" + scoringApproach + "_results.txt"))) {

            for (Pair<String, String> queryPair : queriesWithNumbers) {
                String topicNumber = queryPair.getLeft();
                String queryText = queryPair.getRight();

                String escapedQueryText = QueryParserBase.escape(queryText);

                MultiFieldQueryParser queryParser = new MultiFieldQueryParser(fields, getAnalyzerForQueryType(queryType), fieldBoosts);

                Query query = queryParser.parse(escapedQueryText);

                ScoreDoc[] hits = searcher.search(query, 1000).scoreDocs;

                for (int rank = 0; rank < hits.length; rank++) {
                    ScoreDoc hit = hits[rank];
                    String docNo = searcher.doc(hit.doc).get("docNo");
                    writer.write(String.format("%s Q0 %s %d %.4f %s%n", topicNumber, docNo, rank + 1, hit.score, scoringApproach));
                }
            }
        }
    }

    private Analyzer getAnalyzerForQueryType(QueryFormation.QueryType queryType) {
        return switch (queryType) {
            case TITLE_BASED -> new EnglishAnalyzer();
            case DESCRIPTION_BASED, COMBINED_TITLE_DESCRIPTION -> new EnglishAnalyzer();
            case NARRATIVE_FOCUSED -> new StandardAnalyzer();
            case BEST_INFO_WORDS -> new EnglishAnalyzer();
            default -> throw new IllegalArgumentException("Unsupported QueryType: " + queryType);
        };
    }
}

class FieldSpecificBM25Similarity extends PerFieldSimilarityWrapper {
    private final BM25Similarity defaultSimilarity;
    private final Map<String, BM25Similarity> fieldSimilarities;

    public FieldSpecificBM25Similarity(Map<String, BM25Similarity> fieldSimilarities) {
        this.defaultSimilarity = new BM25Similarity();
        this.fieldSimilarities = fieldSimilarities;
    }

    @Override
    public BM25Similarity get(String fieldName) {
        return fieldSimilarities.getOrDefault(fieldName, defaultSimilarity);
    }
}
