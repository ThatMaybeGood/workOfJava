package com.reports.service;

import com.reports.dto.common.ApiRequest;
import com.reports.dto.common.ApiResponse;

/**
 * 网关服务接口
 */
public interface GatewayService {

    /**
     * 处理网关请求
     */
    ApiResponse<?> process(ApiRequest<?> request);

}
