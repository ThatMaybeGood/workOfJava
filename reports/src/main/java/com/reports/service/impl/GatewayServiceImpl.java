package com.reports.service.impl;

import com.reports.dto.common.ApiRequest;
import com.reports.dto.common.ApiResponse;
import com.reports.enums.ResultCode;
import com.reports.exception.BusinessException;
import com.reports.service.GatewayService;
import com.reports.service.handler.ReportHandler;
import com.reports.service.handler.ReportHandlerFactory;
import com.reports.util.SeqUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 网关服务实现
 */
@Slf4j
@Service
public class GatewayServiceImpl implements GatewayService {

    private final ReportHandlerFactory handlerFactory;

    @Autowired
    public GatewayServiceImpl(ReportHandlerFactory handlerFactory) {
        this.handlerFactory = handlerFactory;
    }

    @Override
    @SuppressWarnings("unchecked")
    public ApiResponse<?> process(ApiRequest request) {
        // 验证 method
        if (request == null || request.getHead() == null || !StringUtils.hasText(request.getMethod())) {
            throw new BusinessException(ResultCode.PARAM_MISSING, "请求报文或 method 不能为空");
        }

        String method = request.getMethod();
        SeqUtil.next();
        log.info("开始处理请求，method=[{}]", method);

        // 获取处理器
        ReportHandler handler = handlerFactory.getHandler(method);
        if (handler == null) {
            throw new BusinessException(ResultCode.METHOD_NOT_FOUND, "method=" + method);
        }

        // 执行处理
        return handler.handle(request);
    }

}
