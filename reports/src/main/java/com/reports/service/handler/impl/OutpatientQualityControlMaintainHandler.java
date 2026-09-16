package com.reports.service.handler.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reports.dto.common.ApiRequest;
import com.reports.dto.common.ApiResponse;
import com.reports.dto.request.QualityControlMaintainRequest;
import com.reports.dto.response.outpatient.quality.control.QcMaintainItem;
import com.reports.enums.ReportModule;
import com.reports.service.OutpatientQualityControlService;
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
 * 门诊管理质量控制数据维护处理器
 * method: reports.outp.quality-control-maintain
 */
@Slf4j
@Component
@MethodMapping("reports.outp.quality-control-maintain")
public class OutpatientQualityControlMaintainHandler implements ReportHandler<QualityControlMaintainRequest, Map<String, Object>> {

    private final OutpatientQualityControlService outpatientQualityControlService;
    private final ObjectMapper objectMapper;

    @Autowired
    public OutpatientQualityControlMaintainHandler(OutpatientQualityControlService outpatientQualityControlService,
                                                   ObjectMapper objectMapper) {
        this.outpatientQualityControlService = outpatientQualityControlService;
        this.objectMapper = objectMapper;
    }

    private static final ReportModule MODULE = ReportModule.OUTPATIENT_QUALITY_CONTROL_MAINTAIN;

    @Override
    public ApiResponse<Map<String, Object>> handle(ApiRequest<Object> request) {
        SeqUtil.next();
        log.info("[{}] 处理请求", MODULE.getChineseName());

        QualityControlMaintainRequest body;
        if (request.getBody() instanceof QualityControlMaintainRequest) {
            body = (QualityControlMaintainRequest) request.getBody();
        } else {
            body = objectMapper.convertValue(request.getBody(), QualityControlMaintainRequest.class);
        }
        if (body == null) {
            body = new QualityControlMaintainRequest();
        }

        String action = body.getAction() == null ? "query" : body.getAction();
        Map<String, Object> result = new HashMap<>();

        if ("save".equals(action)) {
            int affected = outpatientQualityControlService.saveMaintain(body);
            result.put("affected", affected);
            return ApiResponse.success(result, "数据维护保存成功！");
        }

        List<QcMaintainItem> list = outpatientQualityControlService.queryMaintainList(body);
        result.put("list", list);
        return ApiResponse.success(result, "数据维护查询成功！");
    }
}
