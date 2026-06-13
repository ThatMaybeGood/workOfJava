package com.reports.service.handler;

import com.reports.enums.ResultCode;
import com.reports.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 报表处理器工厂 - 根据 method 路由分发
 */
@Slf4j
@Component
public class ReportHandlerFactory {

    private final List<ReportHandler<?, ?>> handlers;
    private final Map<String, ReportHandler<?, ?>> handlerMap = new ConcurrentHashMap<>();

    @Autowired
    public ReportHandlerFactory(List<ReportHandler<?, ?>> handlers) {
        this.handlers = handlers;
    }

    @PostConstruct
    public void init() {
        for (ReportHandler<?, ?> handler : handlers) {
            String method = handler.getMethod();
            if (handlerMap.containsKey(method)) {
                log.warn("Method [{}] 已存在处理器，将被覆盖", method);
            }
            handlerMap.put(method, handler);
            log.info("注册报表处理器: method=[{}], class=[{}]", method, handler.getClass().getSimpleName());
        }
        log.info("共注册 {} 个报表处理器", handlerMap.size());
    }

    /**
     * 根据 method 获取处理器
     */
    @SuppressWarnings("unchecked")
    public <T, R> ReportHandler<T, R> getHandler(String method) {
        ReportHandler<T, R> handler = (ReportHandler<T, R>) handlerMap.get(method);
        if (handler == null) {
            throw new BusinessException(ResultCode.METHOD_NOT_FOUND, "方法不存在: " + method);
        }
        return handler;
    }

    /**
     * 判断是否支持该 method
     */
    public boolean supports(String method) {
        return handlerMap.containsKey(method);
    }

}
