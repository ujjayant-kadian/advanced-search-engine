package com.group6.searchengine;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.PerFieldSimilarityWrapper;
import org.apache.lucene.store.FSDirectory;

import com.group6.searchengine.data.TopicData;
import com.group6.searchengine.parsers.TopicParser;
import com.group6.searchengine.query.QueryFormation;

public class GridSearchApp {

    private static final String RESULTS_DIR = "grid-search-results/";
    private static final String INDEX_DIR = "index/";

    private static final Map<String, BM25Similarity> FIELD_SPECIFIC_BM25 = createFieldSpecificBM25();

    public static void main(String[] args) throws Exception {
        System.out.println("Starting Grid Search for Optimization...");
        
        Map<String, List<Float>> boostRanges = createBoostRanges();
        Map<String, Float> constantBoosts = constantBoosts();
    
        TopicParser topicParser = new TopicParser();
        List<TopicData> topics = topicParser.parseTopics(new File("topics"));
        if (topics.isEmpty()) {
            System.err.println("No topics found. Exiting...");
            return;
        }
    
        // performGridSearch(boostRanges, constantBoosts, topics);
        evaluateBM25Combinations(topics);
    
        System.out.println("Grid Search Complete! Results stored in 'grid-search-results/' directory.");
    }

    private static Map<String, BM25Similarity> createFieldSpecificBM25() {
        Map<String, BM25Similarity> fieldSpecificBM25 = new HashMap<>();
        fieldSpecificBM25.put("title", new BM25Similarity(1.5f, 0.3f));
        fieldSpecificBM25.put("text", new BM25Similarity(1.2f, 0.75f));
        fieldSpecificBM25.put("abstract", new BM25Similarity(1.3f, 0.5f));
        fieldSpecificBM25.put("date", new BM25Similarity(1.0f, 0.6f));
        fieldSpecificBM25.put("author", new BM25Similarity(1.0f, 0.5f));
        fieldSpecificBM25.put("language", new BM25Similarity(1.0f, 0.3f));
        fieldSpecificBM25.put("region", new BM25Similarity(1.2f, 0.4f));
        fieldSpecificBM25.put("usDept", new BM25Similarity(1.3f, 0.5f));
        fieldSpecificBM25.put("agency", new BM25Similarity(1.3f, 0.5f));
        fieldSpecificBM25.put("action", new BM25Similarity(1.2f, 0.4f));
        fieldSpecificBM25.put("supplementary", new BM25Similarity(1.1f, 0.5f));
        fieldSpecificBM25.put("type", new BM25Similarity(1.1f, 0.4f));
        fieldSpecificBM25.put("graphic", new BM25Similarity(1.0f, 0.3f));
        fieldSpecificBM25.put("profile", new BM25Similarity(1.1f, 0.3f));
        fieldSpecificBM25.put("pub", new BM25Similarity(1.2f, 0.4f));
        fieldSpecificBM25.put("section", new BM25Similarity(1.4f, 0.5f));
        return fieldSpecificBM25;
    }

    private static Map<String, List<Float>> createBoostRanges() {
        Map<String, List<Float>> boostRanges = new HashMap<>();
        // boostRanges.put("section", generateRange(0.01f, 0.1f, 0.01f));
        return boostRanges;
    }

    private static Map<String, Float> constantBoosts() {
        Map<String, Float> constantBoosts = new HashMap<>();
        constantBoosts.put("abstract", 0.05f);//best
        constantBoosts.put("title", 0.11f);//best
        constantBoosts.put("text", 0.9f);//best
        constantBoosts.put("date", 0.001f);//best
        constantBoosts.put("author", 0.001f);//best
        constantBoosts.put("language", 0.001f);//best
        constantBoosts.put("region", 0.001f);//best
        constantBoosts.put("usDept", 0.01f);//best (i guess)
        constantBoosts.put("agency", 0.01f);//best (i guess)
        constantBoosts.put("action", 0.01f);//best (i guess)
        constantBoosts.put("supplementary", 0.01f);//best (i guess)
        constantBoosts.put("type", 0.01f);//best (i guess)
        constantBoosts.put("graphic", 0.01f);//best
        constantBoosts.put("profile", 0.001f);//best
        constantBoosts.put("pub", 0.01f);//best
        constantBoosts.put("section", 0.09f);//best
        return constantBoosts;
    }

