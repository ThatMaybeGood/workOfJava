package com.reports.controller;

import com.reports.dto.common.ApiRequest;
import com.reports.dto.common.ApiResponse;
import com.reports.service.GatewayService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 统一网关入口
 */
@Slf4j
@RestController
@RequestMapping("/reports/gateway")
public class GatewayController {

    private final GatewayService gatewayService;

    @Autowired
    public GatewayController(GatewayService gatewayService) {
        this.gatewayService = gatewayService;
    }

    /**
     * 统一网关入口
     */
    @PostMapping
    public ApiResponse<?> gateway(@RequestBody ApiRequest<?> request) {
        log.trace("接收到网关请求: {}", request);
        return gatewayService.process(request);
    }

}
