package com.group6.searchengine.analyzers;

import java.io.IOException;
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
            // FR94 examples
            "DATES: This rule is effective December 2, 1994. Annual reports must be submitted on or before June 15 of the succeeding year, except that the report for calendar year 1993 must be submitted on or before March 15, 1995.",
            "DATES: Effective December 1, 1995. The incorporation by reference of certain publications listed in the regulations is approved by the Director of the Federal Register as of December 1, 1995.",

            // FBIS examples
            "December 29, 1989,",

            // LAT examples
            "December 29, 1989, Friday, Home Edition",
            "December 29, 1989, Friday, Valley Edition"
        );

        List<String> expectedOutputs = Arrays.asList(
            "December 2, 1994",
            "December 1, 1995",
            "December 29, 1989",
            "December 29, 1989",
            "December 29, 1989"
        );

        for (int i = 0; i < testInputs.size(); i++) {
            String input = testInputs.get(i);
            String expected = expectedOutputs.get(i);

            String actual = extractDateUsingAnalyzer(analyzer, input);
            assertEquals(expected, actual, "Failed for input: " + input);
        }
    }

    private String extractDateUsingAnalyzer(Analyzer analyzer, String text) throws IOException {
        try (TokenStream tokenStream = analyzer.tokenStream("fieldName", text)) {
            CharTermAttribute termAttr = tokenStream.addAttribute(CharTermAttribute.class);
            tokenStream.reset();

            if (tokenStream.incrementToken()) {
                return termAttr.toString();
            }
            tokenStream.end();
        }
        return null;
    }
}
