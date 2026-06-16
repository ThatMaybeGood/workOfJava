package com.reports.service.handler.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reports.config.PageConfig;
import com.reports.dto.common.ApiRequest;
import com.reports.dto.common.ApiResponse;
import com.reports.dto.request.OutpatientPatientPortraitRequest;
import com.reports.dto.response.outpatient.patient.portrait.*;
import com.reports.service.OutpatientPatientPortraitService;
import com.reports.enums.ReportModule;
import com.reports.service.handler.MethodMapping;
import com.reports.service.handler.ReportHandler;
import com.reports.util.SeqUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 患者画像处理器
 */
@Slf4j
@Component
@MethodMapping("reports.outp.outpatient-patient-portrait")
public class OutpatientPatientPortraitHandler implements ReportHandler<OutpatientPatientPortraitRequest, OutpatientPatientPortraitResponse> {

    private final OutpatientPatientPortraitService outpatientPatientPortraitService;
    private final ObjectMapper objectMapper;
    private final PageConfig pageConfig;

    @Autowired
    public OutpatientPatientPortraitHandler(OutpatientPatientPortraitService outpatientPatientPortraitService, ObjectMapper objectMapper, PageConfig pageConfig) {
        this.outpatientPatientPortraitService = outpatientPatientPortraitService;
        this.objectMapper = objectMapper;
        this.pageConfig = pageConfig;
    }

    private static final ReportModule MODULE = ReportModule.OUTPATIENT_PATIENT_PORTRAIT;

    @Override
    public ApiResponse<OutpatientPatientPortraitResponse> handle(ApiRequest<Object> request) {
        SeqUtil.next();
        log.info("[{}] 处理请求", MODULE.getChineseName());

        OutpatientPatientPortraitRequest body;
        if (request.getBody() instanceof OutpatientPatientPortraitRequest) {
            body = (OutpatientPatientPortraitRequest) request.getBody();
        } else {
            body = objectMapper.convertValue(request.getBody(), OutpatientPatientPortraitRequest.class);
        }
        if (body == null) {
            body = new OutpatientPatientPortraitRequest();
        }

        AgeAnalysis ageAnalysis = outpatientPatientPortraitService.queryAgeAnalysis(body);
        List<AnalysisItem> insuranceAnalysis = outpatientPatientPortraitService.queryInsuranceAnalysis(body);
        List<AnalysisItem> identityAnalysis = outpatientPatientPortraitService.queryIdentityAnalysis(body);
        List<AnalysisItem> registerOriginAnalysis = outpatientPatientPortraitService.queryRegisterOriginAnalysis(body);
        List<AnalysisItem> archiveOriginAnalysis = outpatientPatientPortraitService.queryArchiveOriginAnalysis(body);

        OutpatientPatientPortraitResponse response = new OutpatientPatientPortraitResponse();
        response.setAgeAnalysis(ageAnalysis);
        response.setInsuranceAnalysis(insuranceAnalysis);
        response.setIdentityAnalysis(identityAnalysis);
        response.setRegisterOriginAnalysis(registerOriginAnalysis);
        response.setArchiveOriginAnalysis(archiveOriginAnalysis);

        return ApiResponse.success(response, MODULE.getChineseName() + "查询成功！");
    }

}