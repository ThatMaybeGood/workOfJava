package com.reports.service.handler.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reports.config.PageConfig;
import com.reports.dto.common.ApiRequest;
import com.reports.dto.common.ApiResponse;
import com.reports.dto.common.PageResult;
import com.reports.dto.request.CashDischargeSettlementRequest;
import com.reports.dto.response.cash.discharge.settlement.*;
import com.reports.service.CashDischargeSettlementService;
import com.reports.enums.ReportModule;
import com.reports.service.handler.MethodMapping;
import com.reports.service.handler.ReportHandler;
import com.reports.util.SeqUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 出院结算报表处理器
 */
@Slf4j
@Component
@MethodMapping("reports.cash.cash-discharge-settlement")
public class CashDischargeSettlementHandler implements ReportHandler<CashDischargeSettlementRequest, CashDischargeSettlementResponse> {

    private final CashDischargeSettlementService cashDischargeSettlementService;
    private final ObjectMapper objectMapper;
    private final PageConfig pageConfig;

    @Autowired
    public CashDischargeSettlementHandler(CashDischargeSettlementService cashDischargeSettlementService, ObjectMapper objectMapper, PageConfig pageConfig) {
        this.cashDischargeSettlementService = cashDischargeSettlementService;
        this.objectMapper = objectMapper;
        this.pageConfig = pageConfig;
    }

    private static final ReportModule MODULE = ReportModule.CASH_DISCHARGE_SETTLEMENT;

    @Override
    public ApiResponse<CashDischargeSettlementResponse> handle(ApiRequest<Object> request) {
        SeqUtil.next();
        log.info("[{}] 处理请求", MODULE.getChineseName());

        CashDischargeSettlementRequest body;
        if (request.getBody() instanceof CashDischargeSettlementRequest) {
            body = (CashDischargeSettlementRequest) request.getBody();
        } else {
            body = objectMapper.convertValue(request.getBody(), CashDischargeSettlementRequest.class);
        }
        if (body == null) {
            body = new CashDischargeSettlementRequest();
        }

        OverviewData overview = cashDischargeSettlementService.queryOverview(body);
        ChartsData charts = cashDischargeSettlementService.queryCharts(body);

        Integer page = body.getPage() != null ? body.getPage() : pageConfig.getDefaultPage();
        Integer pageSize = body.getPageSize() != null ? body.getPageSize() : pageConfig.getDefaultPageSize();
        PageResult<TableItem> table = cashDischargeSettlementService.queryTable(body, page, pageSize);

        CashDischargeSettlementResponse response = new CashDischargeSettlementResponse();
        response.setOverview(overview);
        response.setCharts(charts);
        response.setTable(table);

        return ApiResponse.success(response, MODULE.getChineseName() + "查询成功！");
    }

}