package com.group6.searchengine.query;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.lucene.search.Query;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.group6.searchengine.data.TopicData;
import com.group6.searchengine.parsers.TopicParser;

class QueryFormationTest {

    @Test
    void testGenerateQueriesForAllQueryTypes() {
        File topicFile = new File("topics");
        String outputBasePath = "test-outputs/queries/";
        TopicParser parser = new TopicParser();

        try {
            List<TopicData> topicDataList = parser.parseTopics(topicFile);
            assertNotNull(topicDataList, "Parsed topics list should not be null");
            assertTrue(!topicDataList.isEmpty(), "Parsed topics list should not be empty");

            QueryFormation queryFormation = new QueryFormation("dict/");

            queryFormation.calculateIDFScores(topicDataList);

            for (QueryFormation.QueryType queryType : QueryFormation.QueryType.values()) {
                System.out.println("Testing QueryType: " + queryType);

                List<Pair<String, Query>> queries = queryFormation.generateQueries(topicDataList, queryType);

                assertNotNull(queries, "Generated queries list should not be null for queryType: " + queryType);
                assertTrue(!queries.isEmpty(), "Generated queries list should not be empty for queryType: " + queryType);

                writeQueriesToFile(queries, outputBasePath + queryType.name().toLowerCase() + "-queries.txt");

            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new AssertionError("Test failed due to an exception: " + e.getMessage());
        }
    }

    private void writeQueriesToFile(List<Pair<String, Query>> queries, String outputFilePath) {
        File outputFile = new File(outputFilePath);
        outputFile.getParentFile().mkdirs();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
            for (Pair<String, Query> query : queries) {
                writer.write("Topic Number: " + query.getLeft());
                writer.newLine();
                writer.write("Generated Query: " + query.getRight().toString());
                writer.newLine();
                writer.write("--------------------------------------------------");
                writer.newLine();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}
