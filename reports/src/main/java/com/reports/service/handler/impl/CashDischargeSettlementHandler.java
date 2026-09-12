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
 * 出院结算报表处理器。
 *
 * <p>页面按子接口分别取数（overview / charts / table），读的是各自那一层的结构：
 * overview 是扁平指标、charts 是三个分析数组、table 是分页对象。
 * 所以这里按 {@code RequestHead.endpoint} 返回对应结构；
 * 没传 endpoint 时（旧调用）仍返回合并后的完整对象，保持兼容。
 */
@Slf4j
@Component
@MethodMapping("reports.cash.cash-discharge-settlement")
public class CashDischargeSettlementHandler implements ReportHandler<CashDischargeSettlementRequest, Object> {

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
    public ApiResponse<Object> handle(ApiRequest<Object> request) {
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

        String endpoint = request.getHead() == null ? null : request.getHead().getEndpoint();
        log.info("[{}] 子接口 endpoint={}", MODULE.getChineseName(), endpoint);
        String msg = MODULE.getChineseName() + "查询成功！";

        if ("overview".equals(endpoint)) {
            return ApiResponse.success(cashDischargeSettlementService.queryOverview(body), msg);
        }
        if ("charts".equals(endpoint)) {
            return ApiResponse.success(cashDischargeSettlementService.queryCharts(body), msg);
        }

        Integer page = body.getPage() != null ? body.getPage() : pageConfig.getDefaultPage();
        Integer pageSize = body.getPageSize() != null ? body.getPageSize() : pageConfig.getDefaultPageSize();
        if ("table".equals(endpoint)) {
            return ApiResponse.success(cashDischargeSettlementService.queryTable(body, page, pageSize), msg);
        }

        // 未指定子接口：返回合并对象
        OverviewData overview = cashDischargeSettlementService.queryOverview(body);
        ChartsData charts = cashDischargeSettlementService.queryCharts(body);
        PageResult<TableItem> table = cashDischargeSettlementService.queryTable(body, page, pageSize);

        CashDischargeSettlementResponse response = new CashDischargeSettlementResponse();
        response.setOverview(overview);
        response.setCharts(charts);
        response.setTable(table);

        return ApiResponse.success(response, msg);
    }

}
