package com.reports.service.handler.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reports.config.PageConfig;
import com.reports.dto.common.ApiRequest;
import com.reports.dto.common.ApiResponse;
import com.reports.dto.common.PageResult;
import com.reports.dto.request.OutpatientSpecialtyTreatmentRequest;
import com.reports.dto.response.outpatient.specialty.treatment.*;
import com.reports.service.OutpatientSpecialtyTreatmentService;
import com.reports.enums.ReportModule;
import com.reports.service.handler.MethodMapping;
import com.reports.service.handler.ReportHandler;
import com.reports.util.SeqUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 专科治疗量统计处理器
 */
@Slf4j
@Component
@MethodMapping("reports.outp.outpatient-specialty-treatment")
public class OutpatientSpecialtyTreatmentHandler implements ReportHandler<OutpatientSpecialtyTreatmentRequest, OutpatientSpecialtyTreatmentResponse> {

    private final OutpatientSpecialtyTreatmentService outpatientSpecialtyTreatmentService;
    private final ObjectMapper objectMapper;
    private final PageConfig pageConfig;

    @Autowired
    public OutpatientSpecialtyTreatmentHandler(OutpatientSpecialtyTreatmentService outpatientSpecialtyTreatmentService, ObjectMapper objectMapper, PageConfig pageConfig) {
        this.outpatientSpecialtyTreatmentService = outpatientSpecialtyTreatmentService;
        this.objectMapper = objectMapper;
        this.pageConfig = pageConfig;
    }

    private static final ReportModule MODULE = ReportModule.OUTPATIENT_SPECIALTY_TREATMENT;

    @Override
    public ApiResponse<OutpatientSpecialtyTreatmentResponse> handle(ApiRequest<Object> request) {
        SeqUtil.next();
        log.info("[{}] 处理请求", MODULE.getChineseName());

        OutpatientSpecialtyTreatmentRequest body;
        if (request.getBody() instanceof OutpatientSpecialtyTreatmentRequest) {
            body = (OutpatientSpecialtyTreatmentRequest) request.getBody();
        } else {
            body = objectMapper.convertValue(request.getBody(), OutpatientSpecialtyTreatmentRequest.class);
        }
        if (body == null) {
            body = new OutpatientSpecialtyTreatmentRequest();
        }

        OverviewData overview = outpatientSpecialtyTreatmentService.queryOverview(body);

        Integer page = body.getPage() != null ? body.getPage() : pageConfig.getDefaultPage();
        Integer pageSize = body.getPageSize() != null ? body.getPageSize() : pageConfig.getDefaultPageSize();
        PageResult<TableItem> table = outpatientSpecialtyTreatmentService.queryTable(body, page, pageSize);

        OutpatientSpecialtyTreatmentResponse response = new OutpatientSpecialtyTreatmentResponse();
        response.setOverview(overview);
        response.setTable(table);

        return ApiResponse.success(response, MODULE.getChineseName() + "查询成功！");
    }

}