package com.reports.service.handler.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reports.config.PageConfig;
import com.reports.dto.common.ApiRequest;
import com.reports.dto.common.ApiResponse;
import com.reports.dto.common.PageResult;
import com.reports.dto.request.OutpatientRevenueRequest;
import com.reports.dto.response.outpatient.revenue.*;
import com.reports.service.OutpatientRevenueService;
import com.reports.enums.ReportModule;
import com.reports.service.handler.MethodMapping;
import com.reports.service.handler.ReportHandler;
import com.reports.util.SeqUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 门诊收入分析处理器
 */
@Slf4j
@Component
@MethodMapping("reports.outp.outpatient-revenue")
public class OutpatientRevenueHandler implements ReportHandler<OutpatientRevenueRequest, OutpatientRevenueResponse> {

    private final OutpatientRevenueService outpatientRevenueService;
    private final ObjectMapper objectMapper;
    private final PageConfig pageConfig;

    @Autowired
    public OutpatientRevenueHandler(OutpatientRevenueService outpatientRevenueService, ObjectMapper objectMapper, PageConfig pageConfig) {
        this.outpatientRevenueService = outpatientRevenueService;
        this.objectMapper = objectMapper;
        this.pageConfig = pageConfig;
    }

    private static final ReportModule MODULE = ReportModule.OUTPATIENT_REVENUE;

    @Override
    public ApiResponse<OutpatientRevenueResponse> handle(ApiRequest<Object> request) {
        SeqUtil.next();
        log.info("[{}] 处理请求", MODULE.getChineseName());

        OutpatientRevenueRequest body;
        if (request.getBody() instanceof OutpatientRevenueRequest) {
            body = (OutpatientRevenueRequest) request.getBody();
        } else {
            body = objectMapper.convertValue(request.getBody(), OutpatientRevenueRequest.class);
        }
        if (body == null) {
            body = new OutpatientRevenueRequest();
        }

        OverviewData overview = outpatientRevenueService.queryOverview(body);

        Integer deptPage = body.getDeptPage() != null ? body.getDeptPage() : pageConfig.getDefaultPage();
        Integer deptPageSize = body.getDeptPageSize() != null ? body.getDeptPageSize() : pageConfig.getDefaultPageSize();
        Integer doctorPage = body.getDoctorPage() != null ? body.getDoctorPage() : deptPage;
        Integer doctorPageSize = body.getDoctorPageSize() != null ? body.getDoctorPageSize() : deptPageSize;
        PageResult<DeptTableItem> deptTable = outpatientRevenueService.queryDeptTable(body, deptPage, deptPageSize);
        PageResult<DoctorTableItem> doctorTable = outpatientRevenueService.queryDoctorTable(body, doctorPage, doctorPageSize);

        OutpatientRevenueResponse response = new OutpatientRevenueResponse();
        response.setOverview(overview);
        response.setDeptTable(deptTable);
        response.setDoctorTable(doctorTable);

        return ApiResponse.success(response, MODULE.getChineseName() + "查询成功！");
    }

}