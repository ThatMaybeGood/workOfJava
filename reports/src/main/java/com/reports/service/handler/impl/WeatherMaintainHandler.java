package com.reports.service.handler.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reports.config.PageConfig;
import com.reports.dto.common.ApiRequest;
import com.reports.dto.common.ApiResponse;
import com.reports.dto.common.PageResult;
import com.reports.dto.request.WeatherMaintainRequest;
import com.reports.dto.response.outpatient.forecast.WeatherItem;
import com.reports.enums.ReportModule;
import com.reports.service.WeatherMaintainService;
import com.reports.service.handler.MethodMapping;
import com.reports.service.handler.ReportHandler;
import com.reports.util.SeqUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 天气数据维护处理器
 * method: reports.common.weather-maintain
 */
@Slf4j
@Component
@MethodMapping("reports.common.weather-maintain")
public class WeatherMaintainHandler implements ReportHandler<WeatherMaintainRequest, Map<String, Object>> {

    private final WeatherMaintainService weatherMaintainService;
    private final ObjectMapper objectMapper;
    private final PageConfig pageConfig;

    @Autowired
    public WeatherMaintainHandler(WeatherMaintainService weatherMaintainService,
                                  ObjectMapper objectMapper, PageConfig pageConfig) {
        this.weatherMaintainService = weatherMaintainService;
        this.objectMapper = objectMapper;
        this.pageConfig = pageConfig;
    }

    private static final ReportModule MODULE = ReportModule.WEATHER_MAINTAIN;

    @Override
    public ApiResponse<Map<String, Object>> handle(ApiRequest<Object> request) {
        SeqUtil.next();
        log.info("[{}] 处理请求", MODULE.getChineseName());

        WeatherMaintainRequest body;
        if (request.getBody() instanceof WeatherMaintainRequest) {
            body = (WeatherMaintainRequest) request.getBody();
        } else {
            body = objectMapper.convertValue(request.getBody(), WeatherMaintainRequest.class);
        }
        if (body == null) {
            body = new WeatherMaintainRequest();
        }

        String action = body.getAction() == null ? "query" : body.getAction();
        Map<String, Object> result = new HashMap<>();

        switch (action) {
            case "save": {
                int affected = weatherMaintainService.saveMaintain(body);
                result.put("affected", affected);
                return ApiResponse.success(result, "天气数据保存成功！");
            }
            case "delete": {
                int affected = weatherMaintainService.deleteMaintain(body);
                result.put("affected", affected);
                return ApiResponse.success(result, "天气数据删除成功！");
            }
            default: {
                Integer page = body.getPage() != null ? body.getPage() : pageConfig.getDefaultPage();
                Integer pageSize = body.getPageSize() != null ? body.getPageSize() : pageConfig.getDefaultPageSize();
                PageResult<WeatherItem> pageResult = weatherMaintainService.queryMaintainList(body, page, pageSize);
                result.put("list", pageResult.getList());
                result.put("total", pageResult.getTotal());
                result.put("page", pageResult.getPage());
                result.put("pageSize", pageResult.getPageSize());
                return ApiResponse.success(result, "天气数据查询成功！");
            }
        }
    }
}
