package com.mergedata.config;

import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 全链路追踪号: 请求进入时生成traceId写入MDC, 同一次请求的全部日志带同一号; 前端可传X-Trace-Id串联, 响应头回显便于排查回报
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class TraceIdFilter extends OncePerRequestFilter {

    public static final String TRACE_ID = "traceId";
    public static final String HEADER = "X-Trace-Id";

    /** 链路内日志行号: 请求进入时初始化, 同一线程(本系统无异步)内由LogLineSeqConverter递增 */
    public static final ThreadLocal<AtomicInteger> LINE_SEQ = new ThreadLocal<>();

    /** 追踪号前缀(如CASH_), 仅拼接自动生成的号; 前端传入的X-Trace-Id原样沿用 */
    @Value("${app.log.trace-prefix:}")
    private String tracePrefix;

    /** 请求全局序号(追踪号尾缀, 00-99循环) */
    private static final AtomicInteger SEQ = new AtomicInteger(0);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        String traceId = request.getHeader(HEADER);
        // 防日志伪造: 仅保留字母数字与连字符并截断32位
        traceId = (traceId == null ? "" : traceId.replaceAll("[^a-zA-Z0-9-]", "")).trim();
        if (traceId.length() == 0 || traceId.length() > 32) {
            // 前缀_时间戳毫秒_全局两位序号, 纯数字可读且按时间有序
            traceId = (tracePrefix == null ? "" : tracePrefix)
                    + System.currentTimeMillis() + "_" + String.format("%02d", SEQ.getAndIncrement() % 100);
        }
        MDC.put(TRACE_ID, traceId);
        LINE_SEQ.set(new AtomicInteger(0));
        response.setHeader(HEADER, traceId);
        try {
            chain.doFilter(request, response);
        } finally {
            MDC.clear();
            LINE_SEQ.remove();
        }
    }
}
