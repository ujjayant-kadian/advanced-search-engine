package com.group6.searchengine.parsers;

import com.group6.searchengine.data.DocumentData;
import java.io.File;
import java.io.IOException;
import java.util.List;

public interface DatasetParser {
    void parse(File directory, DocumentConsumer consumer) throws IOException;
}
