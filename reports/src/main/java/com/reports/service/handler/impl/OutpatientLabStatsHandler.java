package com.reports.service.handler.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reports.config.PageConfig;
import com.reports.dto.common.ApiRequest;
import com.reports.dto.common.ApiResponse;
import com.reports.dto.request.OutpatientLabStatsRequest;
import com.reports.dto.response.outpatient.lab.stats.*;
import com.reports.service.OutpatientLabStatsService;
import com.reports.enums.ReportModule;
import com.reports.service.handler.MethodMapping;
import com.reports.service.handler.ReportHandler;
import com.reports.util.SeqUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 检验统计处理器
 */
@Slf4j
@Component
@MethodMapping("reports.outp.outpatient-lab-stats")
public class OutpatientLabStatsHandler implements ReportHandler<OutpatientLabStatsRequest, OutpatientLabStatsResponse> {

    private final OutpatientLabStatsService outpatientLabStatsService;
    private final ObjectMapper objectMapper;
    private final PageConfig pageConfig;

    @Autowired
    public OutpatientLabStatsHandler(OutpatientLabStatsService outpatientLabStatsService, ObjectMapper objectMapper, PageConfig pageConfig) {
        this.outpatientLabStatsService = outpatientLabStatsService;
        this.objectMapper = objectMapper;
        this.pageConfig = pageConfig;
    }

    private static final ReportModule MODULE = ReportModule.OUTPATIENT_LAB_STATS;

    @Override
    public ApiResponse<OutpatientLabStatsResponse> handle(ApiRequest<Object> request) {
        SeqUtil.next();
        log.info("[{}] 处理请求", MODULE.getChineseName());

        OutpatientLabStatsRequest body;
        if (request.getBody() instanceof OutpatientLabStatsRequest) {
            body = (OutpatientLabStatsRequest) request.getBody();
        } else {
            body = objectMapper.convertValue(request.getBody(), OutpatientLabStatsRequest.class);
        }
        if (body == null) {
            body = new OutpatientLabStatsRequest();
        }

        OverviewData overview = outpatientLabStatsService.queryOverview(body);

        OutpatientLabStatsResponse response = new OutpatientLabStatsResponse();
        response.setOverview(overview);

        return ApiResponse.success(response, MODULE.getChineseName() + "查询成功！");
    }

}