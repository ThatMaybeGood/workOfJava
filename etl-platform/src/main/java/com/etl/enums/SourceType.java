package com.etl.enums;

public enum SourceType {
    PROCEDURE("PROCEDURE"),
    SQL("SQL"),
    VIEW("VIEW"),
    TABLE("TABLE"),
    HTTP("HTTP"),
    FILE("FILE");

    private final String value;

    SourceType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static SourceType fromValue(String value) {
        for (SourceType type : values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown SourceType: " + value);
    }
}
