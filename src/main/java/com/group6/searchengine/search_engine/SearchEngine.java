package com.group6.searchengine.search_engine;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.similarities.PerFieldSimilarityWrapper;

import com.group6.searchengine.query.QueryFormation;
import com.group6.searchengine.query.QueryFormation.QueryType;
import com.group6.searchengine.retrieval_model.HybridSimilarity;

public class SearchEngine {

    private static final String RESULTS_DIR = "results/";

    public void searchWithDifferentModels(IndexReader reader, List<Pair<String, Query>> queries, QueryType queryType) throws Exception {
    
        IndexSearcher searcher = new IndexSearcher(reader);

        performSearch(searcher, queries, queryType, "BM25");
        performSearch(searcher, queries, queryType, "VSM");
        performSearch(searcher, queries, queryType, "HYBRID");
    }

    private void performSearch(IndexSearcher searcher, List<Pair<String, Query>> queriesWithNumbers, 
                           QueryFormation.QueryType queryType, String scoringApproach) throws Exception {
                            
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

        switch (scoringApproach.toUpperCase()) {
            case "BM25" -> searcher.setSimilarity(new FieldSpecificBM25Similarity(fieldSpecificBM25));
            case "VSM" -> searcher.setSimilarity(new ClassicSimilarity());
            case "HYBRID" -> searcher.setSimilarity(new HybridSimilarity(
                    new FieldSpecificBM25Similarity(fieldSpecificBM25), 
                    new ClassicSimilarity(), 
                    0.8, 0.2
                ));
            default -> throw new IllegalArgumentException("Unsupported scoring approach: " + scoringApproach);
        }

        File resultsDir = new File(RESULTS_DIR);
            if (!resultsDir.exists()) {
                resultsDir.mkdirs();
            }

        try (BufferedWriter writer = new BufferedWriter(
                new FileWriter(RESULTS_DIR + queryType + "_" + scoringApproach + "_results.txt"))) {

            for (Pair<String, Query> queryPair : queriesWithNumbers) {
                String topicNumber = queryPair.getLeft();
                Query query = queryPair.getRight();
            
                ScoreDoc[] hits = searcher.search(query, 1000).scoreDocs;
            
                for (int rank = 0; rank < hits.length; rank++) {
                    ScoreDoc hit = hits[rank];
                    String docNo = searcher.doc(hit.doc).get("docNo");
                    writer.write(String.format("%s Q0 %s %d %.4f %s%n", topicNumber, docNo, rank + 1, hit.score, scoringApproach));
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