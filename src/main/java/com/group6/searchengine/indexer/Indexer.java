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

import com.group6.searchengine.analyzers.DateAnalyzer;
import com.group6.searchengine.analyzers.LowercaseAnalyzer;
import com.group6.searchengine.data.DocumentData;
import com.group6.searchengine.parsers.FBISParser;
import com.group6.searchengine.parsers.FR94Parser;
import com.group6.searchengine.parsers.LATimesParser;
import com.group6.searchengine.parsers.FTParser;

public class Indexer {

    private IndexWriter indexWriter;
    // Create specific analyzers for different fields
    private Analyzer titleAnalyzer;
    private Analyzer abstractAnalyzer;
    private Analyzer textAnalyzer;
    private Analyzer authorAnalyzer;
    private Analyzer dateAnalyzer;
    private Analyzer sectionAnalyzer;
    private Analyzer typeAnalyzer;
    private Analyzer graphicAnalyzer;
    private Analyzer languageAnalyzer;
    private Analyzer regionAnalyzer;
    private Analyzer usDeptAnalyzer;
    private Analyzer agencyAnalyzer;
    private Analyzer actionAnalyzer;
    private Analyzer supplementaryAnalyzer;


    public Indexer(String indexPath) throws IOException {
        System.out.println("Cleaning index folder!");
        clearIndexDirectory(indexPath);

        Directory indexDir = FSDirectory.open(Paths.get(indexPath));
        IndexWriterConfig config = new IndexWriterConfig(new StandardAnalyzer());
        this.indexWriter = new IndexWriter(indexDir, config);

        this.titleAnalyzer = new EnglishAnalyzer();
        this.abstractAnalyzer = new EnglishAnalyzer();
        this.textAnalyzer = new EnglishAnalyzer();
        this.authorAnalyzer = new LowercaseAnalyzer();
        this.dateAnalyzer = new DateAnalyzer();
        this.languageAnalyzer = new LowercaseAnalyzer();
        this.regionAnalyzer = new LowercaseAnalyzer();
        this.sectionAnalyzer = new StandardAnalyzer();
        this.typeAnalyzer = new StandardAnalyzer();
        this.graphicAnalyzer = new EnglishAnalyzer();
        this.usDeptAnalyzer = new LowercaseAnalyzer();
        this.agencyAnalyzer = new LowercaseAnalyzer();
        this.actionAnalyzer = new EnglishAnalyzer();
        this.supplementaryAnalyzer = new EnglishAnalyzer();
    }

    public void indexFBIS(File fbisDirectory) throws IOException {
        FBISParser fbisParser = new FBISParser();

        fbisParser.parse(fbisDirectory, this::indexDocument);
    }

    public void indexFR94(File fr94Directory) throws IOException {
        FR94Parser fr94Parser = new FR94Parser();

        fr94Parser.parse(fr94Directory, this::indexDocumentFR94);
    }

    public void indexLAT(File latDirectory) throws IOException {
        LATimesParser latParser = new LATimesParser();

        latParser.parse(latDirectory, this::indexDocumentLAT);
    }

    public void indexFT(File ftDirectory) throws IOException {
        FTParser ftParser = new FTParser();

        ftParser.parse(ftDirectory, this::indexDocument);
    }

    public void indexDocument(DocumentData docData) throws IOException {
        Document luceneDoc = new Document();

        luceneDoc.add(new StringField("docNo", docData.getDocNo(), Field.Store.YES));
        
        addAnalyzedFieldIfNotNull(luceneDoc, "author", docData.getAuthor(), authorAnalyzer);
        addAnalyzedFieldIfNotNull(luceneDoc, "date", docData.getDate(), dateAnalyzer);
        addAnalyzedFieldIfNotNull(luceneDoc, "title", docData.getTitle(), titleAnalyzer);
        addAnalyzedFieldIfNotNull(luceneDoc, "abstract", docData.getAbs(), abstractAnalyzer);
        addAnalyzedFieldIfNotNull(luceneDoc, "text", docData.getText(), textAnalyzer);
        addAnalyzedFieldIfNotNull(luceneDoc, "language", docData.getLanguage(), languageAnalyzer);
        addAnalyzedFieldIfNotNull(luceneDoc, "region", docData.getRegion(), regionAnalyzer);
        addAnalyzedFieldIfNotNull(luceneDoc, "section", docData.getSection(), sectionAnalyzer);
    
        indexWriter.addDocument(luceneDoc);
    }

