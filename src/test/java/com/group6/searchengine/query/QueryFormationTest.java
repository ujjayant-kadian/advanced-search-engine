package com.group6.searchengine.query;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.group6.searchengine.data.TopicData;
import com.group6.searchengine.parsers.TopicParser;

class QueryFormationTest {

    @Test
    void testGenerateQueriesForBestInfoWords() {
        File topicFile = new File("topics");
        String outputFilePath = "test-outputs/best-info-words-queries.txt";
        TopicParser parser = new TopicParser();

        try {
            List<TopicData> topicDataList = parser.parseTopics(topicFile);
            assertNotNull(topicDataList);
            assertTrue(!topicDataList.isEmpty());

            QueryFormation queryFormation = new QueryFormation();

            queryFormation.calculateIDFScores(topicDataList);

            List<Pair<String, String>> queries = queryFormation.generateQueries(topicDataList, QueryFormation.QueryType.BEST_INFO_WORDS);

            writeQueriesToFile(queries, outputFilePath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void writeQueriesToFile(List<Pair<String, String>> queries, String outputFilePath) {
        File outputFile = new File(outputFilePath);
        outputFile.getParentFile().mkdirs();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
            for (Pair<String, String> query : queries) {
                writer.write("Topic Number: " + query.getLeft());
                writer.newLine();
                writer.write("Generated Query: " + query.getRight());
                writer.newLine();
                writer.write("--------------------------------------------------");
                writer.newLine();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
