package com.group6.searchengine.indexer;

import com.group6.searchengine.data.FBISData;
import com.group6.searchengine.data.FR94Data;
import com.group6.searchengine.parsers.DatasetParser;
import com.group6.searchengine.parsers.FBISParser;
import com.group6.searchengine.parsers.FR94Parser;
import com.group6.searchengine.parsers.DocumentConsumer;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.ArrayList;

public class Indexer {

    private IndexWriter indexWriter;
    // Create specific analyzers for different fields
    private Analyzer titleAnalyzer;
    private Analyzer textAnalyzer;

    // FIBS specific analyzer
    private Analyzer abstractAnalyzer;

    // FR94 specific analyzer
    private Analyzer summaryAnalyzer;

    public Indexer(String indexPath) throws IOException {
        Directory indexDir = FSDirectory.open(Paths.get(indexPath));
        IndexWriterConfig config = new IndexWriterConfig(new StandardAnalyzer()); // Default analyzer
        this.indexWriter = new IndexWriter(indexDir, config);
        this.titleAnalyzer = new StandardAnalyzer();
        this.textAnalyzer = new EnglishAnalyzer();
        // FIBS specific analyzer
        this.abstractAnalyzer = new EnglishAnalyzer();

        // FR94 analyzer
        this.summaryAnalyzer = new EnglishAnalyzer();

    }

    public void indexFBIS(File fbisDirectory) throws IOException {
        FBISParser fbisParser = new FBISParser();
        
        fbisParser.parse(fbisDirectory, this::indexDocument);
    }

    public void indexFR94(File fr94Dir) throws IOException {
        FR94Parser fr94Parser = new FR94Parser();
        fr94Parser.parse(fr94Dir, this::indexFR94Doc);
    }

    public void indexDocument(FBISData docData) throws IOException {
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

    public void indexFR94Doc(FR94Data fr94Data) throws IOException {
        Document luceneDoc = new Document();

        luceneDoc.add(new StringField("docNo", fr94Data.getDocNo(), Field.Store.YES));
        luceneDoc.add(analyzeAndAddField("title", fr94Data.getDocTitle(), titleAnalyzer));
        luceneDoc.add(analyzeAndAddField("summary", fr94Data.getSummary(), summaryAnalyzer));
        luceneDoc.add(new StringField("agency", fr94Data.getAgency(), Field.Store.NO));
        luceneDoc.add(new StringField("usDept", fr94Data.getUsDept(), Field.Store.NO));
        luceneDoc.add(analyzeAndAddField("text", fr94Data.getFullText(), textAnalyzer));

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
            System.out.println("Indexing FBIS dataset...");
            indexer.indexFBIS(new File("../assignment-2/fbis"));
            System.out.println("FBIS dataset indexed successfully.");
            
            // Index FR94 dataset
            System.out.println("Indexing FR94 dataset...");
            indexer.indexFR94(new File("../assignment-2/fr94"));
            System.out.println("FR94 dataset indexed successfully.");

            indexer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
