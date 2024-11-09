package com.group6.searchengine.parsers;

import com.group6.searchengine.data.FR94Data;
import org.junit.jupiter.api.Test;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.File;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class FR94ParserTest {

    @Test
    void testParseFR94File() {
        File fr94File = new File("../assignment-2/fr94/01/fr940110.0"); // Specify a test file path here
        FR94Parser parser = new FR94Parser();
        String outputFilePath = "test-outputs/fr94-parser.txt";

        try {
            TestConsumer consumer = new TestConsumer(outputFilePath);
            parser.parseSingleFile(fr94File, consumer);
            assertNotNull(consumer);
            consumer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static class TestConsumer implements DocumentConsumer<FR94Data> {
        private BufferedWriter writer;

        public TestConsumer(String outputFilePath) throws IOException {
            File outputFile = new File(outputFilePath);
            outputFile.getParentFile().mkdirs();
            writer = new BufferedWriter(new FileWriter(outputFilePath));
        }

        @Override
        public void consume(FR94Data documentData) {
            try {
                writer.write("Document No: " + documentData.getDocNo());
                writer.newLine();
                writer.write("Title: " + documentData.getDocTitle());
                writer.newLine();
                writer.write("Agency: " + documentData.getAgency());
                writer.newLine();
                writer.write("US Department: " + documentData.getUsDept());
                writer.newLine();
                writer.write("Summary: " + documentData.getSummary());
                writer.newLine();
                writer.write("Text: " + documentData.getFullText());
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

