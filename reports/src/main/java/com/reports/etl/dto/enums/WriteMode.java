package com.reports.etl.dto.enums;

public enum WriteMode {
    INSERT("INSERT"),
    UPDATE("UPDATE"),
    UPSERT("UPSERT");

    private final String value;

    WriteMode(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}