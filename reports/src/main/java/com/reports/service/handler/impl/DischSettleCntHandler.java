package com.reports.service.handler.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reports.dto.common.ApiRequest;
import com.reports.dto.common.ApiResponse;
import com.reports.dto.request.DischSettleCntRequest;
import com.reports.dto.response.cash.discharge.settlement.PersonCountItem;
import com.reports.enums.ReportModule;
import com.reports.service.DischSettleCntService;
import com.reports.service.handler.MethodMapping;
import com.reports.service.handler.ReportHandler;
import com.reports.util.SeqUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 出院结算人次统计处理器
 * method: reports.cash.disch-settle-cnt
 */
@Slf4j
@Component
@MethodMapping("reports.cash.disch-settle-cnt")
public class DischSettleCntHandler implements ReportHandler<DischSettleCntRequest, Map<String, Object>> {

    private final DischSettleCntService dischSettleCntService;
    private final ObjectMapper objectMapper;

    @Autowired
    public DischSettleCntHandler(DischSettleCntService dischSettleCntService, ObjectMapper objectMapper) {
        this.dischSettleCntService = dischSettleCntService;
        this.objectMapper = objectMapper;
    }

    private static final ReportModule MODULE = ReportModule.CASH_DISCH_SETTLE_CNT;

    @Override
    public ApiResponse<Map<String, Object>> handle(ApiRequest<Object> request) {
        SeqUtil.next();
        log.info("[{}] 处理请求", MODULE.getChineseName());

        DischSettleCntRequest body;
        if (request.getBody() instanceof DischSettleCntRequest) {
            body = (DischSettleCntRequest) request.getBody();
        } else {
            body = objectMapper.convertValue(request.getBody(), DischSettleCntRequest.class);
        }
        if (body == null) {
            body = new DischSettleCntRequest();
        }

        List<PersonCountItem> list = dischSettleCntService.queryPersonCount(body);
        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", list.size());
        return ApiResponse.success(result, "出院结算人次统计查询成功！");
    }
}
