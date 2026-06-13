package com.reports.util;

import com.reports.config.TraceIdConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 追踪号生成器
 */
@Component
public class TraceIdGenerator {

    private static final AtomicLong COUNTER = new AtomicLong(0);
    private static final SecureRandom RANDOM = new SecureRandom();

    private final TraceIdConfig traceIdConfig;

    @Autowired
    public TraceIdGenerator(TraceIdConfig traceIdConfig) {
        this.traceIdConfig = traceIdConfig;
    }

    /**
     * 生成追踪号
     */
    public String generate() {
        String prefix = traceIdConfig.getPrefix();
        int numberLength = traceIdConfig.getNumberLength() != null ? traceIdConfig.getNumberLength() : 9;
        boolean includeBrackets = traceIdConfig.getIncludeBrackets() != null ? traceIdConfig.getIncludeBrackets() : true;

        // 生成指定长度的数字
        StringBuilder numberBuilder = new StringBuilder();
        for (int i = 0; i < numberLength; i++) {
            numberBuilder.append(RANDOM.nextInt(10));
        }

        String traceId = prefix + numberBuilder.toString();

        if (includeBrackets) {
            return "[" + traceId + "]";
        }

        return traceId;
    }

    /**
     * 生成简单追踪号（无前缀括号等，仅用于内部）
     */
    public static String simpleTraceId() {
        return String.valueOf(System.currentTimeMillis()) + COUNTER.incrementAndGet();
    }

}
