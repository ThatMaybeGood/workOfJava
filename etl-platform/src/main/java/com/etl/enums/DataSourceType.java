package com.etl.enums;

public enum DataSourceType {
    ORACLE("ORACLE"),
    MYSQL("MYSQL"),
    POSTGRESQL("POSTGRESQL"),
    SQLSERVER("SQLSERVER");

    private final String value;

    DataSourceType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static DataSourceType fromValue(String value) {
        for (DataSourceType type : values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown DataSourceType: " + value);
    }
}
