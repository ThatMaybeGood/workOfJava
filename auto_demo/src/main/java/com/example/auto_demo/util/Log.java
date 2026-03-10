package com.example.auto_demo.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

public class Log {
    private static final Logger log = LoggerFactory.getLogger("SystemLog");

    // 每次打印前自动加1
    private static void autoIncrement() {
        String current = MDC.get("step");
        int next = (current == null) ? 1 : Integer.parseInt(current) + 1;
        MDC.put("step", String.valueOf(next));
    }

    public static void info(String msg) {
        autoIncrement();
        log.info(msg);
    }

    // 如果需要传参的写法
    public static void info(String format, Object... arguments) {
        autoIncrement();
        log.info(format, arguments);
    }

    // 你可以根据需要添加 error 或 warn 方法
    public static void error(String msg) {
        autoIncrement();
        log.error(msg);
    }
}
