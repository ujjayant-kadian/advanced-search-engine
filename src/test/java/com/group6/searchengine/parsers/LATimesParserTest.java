package com.group6.searchengine.parsers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;

import com.group6.searchengine.data.DocumentData;

class LATimesParserTest {

    @Test
    void testParseLATimesFile() {
        File latimesDirectory = new File("assignment-2/latimes/la122989");
        LATimesParser parser = new LATimesParser();
        String outputFilePath = "test-outputs/latimes-parser.txt";

        try {
            TestConsumer consumer = new TestConsumer(outputFilePath);
            parser.parseSingleFile(latimesDirectory, consumer);
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
            writer = new BufferedWriter(new FileWriter(outputFile));
        }

        @Override
        public void consume(DocumentData documentData) {
            try {
                writer.write("Document No: " + documentData.getDocNo());
                writer.newLine();
                writer.write("Date: " + documentData.getDate());
                writer.newLine();
                writer.write("Title: " + documentData.getTitle());
                writer.newLine();
                writer.write("Author: " + documentData.getAuthor());
                writer.newLine();
                writer.write("Abstract: " + documentData.getAbs());
                writer.newLine();
                writer.write("Text: " + documentData.getText());
                writer.newLine();
                writer.write("Section: " + documentData.getSection());
                writer.newLine();
                writer.write("Type: " + documentData.getType());
                writer.newLine();
                writer.write("Graphic: " + documentData.getGraphic());
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
