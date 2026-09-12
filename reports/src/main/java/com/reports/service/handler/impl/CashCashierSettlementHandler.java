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
 * 收费员结账统计处理器。
 *
 * <p>页面按子接口分别取数（overview / table / chart），读的是各自那一层的结构：
 * overview 是扁平指标、table 是分页对象、chart 是 title/categories/data 的图表对象。
 * 所以这里按 {@code RequestHead.endpoint} 返回对应结构；
 * 没传 endpoint 时（旧调用）仍返回合并后的完整对象，保持兼容。
 */
@Slf4j
@Component
@MethodMapping("reports.cash.cash-cashier-settlement")
public class CashCashierSettlementHandler implements ReportHandler<CashCashierSettlementRequest, Object> {

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
    public ApiResponse<Object> handle(ApiRequest<Object> request) {
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

        String endpoint = request.getHead() == null ? null : request.getHead().getEndpoint();
        log.info("[{}] 子接口 endpoint={}", MODULE.getChineseName(), endpoint);
        String msg = MODULE.getChineseName() + "查询成功！";

        if ("overview".equals(endpoint)) {
            return ApiResponse.success(cashCashierSettlementService.queryOverview(body), msg);
        }
        if ("chart".equals(endpoint)) {
            return ApiResponse.success(cashCashierSettlementService.queryChart(body), msg);
        }

        Integer page = body.getPage() != null ? body.getPage() : pageConfig.getDefaultPage();
        Integer pageSize = body.getPageSize() != null ? body.getPageSize() : pageConfig.getDefaultPageSize();
        if ("table".equals(endpoint)) {
            return ApiResponse.success(cashCashierSettlementService.queryTable(body, page, pageSize), msg);
        }

        // 未指定子接口：返回合并对象
        OverviewData overview = cashCashierSettlementService.queryOverview(body);
        PageResult<TableItem> table = cashCashierSettlementService.queryTable(body, page, pageSize);
        ChartData chart = cashCashierSettlementService.queryChart(body);

        CashCashierSettlementResponse response = new CashCashierSettlementResponse();
        response.setOverview(overview);
        response.setTable(table);
        response.setChart(chart);

        return ApiResponse.success(response, msg);
    }

}