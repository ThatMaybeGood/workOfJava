package com.reports.logback;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;

/**
 * 链路内逐行递增序号转换器
 * 每打印一行日志自动+1(同一线程): mybatis 的 Preparing/Parameters/Total 自动形成连续号段, 按号段归组;
 * 无链路(如启动日志)打印"-"
 */
public class StepSeqConverter extends ClassicConverter {

    private static final ThreadLocal<Integer> COUNTER = new ThreadLocal<>();

    /** 链路开始时清零(LogAspect → SeqUtil.init) */
    public static void reset() {
        COUNTER.set(0);
    }

    /** 链路结束时清理, 防线程池线程复用串号 */
    public static void clear() {
        COUNTER.remove();
    }

    @Override
    public String convert(ILoggingEvent event) {
        Integer c = COUNTER.get();
        if (c == null) {
            return "-";
        }
        c = c + 1;
        COUNTER.set(c);
        return String.valueOf(c);
    }
}
