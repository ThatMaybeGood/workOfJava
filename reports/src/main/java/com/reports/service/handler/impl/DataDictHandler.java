package com.reports.service.handler.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reports.dto.common.ApiRequest;
import com.reports.dto.common.ApiResponse;
import com.reports.dto.request.DataDictRequest;
import com.reports.entity.CommonDictEntity;
import com.reports.enums.ReportModule;
import com.reports.service.CommonDictService;
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
 * 通用字典处理器
 * method: reports.common.data-dict
 */
@Slf4j
@Component
@MethodMapping("reports.common.data-dict")
public class DataDictHandler implements ReportHandler<DataDictRequest, Map<String, Object>> {

    private final CommonDictService commonDictService;
    private final ObjectMapper objectMapper;

    @Autowired
    public DataDictHandler(CommonDictService commonDictService, ObjectMapper objectMapper) {
        this.commonDictService = commonDictService;
        this.objectMapper = objectMapper;
    }

    private static final ReportModule MODULE = ReportModule.COMMON_DATA_DICT;

    @Override
    public ApiResponse<Map<String, Object>> handle(ApiRequest<Object> request) {
        SeqUtil.next();
        log.info("[{}] 处理请求", MODULE.getChineseName());

        DataDictRequest body;
        if (request.getBody() instanceof DataDictRequest) {
            body = (DataDictRequest) request.getBody();
        } else {
            body = objectMapper.convertValue(request.getBody(), DataDictRequest.class);
        }
        if (body == null) {
            body = new DataDictRequest();
        }

        String action = body.getAction() == null ? "query" : body.getAction();
        Map<String, Object> result = new HashMap<>();

        switch (action) {
            case "add": {
                CommonDictEntity entity = new CommonDictEntity();
                entity.setDictType(body.getDictType());
                entity.setDictCode(body.getDictCode());
                entity.setDictName(body.getDictName());
                entity.setSortNo(body.getSortNo());
                int affected = commonDictService.addDict(entity);
                result.put("affected", affected);
                return ApiResponse.success(result, "字典项新增成功！");
            }
            case "delete": {
                int affected = commonDictService.deleteDict(body.getId());
                result.put("affected", affected);
                return ApiResponse.success(result, "字典项删除成功！");
            }
            default: {
                List<CommonDictEntity> list = commonDictService.queryDictList(body.getDictType());
                result.put("list", list);
                result.put("total", list.size());
                return ApiResponse.success(result, "字典查询成功！");
            }
        }
    }
}
