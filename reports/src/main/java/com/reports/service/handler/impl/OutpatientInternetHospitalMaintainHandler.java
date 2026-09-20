package com.reports.service.handler.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reports.dto.common.ApiRequest;
import com.reports.dto.common.ApiResponse;
import com.reports.dto.request.InternetHospitalMaintainRequest;
import com.reports.dto.response.outpatient.internet.hospital.IhMaintainItem;
import com.reports.enums.ReportModule;
import com.reports.service.OutpatientInternetHospitalService;
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
 * 互医质控运营月报数据维护处理器
 * method: reports.outp.internet-hospital-maintain
 */
@Slf4j
@Component
@MethodMapping("reports.outp.internet-hospital-maintain")
public class OutpatientInternetHospitalMaintainHandler implements ReportHandler<InternetHospitalMaintainRequest, Map<String, Object>> {

    private final OutpatientInternetHospitalService outpatientInternetHospitalService;
    private final ObjectMapper objectMapper;

    @Autowired
    public OutpatientInternetHospitalMaintainHandler(OutpatientInternetHospitalService outpatientInternetHospitalService,
                                                     ObjectMapper objectMapper) {
        this.outpatientInternetHospitalService = outpatientInternetHospitalService;
        this.objectMapper = objectMapper;
    }

    private static final ReportModule MODULE = ReportModule.OUTPATIENT_INTERNET_HOSPITAL_MAINTAIN;

    @Override
    public ApiResponse<Map<String, Object>> handle(ApiRequest<Object> request) {
        SeqUtil.next();
        log.info("[{}] 处理请求", MODULE.getChineseName());

        InternetHospitalMaintainRequest body;
        if (request.getBody() instanceof InternetHospitalMaintainRequest) {
            body = (InternetHospitalMaintainRequest) request.getBody();
        } else {
            body = objectMapper.convertValue(request.getBody(), InternetHospitalMaintainRequest.class);
        }
        if (body == null) {
            body = new InternetHospitalMaintainRequest();
        }

        String action = body.getAction() == null ? "query" : body.getAction();
        Map<String, Object> result = new HashMap<>();

        if ("save".equals(action)) {
            int affected = outpatientInternetHospitalService.saveMaintain(body);
            result.put("affected", affected);
            return ApiResponse.success(result, "数据维护保存成功！");
        }

        List<IhMaintainItem> list = outpatientInternetHospitalService.queryMaintainList(body);
        result.put("list", list);
        return ApiResponse.success(result, "数据维护查询成功！");
    }
}
