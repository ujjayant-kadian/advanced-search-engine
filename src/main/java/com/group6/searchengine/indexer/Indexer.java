package com.group6.searchengine.indexer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import com.group6.searchengine.data.DocumentData;
import com.group6.searchengine.parsers.FBISParser;

public class Indexer {

    private IndexWriter indexWriter;
    // Create specific analyzers for different fields
    private Analyzer titleAnalyzer;
    private Analyzer abstractAnalyzer;
    private Analyzer textAnalyzer;

    public Indexer(String indexPath) throws IOException {
        Directory indexDir = FSDirectory.open(Paths.get(indexPath));
        IndexWriterConfig config = new IndexWriterConfig(new StandardAnalyzer()); // Default analyzer
        this.indexWriter = new IndexWriter(indexDir, config);

        this.titleAnalyzer = new StandardAnalyzer();
        this.abstractAnalyzer = new EnglishAnalyzer();
        this.textAnalyzer = new EnglishAnalyzer();
    }

    public void indexFBIS(File fbisDirectory) throws IOException {
        FBISParser fbisParser = new FBISParser();
        
        fbisParser.parse(fbisDirectory, this::indexDocument);
    }

    public void indexDocument(DocumentData docData) throws IOException {
        Document luceneDoc = new Document();

        luceneDoc.add(new StringField("docNo", docData.getDocNo(), Field.Store.YES));
        luceneDoc.add(new StringField("author", docData.getAuthor(), Field.Store.NO));
        luceneDoc.add(new StringField("date", docData.getDate(), Field.Store.NO));
        luceneDoc.add(analyzeAndAddField("title", docData.getTitle(), titleAnalyzer));
        luceneDoc.add(analyzeAndAddField("abstract", docData.getAbs(), abstractAnalyzer));
        luceneDoc.add(analyzeAndAddField("text", docData.getText(), textAnalyzer));
        luceneDoc.add(new StringField("language", docData.getLanguage(), Field.Store.NO));
        luceneDoc.add(new StringField("region", docData.getRegion(), Field.Store.NO));
        luceneDoc.add(new StringField("location", docData.getLocation(), Field.Store.NO));

        indexWriter.addDocument(luceneDoc);
    }

    private TextField analyzeAndAddField(String fieldName, String fieldValue, Analyzer analyzer) throws IOException {
        List<String> tokens = new ArrayList<>();
        try (TokenStream tokenStream = analyzer.tokenStream(fieldName, fieldValue)) {
            CharTermAttribute attr = tokenStream.addAttribute(CharTermAttribute.class);
            tokenStream.reset();
            while (tokenStream.incrementToken()) {
                tokens.add(attr.toString());
            }
            tokenStream.end();
        }

        String analyzedValue = String.join(" ", tokens);
        return new TextField(fieldName, analyzedValue, Field.Store.NO);
    }

    public void close() throws IOException {
        indexWriter.close();
    }

    public static void main(String[] args) {
        try {
            Indexer indexer = new Indexer("index/");
            
            // Index FBIS dataset
            indexer.indexFBIS(new File("assignment-2/fbis"));
            
            // You can add more dataset parsers here

            indexer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
