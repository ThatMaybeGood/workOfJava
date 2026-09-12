package com.reports.service.handler.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reports.config.PageConfig;
import com.reports.dto.common.ApiRequest;
import com.reports.dto.common.ApiResponse;
import com.reports.dto.request.TreatmentStatsRequest;
import com.reports.service.TreatmentStatsService;
import com.reports.service.handler.MethodMapping;
import com.reports.service.handler.ReportHandler;
import com.reports.util.SeqUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 治疗统计报表处理器。
 *
 * <p>页面一次请求拿全部四块数据（概览 / 趋势 / TOP 项目 / 科室明细），
 * 所以不需要按 endpoint 分发，直接返回嵌套结构。
 */
@Slf4j
@Component
@MethodMapping("reports.outp.outpatient-treatment-stats")
public class TreatmentStatsHandler implements ReportHandler<TreatmentStatsRequest, Map<String, Object>> {

    private static final String MODULE_NAME = "治疗统计报表";

    private final TreatmentStatsService treatmentStatsService;
    private final ObjectMapper objectMapper;
    private final PageConfig pageConfig;

    @Autowired
    public TreatmentStatsHandler(TreatmentStatsService treatmentStatsService,
                                 ObjectMapper objectMapper, PageConfig pageConfig) {
        this.treatmentStatsService = treatmentStatsService;
        this.objectMapper = objectMapper;
        this.pageConfig = pageConfig;
    }

    @Override
    public ApiResponse<Map<String, Object>> handle(ApiRequest<Object> request) {
        SeqUtil.next();
        log.info("[{}] 处理请求", MODULE_NAME);

        TreatmentStatsRequest body;
        if (request.getBody() instanceof TreatmentStatsRequest) {
            body = (TreatmentStatsRequest) request.getBody();
        } else {
            body = objectMapper.convertValue(request.getBody(), TreatmentStatsRequest.class);
        }
        if (body == null) {
            body = new TreatmentStatsRequest();
        }

        Integer page = body.getPage() != null ? body.getPage() : pageConfig.getDefaultPage();
        Integer pageSize = body.getPageSize() != null ? body.getPageSize() : pageConfig.getDefaultPageSize();

        Map<String, Object> result = treatmentStatsService.queryStats(body, page, pageSize);
        return ApiResponse.success(result, MODULE_NAME + "查询成功！");
    }
}
