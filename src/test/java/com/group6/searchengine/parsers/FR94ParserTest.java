package com.group6.searchengine.parsers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;

import com.group6.searchengine.data.DocumentData;

class FR94ParserTest {

    @Test
    void testParseFBISFile() {
        File fbisDirectory = new File("assignment-2/fr94/12/fr941202.0");
        FR94Parser parser = new FR94Parser();
        String outputFilePath = "test-outputs/fr94-parser.txt";

        try {
            TestConsumer consumer = new TestConsumer(outputFilePath);
            parser.parseSingleFile(fbisDirectory, consumer);
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
                writer.write("Title: " + documentData.getTitle());
                writer.newLine();
                writer.write("Abstract: " + documentData.getAbs());
                writer.newLine();
                writer.write("Text: " + documentData.getText());
                writer.newLine();
                writer.write("US Dept: " + documentData.getUsDept());
                writer.newLine();
                writer.write("Agency: " + documentData.getAgency());
                writer.newLine();
                writer.write("Action: " + documentData.getAction());
                writer.newLine();
                writer.write("Supplementary: " + documentData.getSupplementary());
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
