package com.group6.searchengine.parsers;

import com.group6.searchengine.data.DocumentData;
import java.io.IOException;

public interface DocumentConsumer {
    void consume(DocumentData docData) throws IOException;
}
