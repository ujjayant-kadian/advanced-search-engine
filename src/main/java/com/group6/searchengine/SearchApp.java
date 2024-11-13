package com.group6.searchengine;

import java.io.File;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import com.group6.searchengine.data.TopicData;
import com.group6.searchengine.indexer.Indexer;
import com.group6.searchengine.parsers.TopicParser;
import com.group6.searchengine.query.QueryFormation;
import com.group6.searchengine.query.QueryFormation.QueryType;
import com.group6.searchengine.search_engine.SearchEngine;

public class SearchApp {

    private static final String RESULTS_DIR = "results/";

    public static void main(String[] args) throws Exception {
        clearResultsDirectory();
        String indexPath = "index/";

        System.out.println("Starting Indexing Process...");
        Indexer indexer = new Indexer(indexPath);
        try {
            indexer.indexFBIS(new File("assignment-2/fbis"));
            indexer.indexFR94(new File("assignment-2/fr94"));
            indexer.indexLAT(new File("assignment-2/latimes"));
            indexer.indexFT(new File("assignment-2/ft"));
            System.out.println("Indexing Complete!");
        } finally {
            indexer.close();
        }

        System.out.println("Parsing Topics...");
        TopicParser topicParser = new TopicParser();
        List<TopicData> topics = topicParser.parseTopics(new File("topics"));
        if (topics.isEmpty()) {
            System.err.println("No topics found. Exiting...");
            return;
        }
        System.out.println("Parsed " + topics.size() + " topics.");

        QueryFormation queryFormation = new QueryFormation();
        queryFormation.calculateIDFScores(topics);

        SearchEngine searchEngine = new SearchEngine();
        for (QueryType queryType : QueryType.values()) {
            System.out.println("Processing Query Type: " + queryType);
            List<Pair<String, String>> queries = queryFormation.generateQueries(topics, queryType);

            searchEngine.searchWithDifferentAnalyzers(indexPath, queries, queryType);
        }

        System.out.println("Search Process Complete! Results stored in 'results/' directory.");
    }

    private static void clearResultsDirectory() {
        File resultsDir = new File(RESULTS_DIR);
        if (resultsDir.exists()) {
            System.out.println("Cleaning results folder!");
            deleteDirectoryContents(resultsDir);
        }
    }

    private static void deleteDirectoryContents(File directory) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    file.delete();
                } else if (file.isDirectory()) {
                    deleteDirectoryContents(file);
                    file.delete();
                }
            }
        }
    }
}
