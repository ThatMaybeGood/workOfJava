package com.etl.enums;

public enum HttpAuthType {
    NONE("NONE"),
    BASIC("BASIC"),
    TOKEN("TOKEN");

    private final String value;

    HttpAuthType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static HttpAuthType fromValue(String value) {
        for (HttpAuthType type : values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown HttpAuthType: " + value);
    }
}
