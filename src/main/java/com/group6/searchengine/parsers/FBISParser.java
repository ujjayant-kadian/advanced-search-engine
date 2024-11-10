package com.group6.searchengine.parsers;

import com.group6.searchengine.data.DocumentData;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.charset.MalformedInputException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FBISParser implements DatasetParser {

    private static final Map<String, String> ENTITY_MAP = new HashMap<>();

    static {
        ENTITY_MAP.put("amp", "&");
        ENTITY_MAP.put("gt", ">");
        ENTITY_MAP.put("lt", "<");
        ENTITY_MAP.put("AElig", "Æ");
        ENTITY_MAP.put("ap", "'");
        ENTITY_MAP.put("deg", "°");
        ENTITY_MAP.put("egrave", "è");
        ENTITY_MAP.put("eacute", "é");
        ENTITY_MAP.put("oacute", "ó");
        ENTITY_MAP.put("ubreve", "ŭ");
        ENTITY_MAP.put("Ubreve", "Ŭ");
        ENTITY_MAP.put("egs", "≥");
        ENTITY_MAP.put("els", "≤");
        ENTITY_MAP.put("percnt", "%");
        ENTITY_MAP.put("pound", "£");
        ENTITY_MAP.put("yen", "¥");
        ENTITY_MAP.put("agr", "α");
        ENTITY_MAP.put("bgr", "β");
        ENTITY_MAP.put("dgr", "δ");
        ENTITY_MAP.put("egr", "ε");
        ENTITY_MAP.put("ggr", "γ");
        ENTITY_MAP.put("Ggr", "Γ");
        ENTITY_MAP.put("kgr", "κ");
        ENTITY_MAP.put("lgr", "λ");
        ENTITY_MAP.put("mgr", "μ");
        ENTITY_MAP.put("pgr", "π");
        ENTITY_MAP.put("rgr", "ρ");
        ENTITY_MAP.put("sgr", "σ");
        ENTITY_MAP.put("tgr", "τ");
        ENTITY_MAP.put("xgr", "χ");
        ENTITY_MAP.put("zgr", "ζ");
        ENTITY_MAP.put("eegr", "η");
        ENTITY_MAP.put("khgr", "χ");
        ENTITY_MAP.put("phgr", "φ");
        ENTITY_MAP.put("thgr", "θ");
        ENTITY_MAP.put("ohm", "Ω");
        ENTITY_MAP.put("Bgr", "Β");
        ENTITY_MAP.put("Ngr", "Ν");
        ENTITY_MAP.put("EEgr", "Η");
        ENTITY_MAP.put("OHgr", "Ω");
        ENTITY_MAP.put("PSgr", "Ψ");
        ENTITY_MAP.put("Omacr", "Ō");
    }

    @Override
    public void parse(File fbisDirectory, DocumentConsumer consumer) throws IOException {
        System.out.println("Parsing FBIS Dataset");
        for (File file : fbisDirectory.listFiles()) {
            if (file.isFile() && !file.getName().equals("readchg.txt") && !file.getName().equals("readmefb.txt")) {
                parseFBISFile(file, consumer);
            }
        }
    }

    public void parseSingleFile(File fbisFile, DocumentConsumer consumer) throws IOException {
        if (fbisFile.isFile()) {
            parseFBISFile(fbisFile, consumer);
        } else {
            throw new IllegalArgumentException("The provided file is not valid: " + fbisFile.getPath());
        }
    }

    private void parseFBISFile(File fbisFile, DocumentConsumer consumer) throws IOException {
        String fileContent;
        try {
            fileContent = Files.readString(fbisFile.toPath(), StandardCharsets.ISO_8859_1);
        } catch (MalformedInputException e) {
            System.err.println("Encoding error reading file: " + fbisFile.getName());
            return;
        }
        // Replacing special characters with actual symbols for indexing
        fileContent = replaceSpecialCharacters(fileContent);

        Document doc = Jsoup.parse(fileContent, "", org.jsoup.parser.Parser.xmlParser());
        
        for (Element element : doc.select("DOC")) {
            DocumentData docData = new DocumentData();
    
            // Extracting required fields with null checking for missing tags
            docData.setDocNo(getOrNull(element, "DOCNO"));
            docData.setAuthor(parseAuthorsOrNull(element.select("AU")));
            docData.setDate(getOrNull(element, "DATE1"));
            docData.setTitle(getOrNull(element, "TI"));
            docData.setAbs(getOrNull(element, "ABS"));
    
            String rawText = element.select("TEXT").text();
            docData.setText(cleanTextOrNull(rawText));
    
            docData.setRegion(parseFieldOrNull(element, "F[P=100]"));
            docData.setLocation(parseFieldOrNull(element, "F[P=101]"));
            docData.setLanguage(parseFieldOrNull(element, "F[P=105]"));
    
            consumer.consume(docData);
        }
        
    }

    private String replaceSpecialCharacters(String content) {

        if (content == null || content.isEmpty()) {
            return "";
        }

        for (Map.Entry<String, String> entry : ENTITY_MAP.entrySet()) {
            content = content.replace("&" + entry.getKey() + ";", entry.getValue());
        }
        return content;
    }

    private String getOrNull(Element element, String tagName) {
        String content = element.select(tagName).text();
        return content.isEmpty() ? null : content;
    }

    private String parseFieldOrNull(Element element, String cssQuery) {
        Element fieldElement = element.selectFirst(cssQuery);
        return (fieldElement == null || fieldElement.text().isEmpty()) ? null : fieldElement.text();
    }

    private String parseAuthorsOrNull(Elements authorElements) {
        if (authorElements == null || authorElements.isEmpty()) {
            return null;
        }
        List<String> authors = new ArrayList<>();
        for (Element authorElement : authorElements) {
            authors.add(authorElement.text());
        }
        return authors.isEmpty() ? null : String.join(", ", authors);
    }

    private String cleanTextOrNull(String content) {
        if (content == null) return null;
        content = cleanText(content);
        return content.isEmpty() ? null : content;
    }

    private String cleanText(String content) {
        if (content == null) {
            return "";
        }
        content = content.replaceAll("\\[\\w+\\]", "");
        content = content.replaceAll("<F P=\\d+>.*?</F>", "");
        content = content.replaceAll("(?m)^Language:\\s*.*$", "");
        content = content.replaceAll("(?m)^Article Type:\\s*.*$", "");
        return content.trim().replaceAll("\\s{2,}", " ");
    }

}
