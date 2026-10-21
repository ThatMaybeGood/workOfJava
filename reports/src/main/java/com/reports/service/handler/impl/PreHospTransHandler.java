package com.reports.service.handler.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reports.dto.common.ApiRequest;
import com.reports.dto.common.ApiResponse;
import com.reports.dto.request.PreHospTransRequest;
import com.reports.dto.response.cash.prehosp.PreHospTransItem;
import com.reports.enums.ReportModule;
import com.reports.service.PreHospTransService;
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
 * 院前转住院统计处理器
 * method: reports.cash.pre-hosp-trans
 */
@Slf4j
@Component
@MethodMapping("reports.cash.pre-hosp-trans")
public class PreHospTransHandler implements ReportHandler<PreHospTransRequest, Map<String, Object>> {

    private final PreHospTransService preHospTransService;
    private final ObjectMapper objectMapper;

    @Autowired
    public PreHospTransHandler(PreHospTransService preHospTransService, ObjectMapper objectMapper) {
        this.preHospTransService = preHospTransService;
        this.objectMapper = objectMapper;
    }

    private static final ReportModule MODULE = ReportModule.PRE_HOSP_TRANS;

    @Override
    public ApiResponse<Map<String, Object>> handle(ApiRequest<Object> request) {
        SeqUtil.next();
        log.info("[{}] 处理请求", MODULE.getChineseName());

        PreHospTransRequest body;
        if (request.getBody() instanceof PreHospTransRequest) {
            body = (PreHospTransRequest) request.getBody();
        } else {
            body = objectMapper.convertValue(request.getBody(), PreHospTransRequest.class);
        }
        if (body == null) {
            body = new PreHospTransRequest();
        }

        List<PreHospTransItem> list = preHospTransService.queryStats(body);
        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", list.size());
        return ApiResponse.success(result, "院前转住院统计查询成功！");
    }
}
