package com.group6.searchengine.parsers;

import java.io.File;
import java.io.IOException;
import java.nio.charset.MalformedInputException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.group6.searchengine.data.DocumentData;

public class LATimesParser implements DatasetParser {

    private static final Map<String, String> ENTITY_MAP = new HashMap<>();

    static {
        ENTITY_MAP.put("amp", "&");
        ENTITY_MAP.put("gt", ">");
        ENTITY_MAP.put("lt", "<");
    }

    @Override
    public void parse(File laTimesDirectory, DocumentConsumer consumer) throws IOException {
        System.out.println("Parsing LA Times Dataset");
        for (File file : laTimesDirectory.listFiles()) {
            if (file.isFile()  && !file.getName().equals("readchg.txt") && !file.getName().equals("readmela.txt")) {
                parseLATimesFile(file, consumer);
            }
        }
    }

    public void parseSingleFile(File laTimesFile, DocumentConsumer consumer) throws IOException {
        if (laTimesFile.isFile()) {
            parseLATimesFile(laTimesFile, consumer);
        } else {
            throw new IllegalArgumentException("The provided file is not valid: " + laTimesFile.getPath());
        }
    }

    private void parseLATimesFile(File laTimesFile, DocumentConsumer consumer) throws IOException {
        String fileContent;
        try {
            fileContent = Files.readString(laTimesFile.toPath(), StandardCharsets.ISO_8859_1);
        } catch (MalformedInputException e) {
            System.err.println("Encoding error reading file: " + laTimesFile.getName());
            return;
        }

        fileContent = replaceSpecialCharacters(fileContent);

        Document doc = Jsoup.parse(fileContent, "", org.jsoup.parser.Parser.xmlParser());

        for (Element element : doc.select("DOC")) {
            DocumentData docData = new DocumentData();

            docData.setDocNo(getFieldContent(element, "DOCNO"));
            docData.setDate(getFieldContent(element, "DATE"));
            docData.setTitle(getFieldContent(element, "HEADLINE"));
            docData.setAuthor(parseByline(getFieldContent(element, "BYLINE")));
            docData.setText(getFieldContent(element, "TEXT"));
            docData.setAbs(getFieldContent(element, "SUBJECT"));
            docData.setSection(parseSection(getFieldContent(element, "SECTION")));
            docData.setType(getFieldContent(element, "TYPE"));
            docData.setGraphic(parseGraphic(getFieldContent(element, "GRAPHIC")));

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

    private String getFieldContent(Element element, String tagName) {
        Element fieldElement = element.selectFirst(tagName);
        if (fieldElement == null) {
            return null;
        }

        String content = fieldElement.select("P").isEmpty()
                ? fieldElement.text()
                : fieldElement.select("P").text();

        return cleanText(content);
    }

    private String parseByline(String byline) {
        if (byline == null) {
            return null;
        }
        return byline.replaceFirst("(?i)^(By|From)\\s+", "").trim();
    }

    private String parseSection(String section) {
        if (section == null) {
            return null;
        }
        String[] words = section.split("\\s+");
        return words.length > 0 ? words[0] : null;
    }

    private String parseGraphic(String graphic) {
        if (graphic == null) {
            return null;
        }
        return graphic.replaceFirst("^\\w+,\\s*", "").trim();
    }

    private String cleanText(String content) {
        if (content == null) {
            return null;
        }
        content = content.replaceAll("\\s{2,}", " ");
        return content.trim();
    }
}
