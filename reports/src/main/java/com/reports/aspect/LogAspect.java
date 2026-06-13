package com.reports.aspect;

import com.reports.util.MdcUtil;
import com.reports.util.TraceIdGenerator;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * 日志切面 - 链路追踪
 */
@Slf4j
@Aspect
@Component
public class LogAspect {

    private final TraceIdGenerator traceIdGenerator;

    @Autowired
    public LogAspect(TraceIdGenerator traceIdGenerator) {
        this.traceIdGenerator = traceIdGenerator;
    }

    /**
     * 切点：GatewayController 的所有方法
     */
    @Pointcut("execution(* com.reports.controller.GatewayController.*(..))")
    public void gatewayPointcut() {
    }

    /**
     * 请求进入时
     */
    @Before("gatewayPointcut()")
    public void doBefore(JoinPoint joinPoint) {
        String traceId = traceIdGenerator.generate();
        MdcUtil.setTraceId(traceId);
        log.info("[TraceId={}] 接口请求进入: {}.{}, args={}",
                traceId,
                joinPoint.getTarget().getClass().getSimpleName(),
                joinPoint.getSignature().getName(),
                Arrays.toString(joinPoint.getArgs()));
    }

    /**
     * 请求返回时
     */
    @AfterReturning(pointcut = "gatewayPointcut()", returning = "result")
    public void doAfterReturning(JoinPoint joinPoint, Object result) {
        String traceId = MdcUtil.getTraceId();
        log.info("[TraceId={}] 接口请求返回: {}", traceId, result);
        MdcUtil.clear();
    }

    /**
     * 异常时
     */
    @AfterThrowing(pointcut = "gatewayPointcut()", throwing = "ex")
    public void doAfterThrowing(JoinPoint joinPoint, Throwable ex) {
        String traceId = MdcUtil.getTraceId();
        log.error("[TraceId={}] 接口请求异常: ", traceId, ex);
        MdcUtil.clear();
    }

}
