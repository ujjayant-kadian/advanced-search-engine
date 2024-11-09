package com.group6.searchengine.parsers;

import java.io.File;
import java.io.IOException;
import java.util.List;

public interface DatasetParser<T> {
    void parse(File directory, DocumentConsumer<T> consumer) throws IOException;
}
