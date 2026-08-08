package com.etl.enums;

/**
 * 数据源连接协议类型
 */
public enum ProtocolType {
    JDBC("JDBC", "数据库连接"),
    HTTP("HTTP", "HTTP接口"),
    SOAP("SOAP", "WebService/SOAP接口"),
    FILE("FILE", "文件");

    private final String value;
    private final String label;

    ProtocolType(String value, String label) {
        this.value = value;
        this.label = label;
    }

    public String getValue() {
        return value;
    }

    public String getLabel() {
        return label;
    }

    public static ProtocolType fromValue(String value) {
        for (ProtocolType type : values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown ProtocolType: " + value);
    }
}
