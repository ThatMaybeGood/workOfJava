package com.reports.util;

import org.slf4j.MDC;

/**
 * MDC 工具类
 */
public class MdcUtil {

    public static final String TRACE_ID_KEY = "traceId";

    /**
     * 设置 TraceId
     */
    public static void setTraceId(String traceId) {
        MDC.put(TRACE_ID_KEY, traceId);
    }

    /**
     * 获取 TraceId
     */
    public static String getTraceId() {
        return MDC.get(TRACE_ID_KEY);
    }

    /**
     * 清除 TraceId
     */
    public static void clear() {
        MDC.remove(TRACE_ID_KEY);
    }

}
