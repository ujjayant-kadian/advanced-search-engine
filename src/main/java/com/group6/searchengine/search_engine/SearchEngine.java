package com.group6.searchengine.search_engine;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.nio.file.Paths;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.store.FSDirectory;

import com.group6.searchengine.data.TopicData;
import com.group6.searchengine.query.QueryFormation;

public class SearchEngine {

    private static final String RESULTS_DIR = "results/";

    public void searchWithDifferentAnalyzers(String indexDir, List<TopicData> topics, QueryFormation.QueryType queryType) throws Exception {
        // Define the fields you want to search
        String[] fields = {"title", "text", "abs"};

        try (FSDirectory directory = FSDirectory.open(Paths.get(indexDir));
             DirectoryReader reader = DirectoryReader.open(directory)) {

            IndexSearcher searcher = new IndexSearcher(reader);

            QueryFormation queryFormation = new QueryFormation();

            List<Pair<String, String>> queries = queryFormation.generateQueries(topics, queryType);

            performSearch(searcher, queries, fields, queryType, "BM25", new BM25Similarity());
            performSearch(searcher, queries, fields, queryType, "VSM", new ClassicSimilarity());
        }
    }

    private void performSearch(IndexSearcher searcher, List<Pair<String, String>> queriesWithNumbers, String[] fields, 
                           QueryFormation.QueryType queryType, String scoringApproach, 
                           org.apache.lucene.search.similarities.Similarity similarity) throws Exception {

        searcher.setSimilarity(similarity);

        try (BufferedWriter writer = new BufferedWriter(
                new FileWriter(RESULTS_DIR + queryType + "_" + scoringApproach + "_results.txt"))) {

            for (Pair<String, String> queryPair : queriesWithNumbers) {
                String topicNumber = queryPair.getLeft();
                String queryText = queryPair.getRight();

                MultiFieldQueryParser queryParser = new MultiFieldQueryParser(fields, getAnalyzerForQueryType(queryType));

                Query query = queryParser.parse(queryText);

                ScoreDoc[] hits = searcher.search(query, 1000).scoreDocs;

                for (int rank = 0; rank < hits.length; rank++) {
                    ScoreDoc hit = hits[rank];
                    String docNo = searcher.doc(hit.doc).get("docNo");
                    writer.write(String.format("%s 0 %s %d %.4f %s%n", topicNumber, docNo, rank + 1, hit.score, scoringApproach));
                }
            }
        }
    }

    private Analyzer getAnalyzerForQueryType(QueryFormation.QueryType queryType) {
        return switch (queryType) {
            case TITLE_BASED -> new EnglishAnalyzer();
            case DESCRIPTION_BASED, COMBINED_TITLE_DESCRIPTION -> new StandardAnalyzer();
            case NARRATIVE_FOCUSED -> new WhitespaceAnalyzer();
            case BEST_INFO_WORDS -> new EnglishAnalyzer();
            case QUERY_EXPANSION_TITLE_DESC, QUERY_EXPANSION_NARRATIVE -> new EnglishAnalyzer();
            default -> throw new IllegalArgumentException("Unsupported QueryType: " + queryType);
        };
    }
}
