package com.reports.exception;

import com.reports.dto.common.ApiResponse;
import com.reports.enums.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;

/**
 * 全局异常处理器
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public ApiResponse<Void> handleBusinessException(BusinessException e, HttpServletRequest request) {
        log.warn("[TraceId={}] 业务异常: {}", MDC.get("traceId"), e.getMessage());
        return ApiResponse.fail(e.getCode(), e.getMessage(), e.getSubCode(), e.getSubMsg());
    }

    /**
     * 参数绑定异常
     */
    @ExceptionHandler(BindException.class)
    public ApiResponse<Void> handleBindException(BindException e, HttpServletRequest request) {
        log.warn("[TraceId={}] 参数绑定异常: {}", MDC.get("traceId"), e.getMessage());
        return ApiResponse.fail(
                ResultCode.PARAM_ERROR.getCode(),
                ResultCode.PARAM_ERROR.getMsg(),
                ResultCode.PARAM_ERROR.getSubCode(),
                e.getMessage()
        );
    }

    /**
     * 非法参数异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ApiResponse<Void> handleIllegalArgumentException(IllegalArgumentException e, HttpServletRequest request) {
        log.warn("[TraceId={}] 非法参数: {}", MDC.get("traceId"), e.getMessage());
        return ApiResponse.fail(
                ResultCode.PARAM_ERROR.getCode(),
                ResultCode.PARAM_ERROR.getMsg(),
                ResultCode.PARAM_ERROR.getSubCode(),
                e.getMessage()
        );
    }

    /**
     * 数据源异常
     */
    @ExceptionHandler(DataSourceException.class)
    public ApiResponse<Void> handleDataSourceException(DataSourceException e, HttpServletRequest request) {
        log.error("[TraceId={}] 数据源异常: {}", MDC.get("traceId"), e.getMessage(), e);
        return ApiResponse.fail(
                e.getCode(),
                ResultCode.DATASOURCE_ERROR.getMsg(),
                e.getSubCode(),
                e.getMessage()
        );
    }

    /**
     * 其他所有异常
     */
    @ExceptionHandler(Exception.class)
    public ApiResponse<Void> handleException(Exception e, HttpServletRequest request) {
        log.error("[TraceId={}] 系统异常: ", MDC.get("traceId"), e);
        return ApiResponse.fail(
                ResultCode.SYSTEM_ERROR.getCode(),
                ResultCode.SYSTEM_ERROR.getMsg(),
                ResultCode.SYSTEM_ERROR.getSubCode(),
                e.getMessage()
        );
    }

}
