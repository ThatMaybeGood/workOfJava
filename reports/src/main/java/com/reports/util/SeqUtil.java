package com.reports.util;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 链路内序号工具类
 *
 * 在同一 TraceId 链路内，提供递增序号，用于标记处理步骤
 *
 * 使用示例：
 *   SeqUtil.next();
 *   log.info("查询数据库...");
 *
 *   SeqUtil.next();
 *   log.info("组装响应...");
 *
 * 日志输出：
 *   [YQ008403290] [1] 查询数据库...
 *   [YQ008403290] [2] 组装响应...
 */
@Slf4j
public class SeqUtil {

    private static final ThreadLocal<AtomicInteger> SEQ_HOLDER = new ThreadLocal<>();

    public static final String SEQ_KEY = "seq";

    /**
     * 初始化序号（在链路开始时调用）
     */
    public static void init() {
        SEQ_HOLDER.set(new AtomicInteger(0));
        MDC.put(SEQ_KEY, "0");
    }

    /**
     * 获取下一个序号
     */
    public static int next() {
        AtomicInteger counter = SEQ_HOLDER.get();
        if (counter == null) {
            counter = new AtomicInteger(0);
            SEQ_HOLDER.set(counter);
        }
        int seq = counter.incrementAndGet();
        MDC.put(SEQ_KEY, String.valueOf(seq));
        return seq;
    }

    /**
     * 获取当前序号
     */
    public static int current() {
        AtomicInteger counter = SEQ_HOLDER.get();
        return counter != null ? counter.get() : 0;
    }

    /**
     * 清除序号
     */
    public static void clear() {
        SEQ_HOLDER.remove();
        MDC.remove(SEQ_KEY);
    }

}
