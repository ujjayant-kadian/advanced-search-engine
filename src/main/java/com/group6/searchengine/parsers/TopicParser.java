// TopicParser.java
package com.group6.searchengine.parsers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.group6.searchengine.data.TopicData;

public class TopicParser {

    public List<TopicData> parseTopics(File topicFile) throws IOException {
        List<TopicData> topics = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(topicFile))) {
            String line;
            TopicData currentTopic = null;
            StringBuilder contentBuilder = new StringBuilder();

            while ((line = reader.readLine()) != null) {
                line = line.trim();

                if (line.startsWith("<top>")) {
                    currentTopic = new TopicData();
                    contentBuilder.setLength(0); // Reset builder for new topic
                } else if (line.startsWith("<num>")) {
                    if (currentTopic != null) {
                        currentTopic.setNumber(line.replace("<num> Number: ", "").trim());
                    }
                } else if (line.startsWith("<title>")) {
                    if (currentTopic != null) {
                        currentTopic.setTitle(line.replace("<title>", "").trim());
                    }
                } else if (line.startsWith("<desc>")) {
                    if (currentTopic != null) {
                        contentBuilder.setLength(0);
                        while ((line = reader.readLine()) != null && !line.startsWith("<narr>") && !line.startsWith("</top>")) {
                            line = line.trim();
                            if (!line.isEmpty()) {
                                contentBuilder.append(line).append(" ");
                            }
                        }
                        currentTopic.setDescription(contentBuilder.toString().replace("Description: ", "").trim());
                    }
                }
                if (line != null && line.startsWith("<narr>")) { // Handle `<narr>` after `<desc>`
                    if (currentTopic != null) {
                        contentBuilder.setLength(0);
                        while ((line = reader.readLine()) != null && !line.startsWith("</top>")) {
                            line = line.trim();
                            if (!line.isEmpty()) {
                                contentBuilder.append(line).append(" ");
                            }
                        }
                        currentTopic.setNarrative(contentBuilder.toString().replace("Narrative: ", "").trim());
                    }
                }

                if (line != null && line.startsWith("</top>")) { // End of topic
                    if (currentTopic != null) {
                        topics.add(currentTopic);
                        currentTopic = null; // Reset for next topic
                    }
                }
            }
        }
        return topics;
    }
}