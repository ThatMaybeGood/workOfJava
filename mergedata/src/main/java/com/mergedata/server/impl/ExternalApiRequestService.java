package com.mergedata.server.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.mergedata.constants.ReqConstant;
import com.mergedata.dto.ApiRequest;
import com.mergedata.dto.ApiRequestHead;
import com.mergedata.dto.ApiResponse;
import com.mergedata.dto.HisIncomeDTO;
import com.mergedata.entity.BusinessException;
import com.mergedata.dto.CommonRequestBody;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.*;

import java.util.Collections;
import java.util.List;


@Service
@Slf4j
public class ExternalApiRequestService {

    @Value("${api.urls.hisincome}")
    private String URL_API_HISINCOME;
 
    // 2. 注入依赖
    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ApiRequestHead headConfig; // 注入 Head 配置

    /**
     * 调用HIS收入数据接口
     * @param reportdate 报表日期
     * @return HIS收入数据，调用失败返回空列表
     */
    public List<HisIncomeDTO> getHisIncomeList(String reportdate) {
        log.info("开始调用用户服务 API: {}", URL_API_HISINCOME);

        // 1. 组装请求 Body
        CommonRequestBody body = new CommonRequestBody();
        body.setReportdate(reportdate);

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



        // 定义复杂的返回类型：ApiResponse<List<HisIncomeDTO>>
        ParameterizedTypeReference<ApiResponse<List<HisIncomeDTO>>> typeRef =
                new ParameterizedTypeReference<ApiResponse<List<HisIncomeDTO>>>() {};


        try {
            // 4. 发起调用并处理返回类型
            // 注意：使用 ParameterizedTypeReference 处理 List<T> 这种泛型返回类型
            ResponseEntity<ApiResponse<List<HisIncomeDTO>>> responseEntity = restTemplate.exchange(
                    URL_API_HISINCOME,
                    HttpMethod.POST,
                    requestEntity,
                    typeRef // 使用新的泛型类型
            //new ParameterizedTypeReference<List<HisIncomeDTO>>() {}
            );

            // 1. 检查 HTTP 状态码
            if (responseEntity.getStatusCode().is2xxSuccessful() && responseEntity.getBody() != null) {
                ApiResponse<List<HisIncomeDTO>> apiResponse = responseEntity.getBody();

                // 2. 检查业务状态码 (核心判断逻辑)
                if (apiResponse.getResult().isSuccess()) {
                    log.info("调用用户服务接口成功，业务处理成功: {}", URL_API_HISINCOME);

                    // 返回 body.list 中的数据
                    if (apiResponse.getBody() != null && apiResponse.getBody().getList() != null) {
                        List<HisIncomeDTO> resultList = apiResponse.getBody().getList();
                        log.debug("返回结果列表大小: {}", resultList.size());
                        return resultList;
                    } else {
                        // 业务成功，但 body 或 list 为空 (即 Postman 示例中的无数据情况)
                        log.warn("调用接口成功，但业务返回数据列表为空: {}", URL_API_HISINCOME);
                        return Collections.emptyList();
                    }
                } else {
                    // HTTP 200 OK，但业务状态码非 10000 (业务失败)
                    log.error("调用成功，但业务系统处理失败。Code: {}, Msg: {}",
                            apiResponse.getResult().getCode(),
                            apiResponse.getResult().getSub_msg());
                    // 这里可以抛出 BusinessException 或返回空列表
                    throw new BusinessException("HIS接口业务失败: " + apiResponse.getResult().getSub_msg());
                }
            }

        } catch (ResourceAccessException e) {
            // 1. 捕获连接错误 (如服务未启动、域名解析失败、连接超时)
            log.error("调用失败，网络或连接问题（服务可能未启动或连接超时）: {}", URL_API_HISINCOME, e);

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
            // 4.捕获 RestClientException 时，现在只代表**最外层结构**解析失败，或者其他 RestTemplate 内部错误。
            // 捕获 List<T> 失败的范围小得多，更清晰。
            log.error("调用失败，数据解析或通用 RestTemplate 错误（可能为非JSON响应）：{}", URL_API_HISINCOME, e);
        } catch (Exception e) {
            // 5. 捕获所有未预期的其他运行时错误
            log.error("调用发生未知错误: {}", URL_API_HISINCOME, e);
        }

        // 3. 正常返回但 Body 为空，记录日志并返回空列表
        log.warn("调用接口成功，但返回体为空: {}", URL_API_HISINCOME);
        return Collections.emptyList();
    }


