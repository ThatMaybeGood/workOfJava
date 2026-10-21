package com.reports.service.handler.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reports.dto.common.ApiRequest;
import com.reports.dto.common.ApiResponse;
import com.reports.dto.request.PreHospOperRequest;
import com.reports.entity.PreHospOperEntity;
import com.reports.enums.ReportModule;
import com.reports.service.PreHospOperService;
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
 * 院前操作员维护处理器
 * method: reports.cash.pre-hosp-oper
 */
@Slf4j
@Component
@MethodMapping("reports.cash.pre-hosp-oper")
public class PreHospOperHandler implements ReportHandler<PreHospOperRequest, Map<String, Object>> {

    private final PreHospOperService preHospOperService;
    private final ObjectMapper objectMapper;

    @Autowired
    public PreHospOperHandler(PreHospOperService preHospOperService, ObjectMapper objectMapper) {
        this.preHospOperService = preHospOperService;
        this.objectMapper = objectMapper;
    }

    private static final ReportModule MODULE = ReportModule.PRE_HOSP_OPER;

    @Override
    public ApiResponse<Map<String, Object>> handle(ApiRequest<Object> request) {
        SeqUtil.next();
        log.info("[{}] 处理请求", MODULE.getChineseName());

        PreHospOperRequest body;
        if (request.getBody() instanceof PreHospOperRequest) {
            body = (PreHospOperRequest) request.getBody();
        } else {
            body = objectMapper.convertValue(request.getBody(), PreHospOperRequest.class);
        }
        if (body == null) {
            body = new PreHospOperRequest();
        }

        String action = body.getAction() == null ? "query" : body.getAction();
        Map<String, Object> result = new HashMap<>();
        switch (action) {
            case "search": {
                int page = body.getPage() == null || body.getPage() < 1 ? 1 : body.getPage();
                int pageSize = body.getPageSize() == null || body.getPageSize() < 1 ? 10 : body.getPageSize();
                List<PreHospOperEntity> list = preHospOperService.searchStaff(body.getKeyword(), page, pageSize);
                result.put("list", list);
                result.put("total", preHospOperService.countStaff(body.getKeyword()));
                return ApiResponse.success(result, "候选人员查询成功！");
            }
            case "add": {
                int affected = preHospOperService.addOper(body);
                result.put("affected", affected);
                return ApiResponse.success(result, affected > 0 ? "操作员添加成功！"
                        : "保存失败，请核对HIS系统中是否存在该登录ID和该操作员！");
            }
            case "delete": {
                int affected = preHospOperService.removeOper(body.getId());
                result.put("affected", affected);
                return ApiResponse.success(result, "操作员删除成功！");
            }
            case "setValid": {
                int affected = preHospOperService.setValid(body.getId(), body.getValidFlag());
                result.put("affected", affected);
                return ApiResponse.success(result, "状态更新成功！");
            }
            default: {
                List<PreHospOperEntity> list = preHospOperService.queryList();
                result.put("list", list);
                result.put("total", list.size());
                return ApiResponse.success(result, "院前操作员查询成功！");
            }
        }
    }
}
