package com.reports.service.handler.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reports.config.PageConfig;
import com.reports.dto.common.ApiRequest;
import com.reports.dto.common.ApiResponse;
import com.reports.dto.common.PageResult;
import com.reports.dto.request.OutpatientInternetHospitalRequest;
import com.reports.dto.response.outpatient.internet.hospital.*;
import com.reports.service.OutpatientInternetHospitalService;
import com.reports.enums.ReportModule;
import com.reports.service.handler.MethodMapping;
import com.reports.service.handler.ReportHandler;
import com.reports.util.SeqUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * 互医质控运营月报处理器
 */
@Slf4j
@Component
@MethodMapping("reports.outp.outpatient-internet-hospital")
public class OutpatientInternetHospitalHandler implements ReportHandler<OutpatientInternetHospitalRequest, OutpatientInternetHospitalResponse> {

    private final OutpatientInternetHospitalService outpatientInternetHospitalService;
    private final ObjectMapper objectMapper;
    private final PageConfig pageConfig;

    @Autowired
    public OutpatientInternetHospitalHandler(OutpatientInternetHospitalService outpatientInternetHospitalService, ObjectMapper objectMapper, PageConfig pageConfig) {
        this.outpatientInternetHospitalService = outpatientInternetHospitalService;
        this.objectMapper = objectMapper;
        this.pageConfig = pageConfig;
    }

    private static final ReportModule MODULE = ReportModule.OUTPATIENT_INTERNET_HOSPITAL;

    @Override
    public ApiResponse<OutpatientInternetHospitalResponse> handle(ApiRequest<Object> request) {
        SeqUtil.next();
        log.info("[{}] 处理请求", MODULE.getChineseName());

        OutpatientInternetHospitalRequest body;
        if (request.getBody() instanceof OutpatientInternetHospitalRequest) {
            body = (OutpatientInternetHospitalRequest) request.getBody();
        } else {
            body = objectMapper.convertValue(request.getBody(), OutpatientInternetHospitalRequest.class);
        }
        if (body == null) {
            body = new OutpatientInternetHospitalRequest();
        }
        // 月份为空时兜底成当月：mapper 里是 stat_month = #{statMonth}，
        // 传空会变成 stat_month = null 直接查不到任何数据（页面不传月份时就是这种情况）
        if (body.getMonth() == null || body.getMonth().trim().isEmpty()) {
            body.setMonth(new SimpleDateFormat("yyyy-MM").format(new Date()));
        }

        OverviewData overview = outpatientInternetHospitalService.queryOverview(body);

        List<OperationTableItem> operationTable = outpatientInternetHospitalService.queryOperationTable(body);
        BusinessChart businessChart = outpatientInternetHospitalService.queryBusinessChart(body);

        Integer deptPage = body.getDeptPage() != null ? body.getDeptPage() : pageConfig.getDefaultPage();
        Integer deptPageSize = body.getDeptPageSize() != null ? body.getDeptPageSize() : pageConfig.getDefaultPageSize();
        PageResult<DeptRankingItem> deptRanking = outpatientInternetHospitalService.queryDeptRanking(body, deptPage, deptPageSize);

        Integer doctorPage = body.getDoctorPage() != null ? body.getDoctorPage() : pageConfig.getDefaultPage();
        Integer doctorPageSize = body.getDoctorPageSize() != null ? body.getDoctorPageSize() : pageConfig.getDefaultPageSize();
        PageResult<DoctorRankingItem> doctorRanking = outpatientInternetHospitalService.queryDoctorRanking(body, doctorPage, doctorPageSize);

        GrowthChart growthChart = outpatientInternetHospitalService.queryGrowthChart(body);

        OutpatientInternetHospitalResponse response = new OutpatientInternetHospitalResponse();
        response.setOverview(overview);
        response.setOperationTable(operationTable);
        response.setBusinessChart(businessChart);
        response.setDeptRanking(deptRanking);
        response.setDoctorRanking(doctorRanking);
        response.setGrowthChart(growthChart);

        return ApiResponse.success(response, MODULE.getChineseName() + "查询成功！");
    }

}