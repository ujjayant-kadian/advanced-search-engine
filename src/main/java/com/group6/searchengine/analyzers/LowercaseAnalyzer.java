package com.group6.searchengine.analyzers;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.KeywordTokenizer;

public class LowercaseAnalyzer extends Analyzer {
    @Override
    protected TokenStreamComponents createComponents(String fieldName) {
        Tokenizer tokenizer = new KeywordTokenizer();
        TokenStream filter = new LowerCaseFilter(tokenizer);
        return new TokenStreamComponents(tokenizer, filter);
    }
}

