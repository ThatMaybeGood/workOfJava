package com.reports.service.handler.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reports.dto.common.ApiRequest;
import com.reports.dto.common.ApiResponse;
import com.reports.dto.common.PageResult;
import com.reports.config.PageConfig;
import com.reports.dto.request.OutpatientOperationRequest;
import com.reports.dto.response.*;
import com.reports.service.OutpatientOperationService;
import com.reports.service.handler.MethodMapping;
import com.reports.service.handler.ReportHandler;
import com.reports.util.SeqUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 门诊运行数据统计处理器
 */
@Slf4j
@Component
@MethodMapping("reports.outp.outpatient-operation")
public class OutpatientOperationHandler implements ReportHandler<OutpatientOperationRequest, OutpatientOperationResponse> {

    private final OutpatientOperationService outpatientOperationService;
    private final ObjectMapper objectMapper;
    private final PageConfig pageConfig;

    @Autowired
    public OutpatientOperationHandler(OutpatientOperationService outpatientOperationService, ObjectMapper objectMapper, PageConfig pageConfig) {
        this.outpatientOperationService = outpatientOperationService;
        this.objectMapper = objectMapper;
        this.pageConfig = pageConfig;
    }

    @Override
    public ApiResponse<OutpatientOperationResponse> handle(ApiRequest<Object> request) {
        SeqUtil.next();
        log.info("处理门诊运行数据统计请求");

        // 将 body 从 LinkedHashMap 转换为具体类型
        OutpatientOperationRequest body;
        if (request.getBody() instanceof OutpatientOperationRequest) {
            body = (OutpatientOperationRequest) request.getBody();
        } else {
            body = objectMapper.convertValue(request.getBody(), OutpatientOperationRequest.class);
        }
        if (body == null) {
            body = new OutpatientOperationRequest();
        }

        // 查询概览数据
        OverviewData overview = outpatientOperationService.queryOverview(body);

        // 查询表格数据（分页）
        Integer page = body.getPage() != null ? body.getPage() : pageConfig.getDefaultPage();
        Integer pageSize = body.getPageSize() != null ? body.getPageSize() : pageConfig.getDefaultPageSize();
        PageResult<TableItem> table = outpatientOperationService.queryTable(body, page, pageSize);

        // 组装响应
        OutpatientOperationResponse response = new OutpatientOperationResponse();
        response.setOverview(overview);
        response.setTable(table);

        return ApiResponse.success(response, "门诊运行数据统计查询成功！");
    }

}
