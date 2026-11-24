package com.mergedata.config;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 日志全局行号: 每条日志事件全局递增(000000-999999循环), 与traceId配合定位; 同一事件写多文件时共享序列, 单文件内可能跳号
 */
public class LogLineSeqConverter extends ClassicConverter {

    private static final AtomicInteger SEQ = new AtomicInteger(0);

    @Override
    public String convert(ILoggingEvent event) {
        return String.format("%06d", SEQ.getAndIncrement() % 1000000);
    }
}
