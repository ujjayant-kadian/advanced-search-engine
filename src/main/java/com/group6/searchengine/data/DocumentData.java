package com.group6.searchengine.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentData {
    private String docNo;
    private String author;
    private String date;
    private String title;
    private String abs;
    private String text;
    private String language;
    private String region;
    private String location;
}
