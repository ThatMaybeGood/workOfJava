package com.reports.service.handler.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reports.config.PageConfig;
import com.reports.dto.common.ApiRequest;
import com.reports.dto.common.ApiResponse;
import com.reports.dto.request.StatDataSummaryRequest;
import com.reports.dto.response.stat.StatDataSummaryResponse;
import com.reports.enums.ReportModule;
import com.reports.service.StatDataSummaryService;
import com.reports.service.handler.MethodMapping;
import com.reports.service.handler.ReportHandler;
import com.reports.util.SeqUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 运营数据统计处理器(13个页签单接口, body.tab 区分)。
 */
@Slf4j
@Component
@MethodMapping("reports.stat.data-summary")
public class StatDataSummaryHandler implements ReportHandler<StatDataSummaryRequest, Object> {

    private final StatDataSummaryService statDataSummaryService;
    private final ObjectMapper objectMapper;
    private final PageConfig pageConfig;

    @Autowired
    public StatDataSummaryHandler(StatDataSummaryService statDataSummaryService,
                                  ObjectMapper objectMapper, PageConfig pageConfig) {
        this.statDataSummaryService = statDataSummaryService;
        this.objectMapper = objectMapper;
        this.pageConfig = pageConfig;
    }

    private static final ReportModule MODULE = ReportModule.STAT_DATA_SUMMARY;

    @Override
    public ApiResponse<Object> handle(ApiRequest<Object> request) {
        SeqUtil.next();
        log.info("[{}] 处理请求", MODULE.getChineseName());

        StatDataSummaryRequest body;
        if (request.getBody() instanceof StatDataSummaryRequest) {
            body = (StatDataSummaryRequest) request.getBody();
        } else {
            body = objectMapper.convertValue(request.getBody(), StatDataSummaryRequest.class);
        }
        if (body == null) {
            body = new StatDataSummaryRequest();
        }

        Integer page = body.getPage() != null ? body.getPage() : pageConfig.getDefaultPage();
        Integer pageSize = body.getPageSize() != null ? body.getPageSize() : pageConfig.getDefaultPageSize();

        StatDataSummaryResponse data = statDataSummaryService.query(body, page, pageSize);
        return ApiResponse.success(data, MODULE.getChineseName() + "查询成功！");
    }

}
