package com.mergedata.server.impl;

import com.mergedata.model.dto.ApiRequest;
import com.mergedata.model.dto.ApiRequestHead;
import com.mergedata.model.vo.ApiResponse;
import com.mergedata.model.dto.external.HisDataRequestBodyDTO;
import com.mergedata.exception.BusinessException;
import com.mergedata.model.dto.external.HisIncomeResponseDTO;
import com.mergedata.server.HisDataService;
import com.mergedata.util.RestApiUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Slf4j
@Service
public class HisDataServiceImpl implements HisDataService {

    @Value("${api.urls.hisincome}")
    private String URL_API_HISINCOME;

    @Autowired
    private ApiRequestHead headConfig;

    @Autowired
    private RestApiUtil restApiUtil;

    private RestTemplate restTemplate;


    @Override
    public List<HisIncomeResponseDTO> findByDateOutp(String reportdate) {

        log.info("开始调用 HIS 收入 API (TypeRef): {}", URL_API_HISINCOME);

        // 1. 组装请求对象 (不变)
        HisDataRequestBodyDTO comBody = new HisDataRequestBodyDTO();
        comBody.setReportdate(reportdate);

        ApiRequest<HisDataRequestBodyDTO> apiRequest = new ApiRequest<>();
        apiRequest.setHead(headConfig);
        apiRequest.setBody(comBody);

        // 2. 【修正点 1】定义正确的返回类型
        // 外部 ApiResponse<HisIncomeDTO> 结构：泛型 T 应该代表 list 内部的数据类型
        ParameterizedTypeReference<ApiResponse<HisIncomeResponseDTO>> typeRef = new ParameterizedTypeReference<ApiResponse<HisIncomeResponseDTO>>() {};

        try {
            // 3. 调用工具类发起请求
            // 注意：apiResponse 的类型现在是 ApiResponse<HisIncomeDTO>
            ApiResponse<HisIncomeResponseDTO> apiResponse = restApiUtil.postForObject(
                    URL_API_HISINCOME,
                    apiRequest,
                    typeRef
            );

            // 4. 检查业务状态码 (核心判断逻辑)
            if (apiResponse.getResult().isSuccess()) {
                log.info("调用成功，业务处理成功。");
                // 得到的是 ApiResponseBody<HisIncomeDTO> 对象
                // 然后调用 getList() 才能获取到 List<HisIncomeDTO>
                return Optional.ofNullable(apiResponse.getBody())
                        .map(b -> b.getList()) // <-- 正确获取 List<HisIncomeDTO>
                        .orElse(Collections.emptyList());
            } else {
                // ... (错误处理逻辑不变)
                String errMsg = String.format("HIS接口业务失败。Code: %s, Msg: %s",
                        apiResponse.getResult().getCode(),apiResponse.getResult().getMsg());
                log.error(errMsg);
                throw new BusinessException(errMsg);
            }

        } catch (BusinessException e) {
            log.error("HIS 收入 API 调用失败: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public List<HisIncomeResponseDTO> findByDateInp(String reportDate) {

                RestTemplate restTemplate = new RestTemplate();

                // 请求头
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

                // 请求体
                Map<String, Object> requestBody = new HashMap<>();
                requestBody.put("name", "John");
                requestBody.put("age", 30);

                // 创建请求实体
                HttpEntity<Map<String, Object>> requestEntity =
                        new HttpEntity<>(requestBody, headers);

//                // 发送POST请求
//                ResponseEntity<String> response = restTemplate.postForEntity(
//                        URL_API_HISINCOME, requestEntity, String.class);

                // 或者使用exchange方法
                ResponseEntity<String> response = restTemplate.exchange(
                        URL_API_HISINCOME, HttpMethod.POST, requestEntity, String.class);

                System.out.println("响应状态: " + response.getStatusCode());
                System.out.println("响应体: " + response.getBody());


        return Collections.emptyList();
    }


}
