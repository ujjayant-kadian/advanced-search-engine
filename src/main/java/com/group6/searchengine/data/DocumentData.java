package com.group6.searchengine.data;

import lombok.Data;

@Data
public class DocumentData {
    // Common
    private String docNo;
    private String title;
    private String text;
    private String abs;
    private String date;
    private String section;

    //FBIS
    private String author;
    private String language;
    private String region;

    //FR94
    private String usDept;
    private String agency;
    private String action;
    private String supplementary;

    // LAT
    private String type;
    private String graphic;
}
