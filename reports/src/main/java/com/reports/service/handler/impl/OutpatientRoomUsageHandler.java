package com.reports.service.handler.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reports.config.PageConfig;
import com.reports.dto.common.ApiRequest;
import com.reports.dto.common.ApiResponse;
import com.reports.dto.common.PageResult;
import com.reports.dto.request.OutpatientRoomUsageRequest;
import com.reports.dto.response.outpatient.room.usage.*;
import com.reports.service.OutpatientRoomUsageService;
import com.reports.enums.ReportModule;
import com.reports.service.handler.MethodMapping;
import com.reports.service.handler.ReportHandler;
import com.reports.util.SeqUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 诊室使用率分析处理器
 */
@Slf4j
@Component
@MethodMapping("reports.outp.outpatient-room-usage")
public class OutpatientRoomUsageHandler implements ReportHandler<OutpatientRoomUsageRequest, OutpatientRoomUsageResponse> {

    private final OutpatientRoomUsageService outpatientRoomUsageService;
    private final ObjectMapper objectMapper;
    private final PageConfig pageConfig;

    @Autowired
    public OutpatientRoomUsageHandler(OutpatientRoomUsageService outpatientRoomUsageService, ObjectMapper objectMapper, PageConfig pageConfig) {
        this.outpatientRoomUsageService = outpatientRoomUsageService;
        this.objectMapper = objectMapper;
        this.pageConfig = pageConfig;
    }

    private static final ReportModule MODULE = ReportModule.OUTPATIENT_ROOM_USAGE;

    @Override
    public ApiResponse<OutpatientRoomUsageResponse> handle(ApiRequest<Object> request) {
        SeqUtil.next();
        log.info("[{}] 处理请求", MODULE.getChineseName());

        OutpatientRoomUsageRequest body;
        if (request.getBody() instanceof OutpatientRoomUsageRequest) {
            body = (OutpatientRoomUsageRequest) request.getBody();
        } else {
            body = objectMapper.convertValue(request.getBody(), OutpatientRoomUsageRequest.class);
        }
        if (body == null) {
            body = new OutpatientRoomUsageRequest();
        }

        OverviewData overview = outpatientRoomUsageService.queryOverview(body);

        Integer page = body.getPage() != null ? body.getPage() : pageConfig.getDefaultPage();
        Integer pageSize = body.getPageSize() != null ? body.getPageSize() : pageConfig.getDefaultPageSize();
        PageResult<TableItem> table = outpatientRoomUsageService.queryTable(body, page, pageSize);

        OutpatientRoomUsageResponse response = new OutpatientRoomUsageResponse();
        response.setOverview(overview);
        response.setTable(table);

        return ApiResponse.success(response, MODULE.getChineseName() + "查询成功！");
    }

}