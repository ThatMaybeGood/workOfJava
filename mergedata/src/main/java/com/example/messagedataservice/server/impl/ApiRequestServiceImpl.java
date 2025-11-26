package com.example.messagedataservice.server.impl;

import com.example.messagedataservice.dto.ApiRequest;
import com.example.messagedataservice.dto.ApiRequestHead;
import com.example.messagedataservice.dto.HisIncomeDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ApiRequestServiceImpl {

    @Autowired
    private ApiRequestHead headConfig; // 注入 Head 配置

    // 注入 ObjectMapper Bean (通常Spring Boot会自动配置)
    @Autowired
    private ObjectMapper objectMapper;

    public String assembleAndGenerateJson() {

        // 1. 组装 Body
        HisIncomeDTO hisIncomeDTOBody = new HisIncomeDTO();
        hisIncomeDTOBody.setOperatorNo("测试");

        // 2. 组装 Message
        ApiRequest<HisIncomeDTO> request = new ApiRequest<>();
        request.setHead(headConfig);
        request.setBody(hisIncomeDTOBody);

        try {
            // 3. 序列化为 JSON 报文
            return objectMapper
                    .writerWithDefaultPrettyPrinter() // 格式化输出方便查看
                    .writeValueAsString(request);
        } catch (Exception e) {
            // 错误处理
            return "JSON 序列化失败: " + e.getMessage();
        }
    }
}

