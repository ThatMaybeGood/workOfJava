package com.mergedata.config;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 链路内日志行号: 同一traceId(同一次请求)内从001递增, 请求结束随线程上下文回收; 非请求线程(定时/推送)显示[-]
 */
public class LogLineSeqConverter extends ClassicConverter {

    @Override
    public String convert(ILoggingEvent event) {
        AtomicInteger seq = TraceIdFilter.LINE_SEQ.get();
        return seq == null ? "-" : String.format("%03d", seq.incrementAndGet());
    }
}
