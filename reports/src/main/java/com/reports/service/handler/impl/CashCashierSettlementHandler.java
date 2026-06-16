package com.reports.service.handler.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reports.config.PageConfig;
import com.reports.dto.common.ApiRequest;
import com.reports.dto.common.ApiResponse;
import com.reports.dto.common.PageResult;
import com.reports.dto.request.CashCashierSettlementRequest;
import com.reports.dto.response.cash.cashier.settlement.*;
import com.reports.service.CashCashierSettlementService;
import com.reports.enums.ReportModule;
import com.reports.service.handler.MethodMapping;
import com.reports.service.handler.ReportHandler;
import com.reports.util.SeqUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 收费员结账统计处理器
 */
@Slf4j
@Component
@MethodMapping("reports.cash.cash-cashier-settlement")
public class CashCashierSettlementHandler implements ReportHandler<CashCashierSettlementRequest, CashCashierSettlementResponse> {

    private final CashCashierSettlementService cashCashierSettlementService;
    private final ObjectMapper objectMapper;
    private final PageConfig pageConfig;

    @Autowired
    public CashCashierSettlementHandler(CashCashierSettlementService cashCashierSettlementService, ObjectMapper objectMapper, PageConfig pageConfig) {
        this.cashCashierSettlementService = cashCashierSettlementService;
        this.objectMapper = objectMapper;
        this.pageConfig = pageConfig;
    }

    private static final ReportModule MODULE = ReportModule.CASH_CASHIER_SETTLEMENT;

    @Override
    public ApiResponse<CashCashierSettlementResponse> handle(ApiRequest<Object> request) {
        SeqUtil.next();
        log.info("[{}] 处理请求", MODULE.getChineseName());

        CashCashierSettlementRequest body;
        if (request.getBody() instanceof CashCashierSettlementRequest) {
            body = (CashCashierSettlementRequest) request.getBody();
        } else {
            body = objectMapper.convertValue(request.getBody(), CashCashierSettlementRequest.class);
        }
        if (body == null) {
            body = new CashCashierSettlementRequest();
        }

        OverviewData overview = cashCashierSettlementService.queryOverview(body);

        Integer page = body.getPage() != null ? body.getPage() : pageConfig.getDefaultPage();
        Integer pageSize = body.getPageSize() != null ? body.getPageSize() : pageConfig.getDefaultPageSize();
        PageResult<TableItem> table = cashCashierSettlementService.queryTable(body, page, pageSize);

        ChartData chart = cashCashierSettlementService.queryChart(body);

        CashCashierSettlementResponse response = new CashCashierSettlementResponse();
        response.setOverview(overview);
        response.setTable(table);
        response.setChart(chart);

        return ApiResponse.success(response, MODULE.getChineseName() + "查询成功！");

}
}