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
                System.out.println("Processing line: " + line);  // Debug statement

                if (line.startsWith("<top>")) {
                    currentTopic = new TopicData();
                    contentBuilder.setLength(0); // Clear the content builder for new topic
                    System.out.println("Started new topic");  // Debug statement
                } else if (line.startsWith("<num>")) {
                    if (currentTopic != null) {
                        currentTopic.setNumber(line.replace("<num> Number: ", "").trim());
                        System.out.println("Set number: " + currentTopic.getNumber());  // Debug statement
                    }
                } else if (line.startsWith("<title>")) {
                    if (currentTopic != null) {
                        currentTopic.setTitle(line.replace("<title>", "").trim());
                        System.out.println("Set title: " + currentTopic.getTitle());  // Debug statement
                    }
                } else if (line.startsWith("<desc>")) {
                    if (currentTopic != null) {
                        contentBuilder.setLength(0); // Clear the builder
                        while ((line = reader.readLine()) != null && !line.startsWith("<narr>") && !line.startsWith("</top>")) {
                            if (!line.trim().isEmpty()) {
                                contentBuilder.append(line.trim()).append(" ");
                            }
                            System.out.println("Processing line in description: " + line.trim());  // Debug statement
                        }
                        currentTopic.setDescription(contentBuilder.toString().replace("Description: ", "").trim());
                        System.out.println("Set description: " + currentTopic.getDescription());  // Debug statement
                    }
                    
                    // Check if the next line is the <narr> tag
                    if (line != null && line.startsWith("<narr>")) {
                        if (currentTopic != null) {
                            contentBuilder.setLength(0); // Clear the builder
                            while ((line = reader.readLine()) != null && !line.startsWith("</top>")) {
                                if (!line.trim().isEmpty()) {
                                    contentBuilder.append(line.trim()).append(" ");
                                }
                                System.out.println("Processing line in narrative: " + line.trim());  // Debug statement
                            }
                            currentTopic.setNarrative(contentBuilder.toString().replace("Narrative: ", "").trim());
                            System.out.println("Set narrative: " + currentTopic.getNarrative());  // Debug statement
                        }
                    }
                } else if (line.startsWith("</top>")) {
                    if (currentTopic != null) {
                        topics.add(currentTopic);
                        System.out.println("Added topic: " + currentTopic);  // Debug statement
                        currentTopic = null; // Clear the current topic after adding to list
                    }
                }
            }
        }

        return topics;
    }
}
