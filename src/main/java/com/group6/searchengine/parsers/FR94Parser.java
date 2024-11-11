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

public class FR94Parser implements DatasetParser {
    private static final Map<String, String> ENTITY_MAP = new HashMap<>();

    static {
        // Standard Entities
        ENTITY_MAP.put("amp", "&");
        ENTITY_MAP.put("gt", ">");
        ENTITY_MAP.put("lt", "<");

        // Additional Symbol Entities
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

        // Accented Characters
        ENTITY_MAP.put("acirc", "â");
        ENTITY_MAP.put("ncirc", "ñ");
        ENTITY_MAP.put("atilde", "ã");
        ENTITY_MAP.put("ntilde", "ñ");
        ENTITY_MAP.put("otilde", "õ");
        ENTITY_MAP.put("utilde", "ũ");
        ENTITY_MAP.put("aacute", "á");
        ENTITY_MAP.put("cacute", "ć");
        ENTITY_MAP.put("eacute", "é");
        ENTITY_MAP.put("Eacute", "É");
        ENTITY_MAP.put("Gacute", "Ģ");
        ENTITY_MAP.put("iacute", "í");
        ENTITY_MAP.put("lacute", "ĺ");
        ENTITY_MAP.put("nacute", "ń");
        ENTITY_MAP.put("oacute", "ó");
        ENTITY_MAP.put("pacute", "ṕ");
        ENTITY_MAP.put("racute", "ŕ");
        ENTITY_MAP.put("sacute", "ś");
        ENTITY_MAP.put("uacute", "ú");
        ENTITY_MAP.put("ocirc", "ô");
        ENTITY_MAP.put("auml", "ä");
        ENTITY_MAP.put("euml", "ë");
        ENTITY_MAP.put("Euml", "Ë");
        ENTITY_MAP.put("iuml", "ï");
        ENTITY_MAP.put("Iuml", "Ï");
        ENTITY_MAP.put("Kuml", "K̈");
        ENTITY_MAP.put("Ouml", "Ö");
        ENTITY_MAP.put("ouml", "ö");
        ENTITY_MAP.put("uuml", "ü");
        ENTITY_MAP.put("Ccedil", "Ç");
        ENTITY_MAP.put("ccedil", "ç");
        ENTITY_MAP.put("agrave", "à");
        ENTITY_MAP.put("Agrave", "À");
        ENTITY_MAP.put("egrave", "è");
        ENTITY_MAP.put("Egrave", "È");
        ENTITY_MAP.put("igrave", "ì");
        ENTITY_MAP.put("Ograve", "Ò");
        ENTITY_MAP.put("ograve", "ò");
        ENTITY_MAP.put("ugrave", "ù");

        // Additional Symbol Entities
        ENTITY_MAP.put("hyph", "-");
        ENTITY_MAP.put("blank", " ");
        ENTITY_MAP.put("sect", "§");
        ENTITY_MAP.put("para", "¶");
        ENTITY_MAP.put("cir", "○");
        ENTITY_MAP.put("rsquo", "’");
        ENTITY_MAP.put("mu", "μ");
        ENTITY_MAP.put("times", "x");
        ENTITY_MAP.put("bull", "•");
        ENTITY_MAP.put("ge", "≥");
        ENTITY_MAP.put("reg", "®");
        ENTITY_MAP.put("cent", "¢");
    }

    @Override
    public void parse(File fr94Directory, DocumentConsumer consumer) throws IOException {
        traverseDirectory(fr94Directory, consumer);
    }

    private void traverseDirectory(File directory, DocumentConsumer consumer) throws IOException {
        for (File file : directory.listFiles()) {
            if (file.isDirectory()) {
                System.out.println("Parsing fr94 directory: " + file.getName());
                // Recursive call to handle subdirectories
                traverseDirectory(file, consumer);
            } else if (file.isFile() && file.getName().startsWith("fr")) {
                parseFR94File(file, consumer);
            }
        }
    }

    public void parseSingleFile(File fr94File, DocumentConsumer consumer) throws IOException {
        if (fr94File.isFile()) {
            parseFR94File(fr94File, consumer);
        } else {
            throw new IllegalArgumentException("The provided file is not valid: " + fr94File.getPath());
        }
    }

    private void parseFR94File(File fr94File, DocumentConsumer consumer) throws IOException {
        String fileContent;
        try {
            fileContent = Files.readString(fr94File.toPath(), StandardCharsets.ISO_8859_1);
        } catch (MalformedInputException e) {
            System.err.println("Encoding error reading file: " + fr94File.getName());
            return;
        }
    
        fileContent = replaceSpecialCharacters(fileContent);
    
        Document doc = Jsoup.parse(fileContent, "", org.jsoup.parser.Parser.xmlParser());
    
        for (Element element : doc.select("DOC")) {
            DocumentData fr94Data = new DocumentData();
    
            fr94Data.setDocNo(parseField(element, "DOCNO"));
            fr94Data.setTitle(parseField(element, "DOCTITLE"));
            fr94Data.setAbs(parseField(element, "SUMMARY"));
            fr94Data.setUsDept(parseField(element, "USDEPT"));
            fr94Data.setAgency(parseField(element, "AGENCY"));
            fr94Data.setAction(parseField(element, "ACTION"));
            fr94Data.setDate(parseField(element, "DATE"));
            fr94Data.setSupplementary(parseField(element, "SUPPLEM"));
    
            Element textElement = element.selectFirst("TEXT");
            if (textElement != null) {
                Element cleanedTextElement = excludeFieldsFromElement(textElement, "DOCTITLE", "USDEPT", "AGENCY", "SUMMARY", "ACTION", "SUPPLEM", "DATE");
                fr94Data.setText(cleanText(cleanedTextElement.text()));
            }
    
            consumer.consume(fr94Data);
        }
    }

    private String parseField(Element element, String tagName) {
        Element fieldElement = element.selectFirst(tagName);
        if (fieldElement != null) {
            String content = cleanTextOrNull(fieldElement.text());
            if (tagName.equals("AGENCY") && content.startsWith("AGENCY:")) {
                content = content.substring("AGENCY:".length()).trim();
            } else if (tagName.equals("ACTION") && content.startsWith("ACTION:")) {
                content = content.substring("ACTION:".length()).trim();
            } else if (tagName.equals("SUPPLEM") && content.startsWith("SUPPLEMENTARY INFORMATION:")) {
                content = content.substring("SUPPLEMENTARY INFORMATION:".length()).trim();
            }
            return content;
        }
        return null;
    }
    
    private String cleanTextOrNull(String content) {
        content = cleanText(content);
        return content.isEmpty() ? null : content;
    }

    private Element excludeFieldsFromElement(Element element, String... fields) {
        for (String field : fields) {
            element.select(field).forEach(Element::remove);
        }
        return element;
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

    private String cleanText(String content) {
        if (content == null) {
            return "";
        }

        content = content.replaceAll("<!--.*?-->", "");

        content = content.replaceAll("<(USDEPT|AGENCY|DOCTITLE|SUMMARY|ACTION|SUPPLEM)[^>]*>", "");
        content = content.replaceAll("</(USDEPT|AGENCY|DOCTITLE|SUMMARY|ACTION|SUPPLEM)>", "");

        content = content.replaceAll("<(ITAG|FTAG|STAG|TTAG|ZTAG|QTAG)[^>]*>", "");
        content = content.replaceAll("</(ITAG|FTAG|STAG|TTAG|ZTAG|QTAG)>", "");

        content = content.replaceAll("&hyph;", "-");
        content = content.replaceAll("\\s{2,}", " ");

        return content.trim();
    }
}