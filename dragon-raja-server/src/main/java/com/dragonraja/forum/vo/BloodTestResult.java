package com.dragonraja.forum.vo;

import lombok.Data;

@Data
public class BloodTestResult {
    private String grade;
    private int correctCount;
    private int totalCount;
    private int rate;
    private String yanling;
    private String sequence;
    private String bloodType;
}
