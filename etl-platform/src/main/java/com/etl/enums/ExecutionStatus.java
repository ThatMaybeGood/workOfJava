package com.etl.enums;

public enum ExecutionStatus {
    RUNNING("RUNNING"),
    SUCCESS("SUCCESS"),
    FAILED("FAILED"),
    TIMEOUT("TIMEOUT");

    private final String value;

    ExecutionStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static ExecutionStatus fromValue(String value) {
        for (ExecutionStatus status : values()) {
            if (status.value.equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown ExecutionStatus: " + value);
    }
}
