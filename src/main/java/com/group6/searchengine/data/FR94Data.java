package com.group6.searchengine.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * FR94Data class represents the data structure of the Federal Register 94 dataset.
 * It contains fields that need to be indexed separately and fields that need to be indexed together.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FR94Data {

    // fields that need to be indexed separately
    private String docNo;
    private String docTitle;
    private String agency;
    private String usDept;
    private String summary;

    // fields that need to be indexed together
    private String fullText;
    // the following fields are not included in the fullText field
    // <TEXT>: <TABLE>, <FOOTNOTE>, <FOOTCITE>, <FOOTNAME>, <FURTHER>, <ACTION>,<SUPPLEM>
}

