package com.reports.service.handler.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reports.dto.common.ApiRequest;
import com.reports.dto.common.ApiResponse;
import com.reports.dto.request.OutpatientFinanceRequest;
import com.reports.dto.response.cash.outpatient.finance.OutpatientFinanceResponse;
import com.reports.enums.ReportModule;
import com.reports.service.OutpatientFinanceService;
import com.reports.service.handler.MethodMapping;
import com.reports.service.handler.ReportHandler;
import com.reports.util.SeqUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
        response.setIndicator(outpatientFinanceService.queryIndicator(body));
        response.setDetailList(outpatientFinanceService.queryDetailList(body));
        response.setBarList(outpatientFinanceService.queryBarList(body));
        response.setPieList(outpatientFinanceService.queryPieList(body));

        return ApiResponse.success(response, MODULE.getChineseName() + "查询成功！");
    }

}
