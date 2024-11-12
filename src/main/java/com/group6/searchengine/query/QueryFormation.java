package com.group6.searchengine.query;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;

import com.group6.searchengine.data.TopicData;

public class QueryFormation {

    public enum QueryType {
        TITLE_BASED,
        DESCRIPTION_BASED,
        COMBINED_TITLE_DESCRIPTION,
        NARRATIVE_FOCUSED,
        BEST_INFO_WORDS,
        QUERY_EXPANSION_TITLE_DESC,
        QUERY_EXPANSION_NARRATIVE
    }

    public List<Pair<String, String>> generateQueries(List<TopicData> topics, QueryType queryType) {
        return topics.stream()
                .map(topic -> Pair.of(topic.getNumber(), generateQuery(topic, queryType)))
                .collect(Collectors.toList());
    }

    private String generateQuery(TopicData topic, QueryType queryType) {
        switch (queryType) {
            case TITLE_BASED:
                return topic.getTitle().trim();
            case DESCRIPTION_BASED:
                return topic.getDescription().trim();
            case COMBINED_TITLE_DESCRIPTION:
                return topic.getTitle().trim() + " " + topic.getDescription().trim();
            case NARRATIVE_FOCUSED:
                return topic.getNarrative().trim();
            case BEST_INFO_WORDS:
                return extractKeyPhrases(topic);
            case QUERY_EXPANSION_TITLE_DESC:
                return expandQuery(topic.getTitle().trim() + " " + topic.getDescription().trim());
            case QUERY_EXPANSION_NARRATIVE:
                return expandQuery(topic.getNarrative().trim());
            default:
                throw new IllegalArgumentException("Unsupported query type: " + queryType);
        }
    }

    private String extractKeyPhrases(TopicData topic) {

    }

    private String expandQuery(String query) {

    }
}
