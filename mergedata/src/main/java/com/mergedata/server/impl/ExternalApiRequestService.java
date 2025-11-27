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
import com.mergedata.util.RestApiUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

// 【修正点】由于您已经将 ApiResponseBody<T> 放入 ApiResponse<T> 中，
// 且 ApiResponseBody<T> 内部包含 List<T>，这里不需要额外导入 ApiResponseBody。

@Service
@Slf4j
public class ExternalApiRequestService {

    @Value("${api.urls.hisincome}")
    private String URL_API_HISINCOME;

    @Autowired
    private ApiRequestHead headConfig;

    @Autowired
    private RestApiUtil restApiUtil;

    // 为了实现 String 方法中的手动解析，仍然需要 ObjectMapper
    private final ObjectMapper objectMapper = new ObjectMapper().setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);


    /**
     * 调用HIS收入数据接口 - 使用 ParameterizedTypeReference (推荐)
     */
    public List<HisIncomeDTO> getHisIncomeList(String reportdate) {
        log.info("开始调用 HIS 收入 API (TypeRef): {}", URL_API_HISINCOME);

        // 1. 组装请求对象 (不变)
        CommonRequestBody body = new CommonRequestBody();
        body.setReportdate(reportdate);
        headConfig.setMethod(ReqConstant.METHOD_HISINCOME);
        ApiRequest<CommonRequestBody> apiRequest = new ApiRequest<>();
        apiRequest.setHead(headConfig);
        apiRequest.setBody(body);

        // 2. 【修正点 1】定义正确的返回类型
        // 外部 ApiResponse<HisIncomeDTO> 结构：泛型 T 应该代表 list 内部的数据类型
        ParameterizedTypeReference<ApiResponse<HisIncomeDTO>> typeRef = // 【修正】不再是 List<HisIncomeDTO>
                new ParameterizedTypeReference<ApiResponse<HisIncomeDTO>>() {};

        try {
            // 3. 调用工具类发起请求
            // 注意：apiResponse 的类型现在是 ApiResponse<HisIncomeDTO>
            ApiResponse<HisIncomeDTO> apiResponse = restApiUtil.postForObject(
                    URL_API_HISINCOME,
                    apiRequest,
                    typeRef
            );

            // 4. 检查业务状态码 (核心判断逻辑)
            if (apiResponse.getResult().isSuccess()) {
                log.info("调用成功，业务处理成功。");
                // 【修正点 2】获取数据：getBody() 得到的是 ApiResponseBody<HisIncomeDTO> 对象
                // 然后调用 getList() 才能获取到 List<HisIncomeDTO>
                return Optional.ofNullable(apiResponse.getBody())
                        .map(b -> b.getList()) // <-- 正确获取 List<HisIncomeDTO>
                        .orElse(Collections.emptyList());
            } else {
                // ... (错误处理逻辑不变)
                String errMsg = String.format("HIS接口业务失败。Code: %s, Msg: %s",
                        apiResponse.getResult().getCode(), apiResponse.getResult().getSub_msg());
                log.error(errMsg);
                throw new BusinessException(errMsg);
            }

        } catch (BusinessException e) {
            log.error("HIS 收入 API 调用失败: {}", e.getMessage());
            return Collections.emptyList();
        }
    }


    /**
     * 调用HIS收入数据接口 - 接收 String 手动解析
     */
    public List<HisIncomeDTO> getHisIncomeList_String(String reportdate) {
        log.info("开始调用 HIS 收入 API (String): {}", URL_API_HISINCOME);

        // 1. 组装请求对象 (不变)
        CommonRequestBody body = new CommonRequestBody();
        body.setReportdate(reportdate);
        headConfig.setMethod(ReqConstant.METHOD_HISINCOME);
        headConfig.setTimestamp(String.valueOf(System.currentTimeMillis()));
        ApiRequest<CommonRequestBody> apiRequest = new ApiRequest<>();
        apiRequest.setHead(headConfig);
        apiRequest.setBody(body);

        try {
            // 2. 调用工具类发起请求，接收原始 String (不变)
            String jsonBody = restApiUtil.postForString(URL_API_HISINCOME, apiRequest);

            // 3. 【修正点 3】手动进行 JSON 解析：TypeReference 必须修正
            TypeReference<ApiResponse<HisIncomeDTO>> apiTypeRef = // 【修正】不再是 List<HisIncomeDTO>
                    new TypeReference<ApiResponse<HisIncomeDTO>>() {};

            // 第一次解析：获取整个结构
            ApiResponse<HisIncomeDTO> apiResponse = objectMapper.readValue(jsonBody, apiTypeRef); // 【修正】apiResponse 类型

            String responseCode = apiResponse.getResult().getCode();

            if (ReqConstant.API_RESPONSE_SUCCESS.equals(responseCode) || "10000".equals(responseCode)) {
                // 业务成功
                log.info("调用成功，业务处理成功。");
                // 【修正点 4】获取数据：getBody() 得到的是 ApiResponseBody<HisIncomeDTO>
                return Optional.ofNullable(apiResponse.getBody())
                        .map(b -> b.getList()) // <-- 正确获取 List<HisIncomeDTO>
                        .orElse(Collections.emptyList());

            } else {
                // ... (错误处理逻辑不变)
                String msg = String.format("HIS接口业务失败。Code: %s, Msg: %s",
                        responseCode, apiResponse.getResult().getSub_msg());
                log.error(msg);
                throw new BusinessException(msg);
            }

        } catch (JsonProcessingException e) {
            log.error("调用失败，JSON处理错误: {}", URL_API_HISINCOME, e);
            return Collections.emptyList();
        } catch (BusinessException e) {
            log.error("HIS 收入 API 调用失败: {}", e.getMessage());
            return Collections.emptyList();
        }
    }
}