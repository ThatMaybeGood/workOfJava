package com.example.messagedataservice.server.impl;

import com.example.messagedataservice.constants.ReqConstant;
import com.example.messagedataservice.dto.ApiRequest;
import com.example.messagedataservice.dto.ApiRequestHead;
import com.example.messagedataservice.dto.HisIncomeDTO;
import com.example.messagedataservice.dto.YQOperatorDTO;
import com.example.messagedataservice.entity.CommonRequestBody;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class ExternalApiRequestService {

    // 1. 注入配置文件中的接口地址
    private String HISINCOME_SERVICE_URL = ReqConstant.URL_HISINCOME;

    private String ORDER_SERVICE_URL;

    // 2. 注入依赖
    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ApiRequestHead headConfig; // 注入 Head 配置

    /**
     * 调用用户服务接口，获取用户列表
     * @param departmentId 部门ID
     * @return 用户列表，调用失败返回空列表
     */
    public List<HisIncomeDTO> getUsersList(String reportDate) {
        log.info("开始调用用户服务 API: {}", HISINCOME_SERVICE_URL);

        // 1. 组装请求 Body
        CommonRequestBody body = new CommonRequestBody();
        body.setReportDate(reportDate);

        //设置对应方法
        headConfig.setMethod(ReqConstant.METHOD_HISINCOME);

        // 2. 组装完整请求报文（Head + Body）
        ApiRequest<CommonRequestBody> apiRequest = new ApiRequest<>();
        apiRequest.setHead(headConfig);
        apiRequest.setBody(body);

        // 3. 封装 HttpEntity
        HttpEntity<ApiRequest<CommonRequestBody>> requestEntity = new HttpEntity<>(apiRequest);

        try {
            // 4. 发起调用并处理返回类型
            // 注意：使用 ParameterizedTypeReference 处理 List<T> 这种泛型返回类型
            ResponseEntity<List<HisIncomeDTO>> responseEntity = restTemplate.exchange(
                    HISINCOME_SERVICE_URL,
                    HttpMethod.POST,
                    requestEntity,
                    new ParameterizedTypeReference<List<HisIncomeDTO>>() {}
            );

            // 5. 返回结果，如果 body 为空则返回空列表
            if (responseEntity.getBody() != null) {
                return responseEntity.getBody();
            }
        } catch (Exception e) {
            log.error("调用用户服务接口失败: {}", HISINCOME_SERVICE_URL, e);
        }

        return Collections.emptyList();
    }

    /**
     * 调用订单服务接口，获取订单列表
     * @param goodsId 商品ID
     * @return 订单列表，调用失败返回空列表
     */
    public List<YQOperatorDTO> getOrdersList(String reportDate) {
        log.info("开始调用订单服务 API: {}", ORDER_SERVICE_URL);

        // 1. 组装请求 Body
        CommonRequestBody body = new CommonRequestBody();
        body.setReportDate(reportDate);

        // 2. 组装完整请求报文（Head + Body）
        ApiRequest<CommonRequestBody> requestMessage = new ApiRequest<>();
        requestMessage.setHead(headConfig);
        requestMessage.setBody(body);

        // 3. 封装 HttpEntity
        HttpEntity<ApiRequest<CommonRequestBody>> requestEntity = new HttpEntity<>(requestMessage);

        try {
            // 4. 发起调用并处理返回类型
            ResponseEntity<List<YQOperatorDTO>> responseEntity = restTemplate.exchange(
                    ORDER_SERVICE_URL,
                    HttpMethod.POST,
                    requestEntity,
                    new ParameterizedTypeReference<List<YQOperatorDTO>>() {}
            );

            if (responseEntity.getBody() != null) {
                return responseEntity.getBody();
            }
        } catch (Exception e) {
            log.error("调用订单服务接口失败: {}", ORDER_SERVICE_URL, e);
        }

        return Collections.emptyList();
    }
}