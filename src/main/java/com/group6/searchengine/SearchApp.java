package com.group6.searchengine;

import java.io.File;
import java.util.List;

import com.group6.searchengine.data.TopicData;
import com.group6.searchengine.parsers.TopicParser;
import com.group6.searchengine.query.QueryFormation;
import com.group6.searchengine.search_engine.SearchEngine;

public class SearchApp {

    public static void main(String[] args) throws Exception {

        TopicParser topicParser = new TopicParser();
        List<TopicData> topics = topicParser.parseTopics(new File("topics"));

        SearchEngine searchEngine = new SearchEngine();

        QueryFormation queryFormation = new QueryFormation();

        String indexDir = "index";

        // Evaluate all query types
        for (QueryFormation.QueryType queryType : QueryFormation.QueryType.values()) {
            searchEngine.searchWithDifferentAnalyzers(indexDir, topics, queryType);
            searchEngine.searchWithDifferentAnalyzers(indexDir, topics, queryType);
        }
    }
}
