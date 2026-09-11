package com.reports.service.handler.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reports.dto.common.ApiRequest;
import com.reports.dto.common.ApiResponse;
import com.reports.dto.request.StaffDictRequest;
import com.reports.entity.StaffDictEntity;
import com.reports.enums.ReportModule;
import com.reports.service.StaffDictService;
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
 * 人员字典处理器
 * method: reports.common.staff-dict
 */
@Slf4j
@Component
@MethodMapping("reports.common.staff-dict")
public class StaffDictHandler implements ReportHandler<StaffDictRequest, Map<String, Object>> {

    private final StaffDictService staffDictService;
    private final ObjectMapper objectMapper;

    @Autowired
    public StaffDictHandler(StaffDictService staffDictService, ObjectMapper objectMapper) {
        this.staffDictService = staffDictService;
        this.objectMapper = objectMapper;
    }

    private static final ReportModule MODULE = ReportModule.COMMON_STAFF_DICT;

    @Override
    public ApiResponse<Map<String, Object>> handle(ApiRequest<Object> request) {
        SeqUtil.next();
        log.info("[{}] 处理请求", MODULE.getChineseName());

        StaffDictRequest body;
        if (request.getBody() instanceof StaffDictRequest) {
            body = (StaffDictRequest) request.getBody();
        } else {
            body = objectMapper.convertValue(request.getBody(), StaffDictRequest.class);
        }
        if (body == null) {
            body = new StaffDictRequest();
        }

        List<StaffDictEntity> list = staffDictService.queryStaffList(body.getDeptCode(), body.getStaffName());

        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", list.size());

        return ApiResponse.success(result, "人员字典查询成功！");
    }
}
