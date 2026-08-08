package com.etl.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 抽取调试结果 —— 包含原始响应和解析后的数据
 */
@Data
public class ExtractTestResult {

    /** 是否成功 */
    private boolean success;

    /** 错误信息 */
    private String errorMessage;

    /** 原始响应体（文本） */
    private String rawResponse;

    /** 原始响应状态码 */
    private Integer statusCode;

    /** 原始响应头 */
    private Map<String, String> responseHeaders;

    /** 响应格式: JSON / XML */
    private String responseType;

    /** 解析后数据（列+行） */
    private List<Map<String, Object>> parsedData;

    /** 列名列表 */
    private List<String> columns;

    /** 解析出的行数 */
    private int totalRows;

    /** 执行耗时(毫秒) */
    private long durationMs;

    /** 使用的最终请求 URL */
    private String finalUrl;

    /** 使用的请求方法 */
    private String finalMethod;

    // ── 静态工厂方法 ──

    public static ExtractTestResult fail(String errorMessage) {
        ExtractTestResult r = new ExtractTestResult();
        r.setSuccess(false);
        r.setErrorMessage(errorMessage);
        return r;
    }

    public static ExtractTestResult ok(String rawResponse, String responseType,
                                        List<Map<String, Object>> data, long durationMs) {
        ExtractTestResult r = new ExtractTestResult();
        r.setSuccess(true);
        r.setRawResponse(rawResponse);
        r.setResponseType(responseType);
        r.setParsedData(data);
        r.setTotalRows(data != null ? data.size() : 0);
        r.setDurationMs(durationMs);
        if (data != null && !data.isEmpty()) {
            r.setColumns(data.get(0).keySet().stream().collect(java.util.ArrayList::new,
                    java.util.ArrayList::add, java.util.ArrayList::addAll));
        }
        return r;
    }
}
