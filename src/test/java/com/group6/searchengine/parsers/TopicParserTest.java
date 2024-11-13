package com.group6.searchengine.parsers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.group6.searchengine.data.TopicData;

class TopicParserTest {

    @Test
    void testParseAllTopics() {
        File topicFile = new File("topics");
        TopicParser parser = new TopicParser();
        String outputFilePath = "test-outputs/topic-parser.txt";

        try {
            List<TopicData> topicDataList = parser.parseTopics(topicFile);
            assertNotNull(topicDataList);
            assertTrue(!topicDataList.isEmpty());
            writeTopicDataListToFile(topicDataList, outputFilePath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void writeTopicDataListToFile(List<TopicData> topicDataList, String outputFilePath) throws IOException {
        File outputFile = new File(outputFilePath);
        outputFile.getParentFile().mkdirs();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
            for (TopicData topicData : topicDataList) {
                writer.write("Number: " + topicData.getNumber());
                writer.newLine();
                writer.write("Title: " + topicData.getTitle());
                writer.newLine();
                writer.write("Description: " + topicData.getDescription());
                writer.newLine();
                writer.write("Narrative: " + topicData.getNarrative());
                writer.newLine();
                writer.write("--------------------------------------------------");
                writer.newLine();
            }
        }
    }
}