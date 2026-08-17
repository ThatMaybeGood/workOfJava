package com.reports.etl.dto.enums;

public enum StepName {
    EXTRACT("EXTRACT"),
    TRANSFORM("TRANSFORM"),
    LOAD("LOAD");

    private final String value;

    StepName(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}