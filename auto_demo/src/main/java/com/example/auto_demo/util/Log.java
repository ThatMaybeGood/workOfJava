package com.example.auto_demo.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Log {
    private static final Logger log = LoggerFactory.getLogger(Log.class);

    public static void info(String format, Object... arguments) {
        log.info(format, arguments);
    }

    public static void trace(String format, Object... arguments) {
        log.trace(format, arguments);
    }


    public static void debug(String format, Object... arguments) {
        log.debug(format, arguments);
    }

    public static void warn(String format, Object... arguments) {
        log.warn(format, arguments);
    }

    public static void error(String msg, Throwable t) {
        log.error(msg, t);
    }

    public static void error(String msg) {
        log.error(msg);
    }
    public static void error(String format, Object... arguments) {
        log.error(format, arguments);
    }
}