package com.group6.searchengine.parsers;

import java.io.IOException;

public interface DocumentConsumer<T> {
    void consume(T docData) throws IOException;
}
