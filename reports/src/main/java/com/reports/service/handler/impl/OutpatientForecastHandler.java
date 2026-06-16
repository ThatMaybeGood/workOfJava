package com.reports.service.handler.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reports.config.PageConfig;
import com.reports.dto.common.ApiRequest;
import com.reports.dto.common.ApiResponse;
import com.reports.dto.request.OutpatientForecastRequest;
import com.reports.dto.response.outpatient.forecast.*;
import com.reports.service.OutpatientForecastService;
import com.reports.enums.ReportModule;
import com.reports.service.handler.MethodMapping;
import com.reports.service.handler.ReportHandler;
import com.reports.util.SeqUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 预测门诊量报表处理器
 */
@Slf4j
@Component
@MethodMapping("reports.outp.outpatient-forecast")
public class OutpatientForecastHandler implements ReportHandler<OutpatientForecastRequest, OutpatientForecastResponse> {

    private final OutpatientForecastService outpatientForecastService;
    private final ObjectMapper objectMapper;
    private final PageConfig pageConfig;

    @Autowired
    public OutpatientForecastHandler(OutpatientForecastService outpatientForecastService, ObjectMapper objectMapper, PageConfig pageConfig) {
        this.outpatientForecastService = outpatientForecastService;
        this.objectMapper = objectMapper;
        this.pageConfig = pageConfig;
    }

    private static final ReportModule MODULE = ReportModule.OUTPATIENT_FORECAST;

    @Override
    public ApiResponse<OutpatientForecastResponse> handle(ApiRequest<Object> request) {
        SeqUtil.next();
        log.info("[{}] 处理请求", MODULE.getChineseName());

        OutpatientForecastRequest body;
        if (request.getBody() instanceof OutpatientForecastRequest) {
            body = (OutpatientForecastRequest) request.getBody();
        } else {
            body = objectMapper.convertValue(request.getBody(), OutpatientForecastRequest.class);
        }
        if (body == null) {
            body = new OutpatientForecastRequest();
        }

        OverviewData overview = outpatientForecastService.queryOverview(body);
        MonthForecast monthForecast = outpatientForecastService.queryMonthForecast(body);
        YearForecast yearForecast = outpatientForecastService.queryYearForecast(body);

        OutpatientForecastResponse response = new OutpatientForecastResponse();
        response.setOverview(overview);
        response.setMonthForecast(monthForecast);
        response.setYearForecast(yearForecast);

        return ApiResponse.success(response, MODULE.getChineseName() + "查询成功！");
    }

}