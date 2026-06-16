package com.reports.service.handler.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reports.config.PageConfig;
import com.reports.dto.common.ApiRequest;
import com.reports.dto.common.ApiResponse;
import com.reports.dto.request.OutpatientWindowStatsRequest;
import com.reports.dto.response.outpatient.window.stats.*;
import com.reports.service.OutpatientWindowStatsService;
import com.reports.enums.ReportModule;
import com.reports.service.handler.MethodMapping;
import com.reports.service.handler.ReportHandler;
import com.reports.util.SeqUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 人工窗口统计处理器
 */
@Slf4j
@Component
@MethodMapping("reports.outp.outpatient-window-stats")
public class OutpatientWindowStatsHandler implements ReportHandler<OutpatientWindowStatsRequest, OutpatientWindowStatsResponse> {

    private final OutpatientWindowStatsService outpatientWindowStatsService;
    private final ObjectMapper objectMapper;
    private final PageConfig pageConfig;

    @Autowired
    public OutpatientWindowStatsHandler(OutpatientWindowStatsService outpatientWindowStatsService, ObjectMapper objectMapper, PageConfig pageConfig) {
        this.outpatientWindowStatsService = outpatientWindowStatsService;
        this.objectMapper = objectMapper;
        this.pageConfig = pageConfig;
    }

    private static final ReportModule MODULE = ReportModule.OUTPATIENT_WINDOW_STATS;

    @Override
    public ApiResponse<OutpatientWindowStatsResponse> handle(ApiRequest<Object> request) {
        SeqUtil.next();
        log.info("[{}] 处理请求", MODULE.getChineseName());

        OutpatientWindowStatsRequest body;
        if (request.getBody() instanceof OutpatientWindowStatsRequest) {
            body = (OutpatientWindowStatsRequest) request.getBody();
        } else {
            body = objectMapper.convertValue(request.getBody(), OutpatientWindowStatsRequest.class);
        }
        if (body == null) {
            body = new OutpatientWindowStatsRequest();
        }

        OverviewData overview = outpatientWindowStatsService.queryOverview(body);
        AgeAnalysis ageAnalysis = outpatientWindowStatsService.queryAgeAnalysis(body);
        TimeAnalysis timeAnalysis = outpatientWindowStatsService.queryTimeAnalysis(body);
        List<AnalysisItem> sourceAnalysis = outpatientWindowStatsService.querySourceAnalysis(body);
        WorkloadTable workloadTable = outpatientWindowStatsService.queryWorkloadTable(body);

        OutpatientWindowStatsResponse response = new OutpatientWindowStatsResponse();
        response.setOverview(overview);
        response.setOriginAnalysis(sourceAnalysis);
        response.setAgeAnalysis(ageAnalysis);
        response.setTimeAnalysis(timeAnalysis);
        response.setWorkloadTable(workloadTable);

        return ApiResponse.success(response, MODULE.getChineseName() + "查询成功！");
    }

}