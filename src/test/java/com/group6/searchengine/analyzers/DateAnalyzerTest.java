package com.group6.searchengine.analyzers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

class DateAnalyzerTest {

    @Test
    void testDateAnalyzer() throws IOException {
        Analyzer analyzer = new DateAnalyzer();

        List<String> testInputs = Arrays.asList(
            "DATES: This rule is effective December 2, 1994. Annual reports must be submitted on or before June 15, 1995.",
            "DATES: Effective December 1, 1995. Another rule starts on January 5, 1996.",

            "December 29, 1989,",
            "Effective date: January 1, 2020."
        );

        List<List<String>> expectedOutputs = Arrays.asList(
            Arrays.asList("decemb", "2", "1994"),
            Arrays.asList("decemb", "1", "1995"),
            Arrays.asList("decemb", "29", "1989"),
            Arrays.asList("januari", "1", "2020")
        );

        for (int i = 0; i < testInputs.size(); i++) {
            String input = testInputs.get(i);
            List<String> expected = expectedOutputs.get(i);

            List<String> actual = extractTokensUsingAnalyzer(analyzer, input);
            assertEquals(expected, actual, "Failed for input: " + input);
        }
    }

    private List<String> extractTokensUsingAnalyzer(Analyzer analyzer, String text) throws IOException {
        try (TokenStream tokenStream = analyzer.tokenStream("fieldName", text)) {
            CharTermAttribute termAttr = tokenStream.addAttribute(CharTermAttribute.class);
            tokenStream.reset();

            List<String> tokens = new ArrayList<>();
            while (tokenStream.incrementToken()) {
                tokens.add(termAttr.toString());
            }
            tokenStream.end();
            return tokens;
        }
    }
}
