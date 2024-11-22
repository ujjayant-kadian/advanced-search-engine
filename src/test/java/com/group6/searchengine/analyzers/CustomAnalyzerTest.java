package com.group6.searchengine.analyzers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import static org.junit.jupiter.api.Assertions.assertFalse;
import org.junit.jupiter.api.Test;

public class CustomAnalyzerTest {

    @Test
    public void testCustomAnalyzer() {
        Analyzer analyzer = new CustomAnalyzer();

        List<String> testQueries = List.of(
                "Documents describing genetic or environmental factors.",
                "Substance abuse and addictions are relevant.",
                "Genome project behavior disorders like mood disorders.",
                "irrelevant narrative completely removed",
                "",
                "i.e., mood disorders",
                "26 December 2024",
                "Documents describing genetic or environmental factors relating to understanding and preventing substance abuse and addictions are relevant.  Documents pertaining to attention deficit disorders tied in with genetics are also relevant, as are genetic disorders affecting hearing or muscles.  The genome project is relevant when tied in with behavior disorders (i.e., mood disorders, Alzheimer's disease)"
        );

        for (String query : testQueries) {
            System.out.println("Testing query: \"" + query + "\"");
            try {
                List<String> tokens = analyzeText(analyzer, query);
                System.out.println("Tokens: " + tokens);

                if (!query.isEmpty()) {
                    assertFalse(tokens.isEmpty(), "Tokens should not be empty for non-empty input.");
                }
            } catch (Exception e) {
                System.err.println("ERROR: Exception occurred during analysis for query: \"" + query + "\"");
                e.printStackTrace();
            }
        }

        analyzer.close();
    }

    private static List<String> analyzeText(Analyzer analyzer, String text) throws IOException {
        List<String> tokens = new ArrayList<>();

        try (TokenStream tokenStream = analyzer.tokenStream("field", text)) {
            tokenStream.reset();
            CharTermAttribute charTermAttribute = tokenStream.addAttribute(CharTermAttribute.class);

            while (tokenStream.incrementToken()) {
                tokens.add(charTermAttribute.toString());
            }

            tokenStream.end();
        }

        return tokens;
    }
}