    /**
     * 调用HIS收入数据接口
     * @param reportdate 报表日期
     * @return HIS收入数据，调用失败返回空列表
     */
    public List<HisIncomeDTO> getHisIncomeList_String(String reportdate) {
        log.info("开始调用用户服务 API: {}", URL_API_HISINCOME);

        // 1. 组装请求 Body
        CommonRequestBody body = new CommonRequestBody();
        body.setReportdate(reportdate);

        //设置对应方法
        headConfig.setMethod(ReqConstant.METHOD_HISINCOME);
        // 1. 生成当前的毫秒级时间戳字符串
        String currentTimestamp = String.valueOf(System.currentTimeMillis());
        // 2. 赋值给 head
        headConfig.setTimestamp(currentTimestamp);

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



        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

        String jsonBody = null; // 声明在 try 块外部，方便异常处理中访问

        try {
            // 4. 发起调用，接收 String
            ResponseEntity<String> responseEntity = restTemplate.exchange(
                    URL_API_HISINCOME, HttpMethod.POST, requestEntity, String.class
            );

            // 检查 HTTP 状态码
            if (!responseEntity.getStatusCode().is2xxSuccessful()) {
                // ... (HTTP 错误处理不变) ...
                return Collections.emptyList();
            }

            jsonBody = responseEntity.getBody();
            if (jsonBody == null) {
                log.warn("调用成功，但返回体为空字符串: {}", URL_API_HISINCOME);
                return Collections.emptyList();
            }

            // ==========================================================
            // === 1. 第一次解析：尝试解析出 code ======================
            // ==========================================================

            // 使用 TypeReference 获取 ApiResponse 结构
            TypeReference<ApiResponse<List<HisIncomeDTO>>> apiTypeRef =
                    new TypeReference<ApiResponse<List<HisIncomeDTO>>>() {};

            // 第一次解析只需要 Result 部分，假设 ApiResponse 的 Result 类已经定义好
            ApiResponse<List<HisIncomeDTO>> simpleResponse = objectMapper.readValue(jsonBody, apiTypeRef);

            String responseCode = simpleResponse.getResult().getCode();

            // ==========================================================
            // === 2. 判断 code 并进行二次解析 ============================
            // ==========================================================

            if (ReqConstant.API_RESPONSE_SUCCESS.equals(responseCode) || "10000".equals(responseCode)) {
                // ** 路径 A: 业务成功 (code="10000") **

                // 此时 simpleResponse 已经是成功结构，无需二次解析，直接使用即可
                ApiResponse<List<HisIncomeDTO>> apiResponse = simpleResponse;

                if (apiResponse.getBody() != null && apiResponse.getBody().getList() != null) {
                    log.info("调用成功，业务处理成功 ({} 条数据)", apiResponse.getBody().getList().size());
                    return apiResponse.getBody().getList();
                } else {
                    log.warn("调用成功，业务列表为空");
                    return Collections.emptyList();
                }

            } else if (ReqConstant.API_RESPONSE_SUCCESS.equals(responseCode) || "40001".equals(responseCode)) {
                // ** 路径 B: 缺少参数错误 (code="40001") **

                // 如果 "40001" 结构不同，需要二次解析到专门的 Error DTO
                // 这里我们假设使用相同的 ApiResponse 结构，但会记录详细错误信息
                log.error("业务处理失败：缺少参数。Code: {}, Msg: {}",
                        responseCode, simpleResponse.getResult().getSub_msg());

                // 抛出带有详细信息的业务异常
                String solution = "请检查请求参数，尤其是 sys_track_code 字段。";
                throw new BusinessException("HIS接口缺少参数: " + simpleResponse.getResult().getSub_msg() + " | 建议: " + solution);

            } else {
                // ** 路径 C: 其他业务错误 **
                log.error("调用成功，但遇到未知业务错误。Code: {}, Msg: {}",
                        responseCode, simpleResponse.getResult().getSub_msg());
                throw new BusinessException("HIS接口未知业务错误，Code: " + responseCode);
            }
        }catch (JsonProcessingException e) {
            log.error("调用失败，JSON处理错误: {}", URL_API_HISINCOME, e);

        } catch (ResourceAccessException e) {
            // 1. 捕获连接错误 (如服务未启动、域名解析失败、连接超时)
            log.error("调用失败，网络或连接问题（服务可能未启动或连接超时）: {}", URL_API_HISINCOME, e);

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
            // 4.捕获 RestClientException 时，现在只代表**最外层结构**解析失败，或者其他 RestTemplate 内部错误。
            // 捕获 List<T> 失败的范围小得多，更清晰。
            log.error("调用失败，数据解析或通用 RestTemplate 错误（可能为非JSON响应）：{}", URL_API_HISINCOME, e);
        } catch (Exception e) {
            // 5. 捕获所有未预期的其他运行时错误
            log.error("调用发生未知错误: {}", URL_API_HISINCOME, e);
        }

        // 3. 正常返回但 Body 为空，记录日志并返回空列表
        log.warn("调用接口成功，但返回体为空: {}", URL_API_HISINCOME);
        return Collections.emptyList();
    }

}