    private static List<Float> generateRange(float start, float end, float step) {
        List<Float> range = new ArrayList<>();
        for (float value = start; value <= end; value += step) {
            range.add(value);
        }
        return range;
    }

    private static void performGridSearch(Map<String, List<Float>> boostRanges,
                                      Map<String, Float> constantBoosts,
                                      List<TopicData> topics) throws Exception {
        File resultsDir = new File(RESULTS_DIR);
        if (!resultsDir.exists()) {
            resultsDir.mkdirs();
        }

        Map<String, Float> currentBoosts = new HashMap<>();
        exploreBoostCombinations(boostRanges, constantBoosts, currentBoosts, 0, new ArrayList<>(boostRanges.keySet()), topics);
    }

    private static void exploreBoostCombinations(Map<String, List<Float>> boostRanges,
                                              Map<String, Float> constantBoosts,
                                              Map<String, Float> currentBoosts,
                                              int fieldIndex,
                                              List<String> fields,
                                              List<TopicData> topics) throws Exception {
        if (fieldIndex == fields.size()) {
            currentBoosts.putAll(constantBoosts);

            evaluateBoostCombination(currentBoosts, topics);
            return;
        }

        String field = fields.get(fieldIndex);
        for (Float boostValue : boostRanges.get(field)) {
            currentBoosts.put(field, boostValue);
            exploreBoostCombinations(boostRanges, constantBoosts, currentBoosts, fieldIndex + 1, fields, topics);
            currentBoosts.remove(field);
        }
    }

