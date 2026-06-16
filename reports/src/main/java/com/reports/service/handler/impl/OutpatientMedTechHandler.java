package com.reports.service.handler.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reports.config.PageConfig;
import com.reports.dto.common.ApiRequest;
import com.reports.dto.common.ApiResponse;
import com.reports.dto.common.PageResult;
import com.reports.dto.request.OutpatientMedTechRequest;
import com.reports.dto.response.outpatient.med.tech.*;
import com.reports.service.OutpatientMedTechService;
import com.reports.enums.ReportModule;
import com.reports.service.handler.MethodMapping;
import com.reports.service.handler.ReportHandler;
import com.reports.util.SeqUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 医技统计处理器
 */
@Slf4j
@Component
@MethodMapping("reports.outp.outpatient-med-tech")
public class OutpatientMedTechHandler implements ReportHandler<OutpatientMedTechRequest, OutpatientMedTechResponse> {

    private final OutpatientMedTechService outpatientMedTechService;
    private final ObjectMapper objectMapper;
    private final PageConfig pageConfig;

    @Autowired
    public OutpatientMedTechHandler(OutpatientMedTechService outpatientMedTechService, ObjectMapper objectMapper, PageConfig pageConfig) {
        this.outpatientMedTechService = outpatientMedTechService;
        this.objectMapper = objectMapper;
        this.pageConfig = pageConfig;
    }

    private static final ReportModule MODULE = ReportModule.OUTPATIENT_MED_TECH;

    @Override
    public ApiResponse<OutpatientMedTechResponse> handle(ApiRequest<Object> request) {
        SeqUtil.next();
        log.info("[{}] 处理请求", MODULE.getChineseName());

        OutpatientMedTechRequest body;
        if (request.getBody() instanceof OutpatientMedTechRequest) {
            body = (OutpatientMedTechRequest) request.getBody();
        } else {
            body = objectMapper.convertValue(request.getBody(), OutpatientMedTechRequest.class);
        }
        if (body == null) {
            body = new OutpatientMedTechRequest();
        }

        OverviewData overview = outpatientMedTechService.queryOverview(body);

        Integer page = body.getPage() != null ? body.getPage() : pageConfig.getDefaultPage();
        Integer pageSize = body.getPageSize() != null ? body.getPageSize() : pageConfig.getDefaultPageSize();
        PageResult<TableItem> table = outpatientMedTechService.queryTable(body, page, pageSize);

        OutpatientMedTechResponse response = new OutpatientMedTechResponse();
        response.setOverview(overview);
        response.setTable(table);

        return ApiResponse.success(response, MODULE.getChineseName() + "查询成功！");
    }

}