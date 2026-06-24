package com.reports.service.handler.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reports.dto.common.ApiRequest;
import com.reports.dto.common.ApiResponse;
import com.reports.dto.request.DeptDictRequest;
import com.reports.dto.response.common.DeptDictItem;
import com.reports.service.DeptDictService;
import com.reports.service.handler.MethodMapping;
import com.reports.service.handler.ReportHandler;
import com.reports.util.SeqUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 科室字典处理器
 * method: reports.common.dept-dict
 */
@Slf4j
@Component
@MethodMapping("reports.common.dept-dict")
public class DeptDictHandler implements ReportHandler<DeptDictRequest, Map<String, Object>> {

    private final DeptDictService deptDictService;
    private final ObjectMapper objectMapper;

    @Autowired
    public DeptDictHandler(DeptDictService deptDictService, ObjectMapper objectMapper) {
        this.deptDictService = deptDictService;
        this.objectMapper = objectMapper;
    }

    @Override
    public ApiResponse<Map<String, Object>> handle(ApiRequest<Object> request) {
        SeqUtil.next();
        log.info("[科室字典] 处理请求");

        DeptDictRequest body;
        if (request.getBody() instanceof DeptDictRequest) {
            body = (DeptDictRequest) request.getBody();
        } else {
            body = objectMapper.convertValue(request.getBody(), DeptDictRequest.class);
        }
        if (body == null) {
            body = new DeptDictRequest();
        }

        // 默认查询门诊
        if (body.getDeptType() == null) {
            body.setDeptType(0);
        }

        List<DeptDictItem> list = deptDictService.queryDeptDict(body);

        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", list.size());

        return ApiResponse.success(result, "科室字典查询成功！");
    }
}
