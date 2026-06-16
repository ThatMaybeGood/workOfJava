package com.reports.aspect;

import com.reports.annotation.DataSource;
import com.reports.config.DynamicDataSourceContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * 数据源切换 AOP 切面
 * <p>
 * 拦截带有 {@link DataSource} 注解的方法，自动切换数据源。
 * 方法执行完毕后自动恢复默认数据源。
 * <p>
 * Order 值设置为 -1，确保在事务切面之前执行。
 */
@Slf4j
@Aspect
@Component
@Order(-1)
public class DataSourceAspect {

    @Around("@annotation(com.reports.annotation.DataSource) || @within(com.reports.annotation.DataSource)")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();

        // 优先获取方法上的注解，其次获取类上的注解
        DataSource dataSource = method.getAnnotation(DataSource.class);
        if (dataSource == null) {
            dataSource = point.getTarget().getClass().getAnnotation(DataSource.class);
        }

        if (dataSource != null) {
            String dsKey = dataSource.value();
            log.info("切换数据源: [{}] -> 方法: [{}.{}]", dsKey,
                    point.getTarget().getClass().getSimpleName(), method.getName());
            DynamicDataSourceContextHolder.set(dsKey);
        }

        try {
            return point.proceed();
        } finally {
            // 清除数据源，恢复默认
            DynamicDataSourceContextHolder.clear();
            if (dataSource != null) {
                log.info("恢复默认数据源: [{}]", DynamicDataSourceContextHolder.DEFAULT_DS);
            }
        }
    }

}
