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
 * <p>
 * 自动扫描所有 {@link ReportHandler} 实现，从 {@link MethodMapping} 注解读取路由键进行注册。
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
            Class<?> clazz = handler.getClass();
            MethodMapping mapping = clazz.getAnnotation(MethodMapping.class);
            if (mapping == null) {
                throw new IllegalStateException(
                        String.format("ReportHandler 实现类 [%s] 缺少 @MethodMapping 注解", clazz.getName()));
            }
            String method = mapping.value();
            if (method.isEmpty()) {
                throw new IllegalStateException(
                        String.format("@MethodMapping value 不能为空，类: [%s]", clazz.getName()));
            }
            if (handlerMap.containsKey(method)) {
                log.warn("Method [{}] 已存在处理器，将被覆盖", method);
            }
            handlerMap.put(method, handler);
            log.info("注册报表处理器: method=[{}], class=[{}]", method, clazz.getSimpleName());
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
