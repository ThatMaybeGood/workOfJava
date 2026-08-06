package com.etl.enums;

public enum WriteMode {
    INSERT("INSERT"),
    MERGE("MERGE");

    private final String value;

    WriteMode(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static WriteMode fromValue(String value) {
        for (WriteMode mode : values()) {
            if (mode.value.equalsIgnoreCase(value)) {
                return mode;
            }
        }
        throw new IllegalArgumentException("Unknown WriteMode: " + value);
    }
}
