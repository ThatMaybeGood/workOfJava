package com.reports.service.handler.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reports.config.PageConfig;
import com.reports.dto.common.ApiRequest;
import com.reports.dto.common.ApiResponse;
import com.reports.dto.common.PageResult;
import com.reports.dto.request.ServiceQualityMaintainRequest;
import com.reports.dto.response.outpatient.service.quality.MaintainItem;
import com.reports.enums.ReportModule;
import com.reports.service.OutpatientServiceQualityService;
import com.reports.service.handler.MethodMapping;
import com.reports.service.handler.ReportHandler;
import com.reports.util.SeqUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 门诊服务质量数据维护处理器
 * method: reports.outp.service-quality-maintain
 */
@Slf4j
@Component
@MethodMapping("reports.outp.service-quality-maintain")
public class OutpatientServiceQualityMaintainHandler implements ReportHandler<ServiceQualityMaintainRequest, Map<String, Object>> {

    private final OutpatientServiceQualityService outpatientServiceQualityService;
    private final ObjectMapper objectMapper;
    private final PageConfig pageConfig;

    @Autowired
    public OutpatientServiceQualityMaintainHandler(OutpatientServiceQualityService outpatientServiceQualityService,
                                                   ObjectMapper objectMapper, PageConfig pageConfig) {
        this.outpatientServiceQualityService = outpatientServiceQualityService;
        this.objectMapper = objectMapper;
        this.pageConfig = pageConfig;
    }

    private static final ReportModule MODULE = ReportModule.OUTPATIENT_SERVICE_QUALITY_MAINTAIN;

    @Override
    public ApiResponse<Map<String, Object>> handle(ApiRequest<Object> request) {
        SeqUtil.next();
        log.info("[{}] 处理请求", MODULE.getChineseName());

        ServiceQualityMaintainRequest body;
        if (request.getBody() instanceof ServiceQualityMaintainRequest) {
            body = (ServiceQualityMaintainRequest) request.getBody();
        } else {
            body = objectMapper.convertValue(request.getBody(), ServiceQualityMaintainRequest.class);
        }
        if (body == null) {
            body = new ServiceQualityMaintainRequest();
        }

        String action = body.getAction() == null ? "query" : body.getAction();
        Map<String, Object> result = new HashMap<>();

        switch (action) {
            case "save": {
                int affected = outpatientServiceQualityService.saveMaintain(body);
                result.put("affected", affected);
                return ApiResponse.success(result, "数据维护保存成功！");
            }
            case "delete": {
                int affected = outpatientServiceQualityService.deleteMaintain(body);
                result.put("affected", affected);
                return ApiResponse.success(result, "数据维护删除成功！");
            }
            default: {
                Integer page = body.getPage() != null ? body.getPage() : pageConfig.getDefaultPage();
                Integer pageSize = body.getPageSize() != null ? body.getPageSize() : pageConfig.getDefaultPageSize();
                PageResult<MaintainItem> pageResult = outpatientServiceQualityService.queryMaintainList(body, page, pageSize);
                result.put("list", pageResult.getList());
                result.put("total", pageResult.getTotal());
                result.put("page", pageResult.getPage());
                result.put("pageSize", pageResult.getPageSize());
                return ApiResponse.success(result, "数据维护查询成功！");
            }
        }
    }
}
