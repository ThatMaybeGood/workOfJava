package com.reports.service.handler;

import com.reports.dto.common.ApiRequest;
import com.reports.dto.common.ApiResponse;

/**
 * 报表处理器接口
 *
 * @param <T> 请求 Body 类型
 * @param <R> 响应 Body 类型
 */
public interface ReportHandler<T, R> {

    /**
     * 获取处理器支持的 method
     */
    String getMethod();

    /**
     * 处理请求
     */
    ApiResponse<R> handle(ApiRequest<T> request);

}
