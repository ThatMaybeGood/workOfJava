package com.reports.service.handler.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reports.config.PageConfig;
import com.reports.dto.common.ApiRequest;
import com.reports.dto.common.ApiResponse;
import com.reports.dto.common.PageResult;
import com.reports.dto.request.OutpatientAlertRequest;
import com.reports.dto.response.outpatient.alert.*;
import com.reports.service.OutpatientAlertService;
import com.reports.enums.ReportModule;
import com.reports.service.handler.MethodMapping;
import com.reports.service.handler.ReportHandler;
import com.reports.util.SeqUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 门诊预警统计处理器
 */
@Slf4j
@Component
@MethodMapping("reports.outp.outpatient-alert")
public class OutpatientAlertHandler implements ReportHandler<OutpatientAlertRequest, OutpatientAlertResponse> {

    private final OutpatientAlertService outpatientAlertService;
    private final ObjectMapper objectMapper;
    private final PageConfig pageConfig;

    @Autowired
    public OutpatientAlertHandler(OutpatientAlertService outpatientAlertService, ObjectMapper objectMapper, PageConfig pageConfig) {
        this.outpatientAlertService = outpatientAlertService;
        this.objectMapper = objectMapper;
        this.pageConfig = pageConfig;
    }

    private static final ReportModule MODULE = ReportModule.OUTPATIENT_ALERT;

    @Override
    public ApiResponse<OutpatientAlertResponse> handle(ApiRequest<Object> request) {
        SeqUtil.next();
        log.info("[{}] 处理请求", MODULE.getChineseName());

        OutpatientAlertRequest body;
        if (request.getBody() instanceof OutpatientAlertRequest) {
            body = (OutpatientAlertRequest) request.getBody();
        } else {
            body = objectMapper.convertValue(request.getBody(), OutpatientAlertRequest.class);
        }
        if (body == null) {
            body = new OutpatientAlertRequest();
        }

        OverviewData overview = outpatientAlertService.queryOverview(body);

        Integer page = body.getPage() != null ? body.getPage() : pageConfig.getDefaultPage();
        Integer pageSize = body.getPageSize() != null ? body.getPageSize() : pageConfig.getDefaultPageSize();
        PageResult<DeptTableItem> deptTable = outpatientAlertService.queryDeptTable(body, page, pageSize);
        PageResult<DoctorTableItem> doctorTable = outpatientAlertService.queryDoctorTable(body, page, pageSize);

        OutpatientAlertResponse response = new OutpatientAlertResponse();
        response.setOverview(overview);
        response.setDeptTable(deptTable);
        response.setDoctorTable(doctorTable);

        return ApiResponse.success(response, MODULE.getChineseName() + "查询成功！");
    }

}