package com.reports.etl.dto.enums;

public enum ExtractType {
    WEBSERVICE("WEBSERVICE"),
    PROCEDURE("PROCEDURE");

    private final String value;

    ExtractType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}