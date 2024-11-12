package com.group6.searchengine.parsers;

import java.io.File;
import java.io.IOException;
import java.nio.charset.MalformedInputException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.group6.searchengine.data.DocumentData;

public class FTParser implements DatasetParser {

    private static final Map<String, String> ENTITY_MAP = new HashMap<>();

    static {
        ENTITY_MAP.put("amp", "&");
        ENTITY_MAP.put("gt", ">");
        ENTITY_MAP.put("lt", "<");
    }

    @Override
    public void parse(File ftDirectory, DocumentConsumer consumer) throws IOException {
        traverseDirectory(ftDirectory, consumer);
    }

    private void traverseDirectory(File directory, DocumentConsumer consumer) throws IOException {
        for (File file : directory.listFiles()) {
            if (file.isDirectory()) {
                System.out.println("Parsing ft directory: " + file.getName());
                traverseDirectory(file, consumer);
            } else if (file.isFile() && file.getName().startsWith("ft")) {
                parseFTFile(file, consumer);
            }
        }
    }

    public void parseSingleFile(File ftFile, DocumentConsumer consumer) throws IOException {
        if (ftFile.isFile()) {
            parseFTFile(ftFile, consumer);
        } else {
            throw new IllegalArgumentException("The provided file is not valid: " + ftFile.getPath());
        }
    }

    private void parseFTFile(File ftFile, DocumentConsumer consumer) throws IOException {
        String fileContent;
        try {
            fileContent = Files.readString(ftFile.toPath(), StandardCharsets.ISO_8859_1);
        } catch (MalformedInputException e) {
            System.err.println("Encoding error reading file: " + ftFile.getName());
            return;
        }

        fileContent = replaceSpecialCharacters(fileContent);

        Document doc = Jsoup.parse(fileContent, "", org.jsoup.parser.Parser.xmlParser());

        for (Element element : doc.select("DOC")) {
            DocumentData docData = new DocumentData();

            docData.setDocNo(getOrNull(element, "DOCNO"));
            docData.setProfile(getOrNull(element, "PROFILE"));
            docData.setDate(extractDateFromHeadline(getOrNull(element, "HEADLINE")));
            docData.setTitle(extractTitleFromHeadline(getOrNull(element, "HEADLINE")));
            docData.setAuthor(parseByline(getOrNull(element, "BYLINE")));
            docData.setText(getOrNull(element, "TEXT"));
            docData.setPub(getOrNull(element, "PUB"));

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
        return content.isEmpty() ? null : content.trim().replaceAll("\\s{2,}", " ");
    }

    private String extractDateFromHeadline(String headline) {
        if (headline == null) {
            return null;
        }
    
        Pattern datePattern = Pattern.compile("\\b(\\d{1,2})\\s+(JAN|FEB|MAR|APR|MAY|JUN|JUL|AUG|SEP|OCT|NOV|DEC)\\s+(\\d{2,4})\\b", Pattern.CASE_INSENSITIVE);
        Matcher matcher = datePattern.matcher(headline);
    
        if (matcher.find()) {
            String day = matcher.group(1);
            String month = matcher.group(2);
            String year = matcher.group(3);
    
            if (year.length() == 2) {
                int yearNum = Integer.parseInt(year);
                year = (yearNum > 50 ? "19" : "20") + year; // Assuming "50" as the cutoff year for 19xx or 20xx
            }
    
            try {
                String inputDate = day + " " + month.toUpperCase(Locale.ENGLISH) + " " + year;
                SimpleDateFormat inputFormat = new SimpleDateFormat("d MMM yyyy", Locale.ENGLISH);
                Date date = inputFormat.parse(inputDate);
    
                SimpleDateFormat outputFormat = new SimpleDateFormat("MMMM d, yyyy", Locale.ENGLISH);
                return outputFormat.format(date);
            } catch (ParseException e) {
                e.printStackTrace();
                return null;
            }
        }
    
        return null; // No valid date found
    }

    private String extractTitleFromHeadline(String headline) {
        if (headline == null) {
            return null;
        }
    
        int index = headline.indexOf("/ ");
        if (index != -1 && index + 2 < headline.length()) {
            return headline.substring(index + 2).trim();
        }
    
        return headline.trim();
    }

    private String parseByline(String byline) {
        if (byline == null) {
            return null;
        }
        return byline.replaceFirst("(?i)^(By|From)\\s+", "").trim();
    }

}
