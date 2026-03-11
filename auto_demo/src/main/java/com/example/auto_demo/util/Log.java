package com.example.auto_demo.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

public class Log {
    // 这里的 Logger 名称要对应，建议直接用 LoggerFactory.getLogger(Log.class)
    private static final Logger log = LoggerFactory.getLogger(Log.class);

    private static void autoIncrement() {
        String current = MDC.get("step");
        int next = (current == null || current.isEmpty()) ? 1 : Integer.parseInt(current) + 1;
        MDC.put("step", String.valueOf(next));
    }

    // INFO 级别
    public static void info(String msg) {
        autoIncrement();
        log.info(msg);
    }

    // TRACE 级别 (对应你样例中的 [TRACE])
    public static void trace(String msg) {
        autoIncrement();
        log.trace(msg);
    }

    // ERROR 级别
    public static void error(String msg) {
        autoIncrement();
        log.error(msg);
    }
}
