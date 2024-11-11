package com.group6.searchengine.analyzers;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.KeywordTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

public class DateAnalyzer extends Analyzer {

    @Override
    protected TokenStreamComponents createComponents(String fieldName) {
        Tokenizer tokenizer = new KeywordTokenizer();
        TokenStream filter = new DateTokenFilter(tokenizer);
        return new TokenStreamComponents(tokenizer, filter);
    }

    private static class DateTokenFilter extends TokenFilter {

        private final CharTermAttribute termAttribute = addAttribute(CharTermAttribute.class);
        private boolean dateExtractedForCurrentToken = false;
    
        private static final Pattern VERBOSE_DATE_PATTERN = Pattern.compile(
                "\\b(January|February|March|April|May|June|July|August|September|October|November|December)\\s+(\\d{1,2}),\\s+(\\d{4})\\b"
        );
    
        protected DateTokenFilter(TokenStream input) {
            super(input);
        }
    
        @Override
        public boolean incrementToken() throws IOException {
            dateExtractedForCurrentToken = false;
    
            if (input.incrementToken()) {
                String text = termAttribute.toString();
                // System.out.println("Debug: Current token text: " + text);
    
                Matcher matcher = VERBOSE_DATE_PATTERN.matcher(text);
    
                if (matcher.find()) {
                    String extractedDate = matcher.group();
                    // System.out.println("Debug: Date found - " + extractedDate);
    
                    termAttribute.setEmpty().append(extractedDate);
                    dateExtractedForCurrentToken = true;
                    return true;
                } else {
                    // System.out.println("Debug: No matching date found in the text.");
                }
            } else {
                // System.out.println("Debug: No more tokens to increment.");
            }
    
            return false; // No valid date found
        }
    }
    
}
