package com.reports.service.handler.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reports.config.PageConfig;
import com.reports.dto.common.ApiRequest;
import com.reports.dto.common.ApiResponse;
import com.reports.dto.common.PageResult;
import com.reports.dto.request.OutpatientServiceQualityRequest;
import com.reports.dto.response.outpatient.service.quality.*;
import com.reports.service.OutpatientServiceQualityService;
import com.reports.enums.ReportModule;
import com.reports.service.handler.MethodMapping;
import com.reports.service.handler.ReportHandler;
import com.reports.util.SeqUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 门诊服务质量分析处理器
 */
@Slf4j
@Component
@MethodMapping("reports.outp.outpatient-service-quality")
public class OutpatientServiceQualityHandler implements ReportHandler<OutpatientServiceQualityRequest, OutpatientServiceQualityResponse> {

    private final OutpatientServiceQualityService outpatientServiceQualityService;
    private final ObjectMapper objectMapper;
    private final PageConfig pageConfig;

    @Autowired
    public OutpatientServiceQualityHandler(OutpatientServiceQualityService outpatientServiceQualityService, ObjectMapper objectMapper, PageConfig pageConfig) {
        this.outpatientServiceQualityService = outpatientServiceQualityService;
        this.objectMapper = objectMapper;
        this.pageConfig = pageConfig;
    }

    private static final ReportModule MODULE = ReportModule.OUTPATIENT_SERVICE_QUALITY;

    @Override
    public ApiResponse<OutpatientServiceQualityResponse> handle(ApiRequest<Object> request) {
        SeqUtil.next();
        log.info("[{}] 处理请求", MODULE.getChineseName());

        OutpatientServiceQualityRequest body;
        if (request.getBody() instanceof OutpatientServiceQualityRequest) {
            body = (OutpatientServiceQualityRequest) request.getBody();
        } else {
            body = objectMapper.convertValue(request.getBody(), OutpatientServiceQualityRequest.class);
        }
        if (body == null) {
            body = new OutpatientServiceQualityRequest();
        }

        OverviewData overview = outpatientServiceQualityService.queryOverview(body);

        Integer page = body.getPage() != null ? body.getPage() : pageConfig.getDefaultPage();
        Integer pageSize = body.getPageSize() != null ? body.getPageSize() : pageConfig.getDefaultPageSize();
        PageResult<ComplaintItem> complaintList = outpatientServiceQualityService.queryComplaintList(body, page, pageSize);
        PageResult<PraiseItem> praiseList = outpatientServiceQualityService.queryPraiseList(body, page, pageSize);

        OutpatientServiceQualityResponse response = new OutpatientServiceQualityResponse();
        response.setOverview(overview);
        response.setComplaint(complaintList);
        response.setPraise(praiseList);

        return ApiResponse.success(response, MODULE.getChineseName() + "查询成功！");
    }

}