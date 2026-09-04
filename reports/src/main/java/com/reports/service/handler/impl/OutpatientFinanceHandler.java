package com.reports.service.handler.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reports.dto.common.ApiRequest;
import com.reports.dto.common.ApiResponse;
import com.reports.dto.request.OutpatientFinanceRequest;
import com.reports.dto.response.cash.outpatient.finance.DetailListItem;
import com.reports.dto.response.cash.outpatient.finance.OutpatientFinanceResponse;
import com.reports.enums.ReportModule;
import com.reports.service.OutpatientFinanceService;
import com.reports.service.handler.MethodMapping;
import com.reports.service.handler.ReportHandler;
import com.reports.util.SeqUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 门诊财务报表处理器
 */
@Slf4j
@Component
@MethodMapping("reports.cash.outpatient-finance")
public class OutpatientFinanceHandler implements ReportHandler<OutpatientFinanceRequest, OutpatientFinanceResponse> {

    private final OutpatientFinanceService outpatientFinanceService;
    private final ObjectMapper objectMapper;

    @Autowired
    public OutpatientFinanceHandler(OutpatientFinanceService outpatientFinanceService, ObjectMapper objectMapper) {
        this.outpatientFinanceService = outpatientFinanceService;
        this.objectMapper = objectMapper;
    }

    private static final ReportModule MODULE = ReportModule.CASH_OUTPATIENT_FINANCE;

    @Override
    public ApiResponse<OutpatientFinanceResponse> handle(ApiRequest<Object> request) {
        SeqUtil.next();
        log.info("[{}] 处理请求", MODULE.getChineseName());

        OutpatientFinanceRequest body;
        if (request.getBody() instanceof OutpatientFinanceRequest) {
            body = (OutpatientFinanceRequest) request.getBody();
        } else {
            body = objectMapper.convertValue(request.getBody(), OutpatientFinanceRequest.class);
        }
        if (body == null) {
            body = new OutpatientFinanceRequest();
        }

        OutpatientFinanceResponse response = new OutpatientFinanceResponse();

        // 按需饼图请求：前端点内层分析 tab 时携带 pieTypes，只查询指定业务类型并直接返回
        if (body.getPieTypes() != null && !body.getPieTypes().trim().isEmpty()) {
            response.setPieList(outpatientFinanceService.queryPieList(body, body.getPieTypes()));
            return ApiResponse.success(response, MODULE.getChineseName() + "饼图查询成功！");
        }

        List<DetailListItem> detailList = outpatientFinanceService.queryDetailList(body);
        response.setIndicator(outpatientFinanceService.queryIndicator(body, detailList));
        response.setDetailList(detailList);
        response.setBarList(outpatientFinanceService.queryBarList(body, detailList));
        // 饼图不再随主请求返回，由前端按内层 tab 携带 pieTypes 按需查询

        return ApiResponse.success(response, MODULE.getChineseName() + "查询成功！");
    }

}
