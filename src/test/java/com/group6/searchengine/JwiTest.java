package com.group6.searchengine;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import edu.mit.jwi.Dictionary;
import edu.mit.jwi.item.ISynset;
import edu.mit.jwi.item.IWord;
import edu.mit.jwi.item.POS;

public class JwiTest {

    private static Dictionary dictionary;

    @BeforeAll
    public static void setup() {
        try {
            String wordNetPath = "dict/";
            @SuppressWarnings("deprecation")
            URL url = new URL("file", null, wordNetPath);
            dictionary = new Dictionary(url);
            dictionary.open();
            assertNotNull(dictionary, "Dictionary should be initialized and not null");
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to set up WordNet dictionary", e);
        }
    }

    @AfterAll
    public static void teardown() {
        if (dictionary != null) {
            dictionary.close();
        }
    }

    @Test
    public void testFetchSynonymsForDog() {
        List<String> synonyms = fetchSynonyms("dog", POS.NOUN);
        assertNotNull(synonyms, "Synonyms list should not be null");
        assertTrue(!synonyms.isEmpty(), "Synonyms list should not be empty");
        System.out.println("Synonyms for 'dog': " + synonyms);
    }

    @Test
    public void testFetchSynonymsForCat() {
        List<String> synonyms = fetchSynonyms("cat", POS.NOUN);
        assertNotNull(synonyms, "Synonyms list should not be null");
        assertTrue(!synonyms.isEmpty(), "Synonyms list should not be empty");
        System.out.println("Synonyms for 'cat': " + synonyms);
    }

    private List<String> fetchSynonyms(String word, POS pos) {
        List<String> synonyms = new ArrayList<>();
        try {
            var indexWord = dictionary.getIndexWord(word, pos);
            if (indexWord != null) {
                for (var wordID : indexWord.getWordIDs()) {
                    ISynset synset = dictionary.getWord(wordID).getSynset();
                    for (IWord synonym : synset.getWords()) {
                        synonyms.add(synonym.getLemma());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return synonyms;
    }
}
