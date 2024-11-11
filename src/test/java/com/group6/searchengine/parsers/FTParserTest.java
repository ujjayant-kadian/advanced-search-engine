package com.group6.searchengine.parsers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;

import com.group6.searchengine.data.DocumentData;

class FTParserTest {

    @Test
    void testParseFTFile() {
        File ftDirectory = new File("assignment-2/ft/ft911/ft911_1");
        FTParser parser = new FTParser();
        String outputFilePath = "test-outputs/ft-parser.txt";

        try {
            TestConsumer consumer = new TestConsumer(outputFilePath);
            parser.parseSingleFile(ftDirectory, consumer);
            assertNotNull(consumer);
            consumer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static class TestConsumer implements DocumentConsumer {
        private BufferedWriter writer;

        public TestConsumer(String outputFilePath) throws IOException {
            File outputFile = new File(outputFilePath);
            outputFile.getParentFile().mkdirs();
            writer = new BufferedWriter(new FileWriter(new File(outputFilePath)));
        }

        @Override
        public void consume(DocumentData documentData) {
            try {
                writer.write("Document No: " + documentData.getDocNo());
                writer.newLine();
                writer.write("Profile: " + documentData.getProfile());
                writer.newLine();
                writer.write("Date: " + documentData.getDate());
                writer.newLine();
                writer.write("Headline: " + documentData.getHeadline());
                writer.newLine();
                writer.write("By line: " + documentData.getByline());
                writer.newLine();
                writer.write("Text: " + documentData.getText());
                writer.newLine();
                writer.write("Pub: " + documentData.getPub());
                writer.newLine();
                writer.write("Page: " + documentData.getPage());
                writer.newLine();
                writer.write("--------------------------------------------------");
                writer.newLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void close() throws IOException {
            writer.close();
        }
    }
}
