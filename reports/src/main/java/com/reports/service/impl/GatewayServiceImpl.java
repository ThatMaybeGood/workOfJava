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

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Map;

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

        // 查询跨度兜底: 日期范围超一年直接拒绝, 防几年跨度全表扫描拖垮共享库(按月控件上限12个月, 留余量)
        validateDateSpan(request.getBody());

        // 执行处理
        return handler.handle(request);
    }

    private void validateDateSpan(Object body) {
        if (!(body instanceof Map)) {
            return;
        }
        Map<?, ?> map = (Map<?, ?>) body;
        checkSpan(map.get("startDate"), map.get("endDate"));
        checkSpan(map.get("lastStartDate"), map.get("lastEndDate"));
    }

    private void checkSpan(Object startObj, Object endObj) {
        if (!(startObj instanceof String) || !(endObj instanceof String)) {
            return;
        }
        try {
            LocalDate start = LocalDate.parse(((String) startObj).trim());
            LocalDate end = LocalDate.parse(((String) endObj).trim());
            if (ChronoUnit.DAYS.between(start, end) > 370) {
                throw new BusinessException(ResultCode.PARAM_ERROR, "查询时间跨度不能超过一年，请缩小日期范围");
            }
        } catch (DateTimeParseException e) {
            // 格式不对交给后续参数校验报错
        }
    }

}
