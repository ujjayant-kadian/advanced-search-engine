package com.group6.searchengine.analyzers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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
       private final List<String> analyzedTokens = new ArrayList<>();
       private int tokenIndex = 0;
       private final CustomAnalyzer customAnalyzer = new CustomAnalyzer();
       
       private static final Pattern VERBOSE_DATE_PATTERN = Pattern.compile(
               "\\b(January|February|March|April|May|June|July|August|September|October|November|December)\\s+(\\d{1,2}),\\s+(\\d{4})\\b"
       );
   
       protected DateTokenFilter(TokenStream input) {
           super(input);
       }
   
       @Override
       public boolean incrementToken() throws IOException {
           if (tokenIndex < analyzedTokens.size()) {
               termAttribute.setEmpty().append(analyzedTokens.get(tokenIndex++));
               return true;
           }
   
           analyzedTokens.clear();
           tokenIndex = 0;
   
           if (input.incrementToken()) {
               String text = termAttribute.toString();
               Matcher matcher = VERBOSE_DATE_PATTERN.matcher(text);
   
               if (matcher.find()) {
                   String extractedDate = matcher.group();
                   analyzeWithCustomAnalyzer(extractedDate);
                   
                   if (!analyzedTokens.isEmpty()) {
                       termAttribute.setEmpty().append(analyzedTokens.get(tokenIndex++));
                       return true;
                   }
               }
           }
   
           return false;
       }

       private void analyzeWithCustomAnalyzer(String text) throws IOException {
           try (TokenStream tokenStream = customAnalyzer.tokenStream("field", text)) {
               CharTermAttribute charTermAttr = tokenStream.addAttribute(CharTermAttribute.class);
               tokenStream.reset();
   
               while (tokenStream.incrementToken()) {
                   analyzedTokens.add(charTermAttr.toString());
               }
               tokenStream.end();
           }
       }
   }
   
}
