package com.showexcel.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ApiRootController {

    @GetMapping("")
    public String apiRoot() {
        return "API endpoints available: \n" +
               "- /api/cash-statistics - 现金统计相关API\n" +
               "- /api/holidays - 节假日管理API";
    }

    @GetMapping("/")
    public String apiRootSlash() {
        return apiRoot();
    }
}