    private static void evaluateBoostCombination(Map<String, Float> boosts, List<TopicData> topics) throws Exception {
        System.out.println("Evaluating Boost Combination: " + boosts);
    
        try (FSDirectory directory = FSDirectory.open(Paths.get(INDEX_DIR));
             DirectoryReader reader = DirectoryReader.open(directory)) {
    
            IndexSearcher searcher = new IndexSearcher(reader);
            searcher.setSimilarity(new FieldSpecificBM25Similarity(FIELD_SPECIFIC_BM25));
    
            QueryFormation queryFormation = new QueryFormation("dict/");
            queryFormation.setBoosts(boosts);
    
            List<Pair<String, Query>> queries = queryFormation.generateQueries(topics, QueryFormation.QueryType.BOOLEAN_QUERY_BUILDER);
    
            File resultFile = new File(RESULTS_DIR + "boost_" + boosts.toString().replaceAll("[{}=, ]", "_") + ".txt");
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(resultFile))) {
                for (Pair<String, Query> queryPair : queries) {
                    String topicNumber = queryPair.getLeft();
                    Query query = queryPair.getRight();
    
                    ScoreDoc[] hits = searcher.search(query, 1000).scoreDocs;
    
                    for (int rank = 0; rank < hits.length; rank++) {
                        ScoreDoc hit = hits[rank];
                        String docNo = searcher.doc(hit.doc).get("docNo");
                        writer.write(String.format("%s Q0 %s %d %.4f %s%n", topicNumber, docNo, rank + 1, hit.score, "BM25"));
                    }
                }
            }
        }
    }

    private static void evaluateBM25Combinations(List<TopicData> topics) throws Exception {
        File resultsDir = new File(RESULTS_DIR);
        if (!resultsDir.exists()) {
            resultsDir.mkdirs();
        }

        // Define BM25 parameter ranges
        List<Float> k1Range = generateRange(1.0f, 1.5f, 0.1f); // Example range for k1
        List<Float> bRange = generateRange(0.1f, 0.9f, 0.2f);  // Example range for b

        // Define fields to optimize
        List<String> fieldsToOptimize = List.of("section");

        // Generate BM25 combinations for each field
        for (String field : fieldsToOptimize) {
            for (float k1 : k1Range) {
                for (float b : bRange) {
                    // Create a BM25 configuration for the current combination
                    Map<String, BM25Similarity> bm25Configurations = createBM25Configuration(field, k1, b);

                    // Evaluate this BM25 configuration
                    evaluateBM25Configuration(bm25Configurations, topics, field, k1, b);
                }
            }
        }
    }

    private static Map<String, BM25Similarity> createBM25Configuration(String field, float k1, float b) {
        Map<String, BM25Similarity> fieldSpecificBM25 = new HashMap<>();
        fieldSpecificBM25.put("title", new BM25Similarity(1.2f, 0.2f));
        fieldSpecificBM25.put("text", new BM25Similarity(1.1f, 0.7f));
        fieldSpecificBM25.put("abstract", new BM25Similarity(1.5f, 0.5f));
        fieldSpecificBM25.put("date", new BM25Similarity(1.2f, 0.2f));
        fieldSpecificBM25.put("author", new BM25Similarity(1.2f, 0.2f));
        fieldSpecificBM25.put("language", new BM25Similarity(1.2f, 0.2f));
        fieldSpecificBM25.put("region", new BM25Similarity(1.2f, 0.2f));
        fieldSpecificBM25.put("usDept", new BM25Similarity(1.2f, 0.2f));
        fieldSpecificBM25.put("agency", new BM25Similarity(1.2f, 0.2f));
        fieldSpecificBM25.put("action", new BM25Similarity(1.2f, 0.2f));
        fieldSpecificBM25.put("supplementary", new BM25Similarity(1.1f, 0.5f));
        fieldSpecificBM25.put("type", new BM25Similarity(1.2f, 0.1f));
        fieldSpecificBM25.put("graphic", new BM25Similarity(1.1f, 0.3f));
        fieldSpecificBM25.put("profile", new BM25Similarity(1.0f, 0.1f));
        fieldSpecificBM25.put("pub", new BM25Similarity(1.2f, 0.2f));
        fieldSpecificBM25.put("section", new BM25Similarity(1.0f, 0.5f));

        // Override the parameters for the specified field
        fieldSpecificBM25.put(field, new BM25Similarity(k1, b));

        return fieldSpecificBM25;
    }

    private static void evaluateBM25Configuration(Map<String, BM25Similarity> bm25Configurations,
                                                List<TopicData> topics,
                                                String field,
                                                float k1,
                                                float b) throws Exception {
        System.out.println("Evaluating BM25 for field: " + field + " (k1=" + k1 + ", b=" + b + ")");

        try (FSDirectory directory = FSDirectory.open(Paths.get(INDEX_DIR));
                DirectoryReader reader = DirectoryReader.open(directory)) {

            IndexSearcher searcher = new IndexSearcher(reader);
            searcher.setSimilarity(new FieldSpecificBM25Similarity(bm25Configurations));

            QueryFormation queryFormation = new QueryFormation("dict/");

            List<Pair<String, Query>> queries = queryFormation.generateQueries(topics, QueryFormation.QueryType.BOOLEAN_QUERY_BUILDER);

            File resultFile = new File(RESULTS_DIR + "bm25_" + field + "_k1_" + k1 + "_b_" + b + ".txt");
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(resultFile))) {
                for (Pair<String, Query> queryPair : queries) {
                    String topicNumber = queryPair.getLeft();
                    Query query = queryPair.getRight();

                    ScoreDoc[] hits = searcher.search(query, 1000).scoreDocs;

                    for (int rank = 0; rank < hits.length; rank++) {
                        ScoreDoc hit = hits[rank];
                        String docNo = searcher.doc(hit.doc).get("docNo");
                        writer.write(String.format("%s Q0 %s %d %.4f BM25%n", topicNumber, docNo, rank + 1, hit.score));
                    }
                }
            }
        }
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
