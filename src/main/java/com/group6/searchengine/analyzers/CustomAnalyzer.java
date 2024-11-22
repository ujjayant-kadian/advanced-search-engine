package com.group6.searchengine.analyzers;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.StopwordAnalyzerBase;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.FlattenGraphFilter;
import org.apache.lucene.analysis.en.EnglishPossessiveFilter;
import org.apache.lucene.analysis.miscellaneous.SetKeywordMarkerFilter;
import org.apache.lucene.analysis.miscellaneous.TrimFilter;
import org.apache.lucene.analysis.miscellaneous.WordDelimiterGraphFilter;
import org.apache.lucene.analysis.pattern.PatternReplaceFilter;
import org.apache.lucene.analysis.snowball.SnowballFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.synonym.SynonymGraphFilter;
import org.apache.lucene.analysis.synonym.SynonymMap;
import org.apache.lucene.util.CharsRef;
import org.tartarus.snowball.ext.EnglishStemmer;

public class CustomAnalyzer extends StopwordAnalyzerBase {

    private final CharArraySet stemExclusionSet;

    public CustomAnalyzer() {
        this(CharArraySet.EMPTY_SET);
    }

    public CustomAnalyzer(CharArraySet stemExclusionSet) {
        super(CharArraySet.EMPTY_SET);
        this.stemExclusionSet = CharArraySet.unmodifiableSet(stemExclusionSet);
    }

    @SuppressWarnings("resource")
    @Override
    protected TokenStreamComponents createComponents(String fieldName) {
        final Tokenizer tokenizer = new StandardTokenizer();

        // Start token stream pipeline
        TokenStream tokenStream = tokenizer;

        // Possessive Removal
        tokenStream = new EnglishPossessiveFilter(tokenStream);

        // Lowercase Conversion
        tokenStream = new LowerCaseFilter(tokenStream);

        // Trim Whitespace
        tokenStream = new TrimFilter(tokenStream);

        // Punctuation Removal
        tokenStream = new PatternReplaceFilter(
            tokenStream,
            Pattern.compile("(?<!\\b[A-Z])\\p{Punct}(?![A-Z]\\b)"),
            "",
            true
        );

        // Word Splitting (Compound Words)
        tokenStream = new WordDelimiterGraphFilter(tokenStream,
                WordDelimiterGraphFilter.SPLIT_ON_NUMERICS |
                WordDelimiterGraphFilter.GENERATE_WORD_PARTS |
                WordDelimiterGraphFilter.GENERATE_NUMBER_PARTS,
                null);

        // Graph Flattening
        tokenStream = new FlattenGraphFilter(tokenStream);

        // Synonym Expansion
        tokenStream = new SynonymGraphFilter(tokenStream, createSynonymMap(), true);

        // Exclude Tokens from Stemming
        if (!stemExclusionSet.isEmpty()) {
            tokenStream = new SetKeywordMarkerFilter(tokenStream, stemExclusionSet);
        }

        // Stop Words Removal
        tokenStream = new StopFilter(tokenStream, StopFilter.makeStopSet(createStopWordList(), true));

        // Stemming
        tokenStream = new SnowballFilter(tokenStream, new EnglishStemmer());

        return new TokenStreamComponents(tokenizer, tokenStream);
    }

    private SynonymMap createSynonymMap() {
        SynonymMap synMap = new SynonymMap(null, null, 0);
        try (BufferedReader countries = new BufferedReader(new InputStreamReader(
                getClass().getClassLoader().getResourceAsStream("custom-analyzer-data/countries.txt")))) {

            SynonymMap.Builder builder = new SynonymMap.Builder(true);
            String country = countries.readLine();

            while (country != null) {
                builder.add(new CharsRef("country"), new CharsRef(country), true);
                builder.add(new CharsRef("countries"), new CharsRef(country), true);
                country = countries.readLine();
            }

            synMap = builder.build();
        } catch (Exception e) {
            System.out.println("ERROR: " + e.getLocalizedMessage() + " occurred when trying to create synonym map");
        }
        return synMap;
    }

    private List<String> createStopWordList() {
        List<String> stopWordList = new ArrayList<>();
        try (BufferedReader stopwords = new BufferedReader(new InputStreamReader(
                getClass().getClassLoader().getResourceAsStream("custom-analyzer-data/stopwords.txt")))) {

            String word = stopwords.readLine();
            while (word != null) {
                stopWordList.add(word);
                word = stopwords.readLine();
            }
        } catch (Exception e) {
            System.out.println("ERROR: " + e.getLocalizedMessage() + " occurred when trying to create stopword list");
        }
        return stopWordList;
    }
}
