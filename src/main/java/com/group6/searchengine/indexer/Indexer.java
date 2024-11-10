package com.group6.searchengine.indexer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
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
import com.group6.searchengine.parsers.FR94Parser;

public class Indexer {

    private IndexWriter indexWriter;
    // Create specific analyzers for different fields
    private Analyzer titleAnalyzer;
    private Analyzer abstractAnalyzer;
    private Analyzer textAnalyzer;

    public Indexer(String indexPath) throws IOException {
        System.out.println("Cleaning index folder!");
        clearIndexDirectory(indexPath);

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

    public void indexFR94(File fr94Directory) throws IOException {
        FR94Parser fr94Parser = new FR94Parser();
        fr94Parser.parse(fr94Directory, this::indexDocumentFR94);
    }

    public void indexDocument(DocumentData docData) throws IOException {
        Document luceneDoc = new Document();
    
        luceneDoc.add(new StringField("docNo", docData.getDocNo(), Field.Store.YES));
    
        addFieldIfNotNull(luceneDoc, "author", docData.getAuthor());
        addFieldIfNotNull(luceneDoc, "date", docData.getDate());
        addAnalyzedFieldIfNotNull(luceneDoc, "title", docData.getTitle(), titleAnalyzer);
        addAnalyzedFieldIfNotNull(luceneDoc, "abstract", docData.getAbs(), abstractAnalyzer);
        addAnalyzedFieldIfNotNull(luceneDoc, "text", docData.getText(), textAnalyzer);
        addFieldIfNotNull(luceneDoc, "language", docData.getLanguage());
        addFieldIfNotNull(luceneDoc, "region", docData.getRegion());
        addFieldIfNotNull(luceneDoc, "location", docData.getLocation());
    
        indexWriter.addDocument(luceneDoc);
    }

    public void indexDocumentFR94(DocumentData docData) throws IOException {
        Document luceneDoc = new Document();
    
        luceneDoc.add(new StringField("docNo", docData.getDocNo(), Field.Store.YES));
    
        // Add fields only if they are not null or empty
        addFieldIfNotNull(luceneDoc, "date", docData.getDate());
        addAnalyzedFieldIfNotNull(luceneDoc, "title", docData.getTitle(), titleAnalyzer);
        addAnalyzedFieldIfNotNull(luceneDoc, "abstract", docData.getAbs(), abstractAnalyzer);
        addAnalyzedFieldIfNotNull(luceneDoc, "text", docData.getText(), textAnalyzer);
        addFieldIfNotNull(luceneDoc, "usDept", docData.getUsDept());
        addFieldIfNotNull(luceneDoc, "agency", docData.getAgency());
        addFieldIfNotNull(luceneDoc, "action", docData.getAction());
        addAnalyzedFieldIfNotNull(luceneDoc, "supplementary", docData.getSupplementary(), textAnalyzer);
    
        indexWriter.addDocument(luceneDoc);
    }    

    private TextField analyzeAndAddField(String fieldName, String fieldValue, Analyzer analyzer) throws IOException {
        if (fieldValue == null || fieldValue.isEmpty()) {
            return null; // Skip indexing this field
        }
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

    private void addFieldIfNotNull(Document luceneDoc, String fieldName, String fieldValue) {
        if (fieldValue != null && !fieldValue.isEmpty()) {
            luceneDoc.add(new StringField(fieldName, fieldValue, Field.Store.NO));
        }
    }
    
    private void addAnalyzedFieldIfNotNull(Document luceneDoc, String fieldName, String fieldValue, Analyzer analyzer) throws IOException {
        TextField analyzedField = analyzeAndAddField(fieldName, fieldValue, analyzer);
        if (analyzedField != null) {
            luceneDoc.add(analyzedField);
        }
    }

    public void close() throws IOException {
        indexWriter.close();
    }

    private void clearIndexDirectory(String indexPath) throws IOException {
        File indexDir = new File(indexPath);
        if (indexDir.exists()) {
            deleteDirectory(indexDir);
        }
    }

    private void deleteDirectory(File dir) throws IOException {
        if (dir.isDirectory()) {
            for (File file : dir.listFiles()) {
                deleteDirectory(file);
            }
        }
        Files.delete(dir.toPath());
    }

    public static void main(String[] args) {
        try {
            Indexer indexer = new Indexer("index/");
            
            // Index FBIS dataset
            indexer.indexFBIS(new File("assignment-2/fbis"));

            // Index FR94 dataset
            indexer.indexFR94(new File("assignment-2/fr94"));
            
            // You can add more dataset parsers here

            indexer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
