package com.reports.service.handler.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reports.config.PageConfig;
import com.reports.dto.common.ApiRequest;
import com.reports.dto.common.ApiResponse;
import com.reports.dto.common.PageResult;
import com.reports.dto.request.OutpatientQualityControlRequest;
import com.reports.dto.response.outpatient.quality.control.*;
import com.reports.service.OutpatientQualityControlService;
import com.reports.enums.ReportModule;
import com.reports.service.handler.MethodMapping;
import com.reports.service.handler.ReportHandler;
import com.reports.util.SeqUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 门诊管理质量控制处理器
 */
@Slf4j
@Component
@MethodMapping("reports.outp.outpatient-quality-control")
public class OutpatientQualityControlHandler implements ReportHandler<OutpatientQualityControlRequest, OutpatientQualityControlResponse> {

    private final OutpatientQualityControlService outpatientQualityControlService;
    private final ObjectMapper objectMapper;
    private final PageConfig pageConfig;

    @Autowired
    public OutpatientQualityControlHandler(OutpatientQualityControlService outpatientQualityControlService, ObjectMapper objectMapper, PageConfig pageConfig) {
        this.outpatientQualityControlService = outpatientQualityControlService;
        this.objectMapper = objectMapper;
        this.pageConfig = pageConfig;
    }

    private static final ReportModule MODULE = ReportModule.OUTPATIENT_QUALITY_CONTROL;

    @Override
    public ApiResponse<OutpatientQualityControlResponse> handle(ApiRequest<Object> request) {
        SeqUtil.next();
        log.info("[{}] 处理请求", MODULE.getChineseName());

        OutpatientQualityControlRequest body;
        if (request.getBody() instanceof OutpatientQualityControlRequest) {
            body = (OutpatientQualityControlRequest) request.getBody();
        } else {
            body = objectMapper.convertValue(request.getBody(), OutpatientQualityControlRequest.class);
        }
        if (body == null) {
            body = new OutpatientQualityControlRequest();
        }

        OverviewData overview = outpatientQualityControlService.queryOverview(body);

        Integer page = body.getPage() != null ? body.getPage() : pageConfig.getDefaultPage();
        Integer pageSize = body.getPageSize() != null ? body.getPageSize() : pageConfig.getDefaultPageSize();
        PageResult<TableItem> table = outpatientQualityControlService.queryTable(body, page, pageSize);

        OutpatientQualityControlResponse response = new OutpatientQualityControlResponse();
        response.setOverview(overview);
        response.setTable(table);

        return ApiResponse.success(response, MODULE.getChineseName() + "查询成功！");
    }

}