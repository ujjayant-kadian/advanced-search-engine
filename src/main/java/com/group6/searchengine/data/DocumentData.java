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

    //FBIS
    private String author;
    private String language;
    private String region;
    private String location;

    //FR94
    private String usDept;
    private String agency;
    private String action;
    private String supplementary;

    // LAT
    private String section;
    private String type;
    private String graphic;
}
