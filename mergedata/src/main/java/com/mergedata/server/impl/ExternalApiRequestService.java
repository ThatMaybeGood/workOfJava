package com.mergedata.server.impl;

import com.mergedata.constants.ReqConstant;
import com.mergedata.dto.ApiRequest;
import com.mergedata.dto.ApiRequestHead;
import com.mergedata.dto.HisIncomeDTO;
import com.mergedata.dto.YQOperatorDTO;
import com.mergedata.entity.CommonRequestBody;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.*;

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
    public List<HisIncomeDTO> getHisIncomeList(String reportDate) {
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
        HttpHeaders headers = new HttpHeaders();
        // 关键：强制设置 Content-Type 为 JSON
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        // 3. 封装 HttpEntity
        HttpEntity<ApiRequest<CommonRequestBody>> requestEntity = new HttpEntity<>(apiRequest,headers);


        try {
            // 4. 发起调用并处理返回类型
            // 注意：使用 ParameterizedTypeReference 处理 List<T> 这种泛型返回类型
            ResponseEntity<List<HisIncomeDTO>> responseEntity = restTemplate.exchange(
                    HISINCOME_SERVICE_URL,
                    HttpMethod.POST,
                    requestEntity,
                    new ParameterizedTypeReference<List<HisIncomeDTO>>() {}
            );



            // 2. 检查 HTTP 状态码（RestTemplate 默认只对 4xx/5xx 抛异常，但可以额外检查 2xx 以外的状态）
            if (responseEntity.getStatusCode().is2xxSuccessful()) {
                //返回结果，如果 body 为空则返回空列表
                 if (responseEntity.getBody() != null) {
                    log.info("调用用户服务接口成功: {}", HISINCOME_SERVICE_URL);
                    // 打印内容可能过多，这里只打印列表大小
                    log.debug("返回结果列表大小: {}", responseEntity.getBody().size());
                    return responseEntity.getBody();
                }
            }


        } catch (ResourceAccessException e) {
            // 1. 捕获连接错误 (如服务未启动、域名解析失败、连接超时)
            log.error("调用失败，网络或连接问题（服务可能未启动或连接超时）: {}", HISINCOME_SERVICE_URL, e);

        } catch (HttpClientErrorException e) {
            // 2. 捕获 HTTP 4xx 错误 (如 404 Not Found, 400 Bad Request)
            log.error("调用失败，客户端错误 (状态码: {}). 响应体: {}",
                    e.getStatusCode(),
                    e.getResponseBodyAsString().trim()); // 打印服务器返回的错误信息

        } catch (HttpServerErrorException e) {
            // 3. 捕获 HTTP 5xx 错误 (如 500 Internal Server Error)
            log.error("调用失败，服务器端错误 (状态码: {}). 响应体: {}",
                    e.getStatusCode(),
                    e.getResponseBodyAsString().trim());

        } catch (RestClientException e) {
            // 4. 捕获通用 RestTemplate 错误，主要包含**数据解析失败** (如日志中您遇到的 Content-Type 不匹配问题)
            log.error("调用失败，数据解析或通用 RestTemplate 错误: {}", HISINCOME_SERVICE_URL, e);

        } catch (Exception e) {
            // 5. 捕获所有未预期的其他运行时错误
            log.error("调用发生未知错误: {}", HISINCOME_SERVICE_URL, e);
        }

        // 3. 正常返回但 Body 为空，记录日志并返回空列表
        log.warn("调用接口成功，但返回体为空: {}", HISINCOME_SERVICE_URL);
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
                log.info("调用订单服务接口成功: {}", ORDER_SERVICE_URL);
                log.info("返回结果: {}", responseEntity.getBody());
                return responseEntity.getBody();
            }
        } catch (Exception e) {
            log.error("调用订单服务接口失败: {}", ORDER_SERVICE_URL, e);
        }

        return Collections.emptyList();
    }
}