    public void indexDocumentFR94(DocumentData docData) throws IOException {
        Document luceneDoc = new Document();

        luceneDoc.add(new StringField("docNo", docData.getDocNo(), Field.Store.YES));
    
        addAnalyzedFieldIfNotNull(luceneDoc, "date", docData.getDate(), dateAnalyzer);
        addAnalyzedFieldIfNotNull(luceneDoc, "title", docData.getTitle(), titleAnalyzer);
        addAnalyzedFieldIfNotNull(luceneDoc, "abstract", docData.getAbs(), abstractAnalyzer);
        addAnalyzedFieldIfNotNull(luceneDoc, "text", docData.getText(), textAnalyzer);
        addAnalyzedFieldIfNotNull(luceneDoc, "usDept", docData.getUsDept(), usDeptAnalyzer);
        addAnalyzedFieldIfNotNull(luceneDoc, "agency", docData.getAgency(), agencyAnalyzer);
        addAnalyzedFieldIfNotNull(luceneDoc, "action", docData.getAction(), actionAnalyzer);
        addAnalyzedFieldIfNotNull(luceneDoc, "supplementary", docData.getSupplementary(), supplementaryAnalyzer);
    
        indexWriter.addDocument(luceneDoc);
    }

    public void indexDocumentLAT(DocumentData docData) throws IOException {
        Document luceneDoc = new Document();

        luceneDoc.add(new StringField("docNo", docData.getDocNo(), Field.Store.YES));
    
        addAnalyzedFieldIfNotNull(luceneDoc, "date", docData.getDate(), dateAnalyzer);
        addAnalyzedFieldIfNotNull(luceneDoc, "title", docData.getTitle(), titleAnalyzer);
        addAnalyzedFieldIfNotNull(luceneDoc, "abstract", docData.getAbs(), abstractAnalyzer);
        addAnalyzedFieldIfNotNull(luceneDoc, "text", docData.getText(), textAnalyzer);
        addAnalyzedFieldIfNotNull(luceneDoc, "author", docData.getAuthor(), authorAnalyzer);
        addAnalyzedFieldIfNotNull(luceneDoc, "section", docData.getSection(), sectionAnalyzer);
        addAnalyzedFieldIfNotNull(luceneDoc, "type", docData.getType(), typeAnalyzer);
        addAnalyzedFieldIfNotNull(luceneDoc, "graphic", docData.getGraphic(), graphicAnalyzer);
    
        indexWriter.addDocument(luceneDoc);
    }

    public void indexDocumentFT(DocumentData docData) throws IOException {
        Document luceneDoc = new Document();

        luceneDoc.add(new StringField("docNo", docData.getDocNo(), Field.Store.YES));

        addFieldIfNotNull(luceneDoc, "profile", docData.getProfile());
        addFieldIfNotNull(luceneDoc, "date", docData.getDate());
        addAnalyzedFieldIfNotNull(luceneDoc, "headline", docData.getHeadline(), textAnalyzer);
        addAnalyzedFieldIfNotNull(luceneDoc, "byline", docData.getByline(), textAnalyzer);
        addAnalyzedFieldIfNotNull(luceneDoc, "text", docData.getText(), textAnalyzer);
        addAnalyzedFieldIfNotNull(luceneDoc, "pub", docData.getPub(), titleAnalyzer);
        addAnalyzedFieldIfNotNull(luceneDoc, "page", docData.getPage(), textAnalyzer);

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

            // Index LAT dataset
            indexer.indexLAT(new File("assignment-2/latimes"));

            // Index FT dataset
            indexer.indexFT(new File("assignment-2/ft"));

            indexer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
