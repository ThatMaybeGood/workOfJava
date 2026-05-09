package com.demo.integration.core.trace;

import org.slf4j.MDC;

/**
 * @author Mine
 * @version 1.0
 * 描述:
 * @date 2026/5/9 23:05
 */
public class TraceContextUtil {

    public static final String TRACE_ID = "traceId";

    public static void set(String traceId) {

        MDC.put(TRACE_ID, traceId);
    }

    public static String get() {

        return MDC.get(TRACE_ID);
    }

    public static void clear() {

        MDC.remove(TRACE_ID);
    }
}
