package com.reports.etl.dto.enums;

public enum DsRole {
    SOURCE("SOURCE"),
    TARGET("TARGET");

    private final String value;

    DsRole(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}