package com.group6.searchengine.query;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;

import com.group6.searchengine.data.TopicData;

public class QueryFormation {

    private Map<String, Double> idfScores = new HashMap<>();
    private int totalDocuments;

    public enum QueryType {
        TITLE_BASED,
        DESCRIPTION_BASED,
        COMBINED_TITLE_DESCRIPTION,
        NARRATIVE_FOCUSED,
        BEST_INFO_WORDS
    }

    public List<Pair<String, String>> generateQueries(List<TopicData> topics, QueryType queryType) {
        return topics.stream()
                .map(topic -> Pair.of(topic.getNumber(), generateQuery(topic, queryType)))
                .collect(Collectors.toList());
    }

    private String generateQuery(TopicData topic, QueryType queryType) {
        switch (queryType) {
            case TITLE_BASED -> {
                return topic.getTitle().trim();
            }
            case DESCRIPTION_BASED -> {
                return topic.getDescription().trim();
            }
            case COMBINED_TITLE_DESCRIPTION -> {
                return topic.getTitle().trim() + " " + topic.getDescription().trim();
            }
            case NARRATIVE_FOCUSED -> {
                return topic.getNarrative().trim();
            }
            case BEST_INFO_WORDS -> {
                return extractKeyPhrases(topic);
            }
            default -> throw new IllegalArgumentException("Unsupported query type: " + queryType);
        }
    }

    private String extractKeyPhrases(TopicData topic) {

        String combinedText = String.join(" ",
                topic.getTitle().toLowerCase(),
                topic.getDescription().toLowerCase(),
                topic.getNarrative()).toLowerCase();

        List<String> tokens = tokenize(combinedText);
        Map<String, Integer> termFrequencies = new HashMap<>();
        for (String token : tokens) {
            termFrequencies.put(token, termFrequencies.getOrDefault(token, 0) + 1);
        }

        Map<String, Double> tfidfScores = termFrequencies.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue() * idfScores.getOrDefault(entry.getKey(), 0.0)
                ));

        // Sort terms by TF-IDF score
        List<String> sortedTerms = tfidfScores.entrySet().stream()
                .sorted((e1, e2) -> Double.compare(e2.getValue(), e1.getValue()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        // Return top N terms as key phrases (N = 5)
        return String.join(" ", sortedTerms.subList(0, Math.min(5, sortedTerms.size())));
    }

    public void calculateIDFScores(List<TopicData> topics) {
        Map<String, Integer> documentFrequency = new HashMap<>();
        totalDocuments = topics.size();

        for (TopicData topic : topics) {
            String combinedText = String.join(" ",
                    topic.getTitle().toLowerCase(),
                    topic.getDescription().toLowerCase(),
                    topic.getNarrative()).toLowerCase();

            Set<String> uniqueTerms = new HashSet<>(tokenize(combinedText));
            for (String term : uniqueTerms) {
                documentFrequency.put(term, documentFrequency.getOrDefault(term, 0) + 1);
            }
        }

        idfScores = documentFrequency.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> Math.log((double) totalDocuments / entry.getValue())
                ));
    }

    private List<String> tokenize(String text) {
        return Arrays.stream(text.toLowerCase().split("\\W+"))
                .filter(token -> !token.isEmpty() && token.length() > 2)
                .collect(Collectors.toList());
    }
}
