package com.reports.service.handler.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reports.config.PageConfig;
import com.reports.dto.common.ApiRequest;
import com.reports.dto.common.ApiResponse;
import com.reports.dto.common.PageResult;
import com.reports.dto.request.OutpatientNoShowRequest;
import com.reports.dto.response.outpatient.no.show.*;
import com.reports.service.OutpatientNoShowService;
import com.reports.enums.ReportModule;
import com.reports.service.handler.MethodMapping;
import com.reports.service.handler.ReportHandler;
import com.reports.util.SeqUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 爽约退号分析处理器
 */
@Slf4j
@Component
@MethodMapping("reports.outp.outpatient-no-show")
public class OutpatientNoShowHandler implements ReportHandler<OutpatientNoShowRequest, OutpatientNoShowResponse> {

    private final OutpatientNoShowService outpatientNoShowService;
    private final ObjectMapper objectMapper;
    private final PageConfig pageConfig;

    @Autowired
    public OutpatientNoShowHandler(OutpatientNoShowService outpatientNoShowService, ObjectMapper objectMapper, PageConfig pageConfig) {
        this.outpatientNoShowService = outpatientNoShowService;
        this.objectMapper = objectMapper;
        this.pageConfig = pageConfig;
    }

    private static final ReportModule MODULE = ReportModule.OUTPATIENT_NO_SHOW;

    @Override
    public ApiResponse<OutpatientNoShowResponse> handle(ApiRequest<Object> request) {
        SeqUtil.next();
        log.info("[{}] 处理请求", MODULE.getChineseName());

        OutpatientNoShowRequest body;
        if (request.getBody() instanceof OutpatientNoShowRequest) {
            body = (OutpatientNoShowRequest) request.getBody();
        } else {
            body = objectMapper.convertValue(request.getBody(), OutpatientNoShowRequest.class);
        }
        if (body == null) {
            body = new OutpatientNoShowRequest();
        }

        OverviewData overview = outpatientNoShowService.queryOverview(body);
        List<AnalysisItem> refundOrigin = outpatientNoShowService.queryRefundOrigin(body);
        List<AnalysisItem> refundChannel = outpatientNoShowService.queryRefundChannel(body);
        AgeAnalysis ageAnalysis = outpatientNoShowService.queryAgeAnalysis(body);

        Integer page = body.getPage() != null ? body.getPage() : pageConfig.getDefaultPage();
        Integer pageSize = body.getPageSize() != null ? body.getPageSize() : pageConfig.getDefaultPageSize();
        PageResult<TableItem> table = outpatientNoShowService.queryTable(body, page, pageSize);

        OutpatientNoShowResponse response = new OutpatientNoShowResponse();
        response.setOverview(overview);
        response.setRefundOrigin(refundOrigin);
        response.setRefundChannel(refundChannel);
        response.setAgeAnalysis(ageAnalysis);
        response.setTable(table);

        return ApiResponse.success(response, MODULE.getChineseName() + "查询成功！");
    }

}