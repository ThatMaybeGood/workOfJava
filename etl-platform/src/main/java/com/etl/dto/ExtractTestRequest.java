package com.etl.dto;

import lombok.Data;

/**
 * 抽取调试请求 —— 内联的抽取配置，可以不保存任务直接测试抽取
 */
@Data
public class ExtractTestRequest {

    /** 源数据类型: TABLE / SQL / PROCEDURE / VIEW / HTTP / SOAP / FILE */
    private String sourceType;

    /** 数据源名称 (DB类型时需要) */
    private String dataSourceName;

    // ── DB 类型 ──
    private String tableName;
    private String procedureName;
    private String sqlText;
    private String viewName;
    private String sourceParams;

    // ── HTTP / SOAP 类型 ──
    private String url;
    private String httpMethod;
    private String headers;
    private String requestBody;
    private String authType;
    private String authUsername;
    private String authPassword;
    private String authToken;
    private String responseType;        // JSON / XML
    private String dataPath;            // JSONPath / XPath 定位数据数组
    private String pagination;          // Y / N
    private String pageParam;
    private String sizeParam;
    private Integer pageSize;
    private Integer timeout;

    // ── SOAP 特有 ──
    private String soapAction;
    private String soapBinding;         // SOAP11 / SOAP12
    private String soapNamespace;       // XML 命名空间

    // ── 提取行数限制 ──
    private Integer limit;
}
