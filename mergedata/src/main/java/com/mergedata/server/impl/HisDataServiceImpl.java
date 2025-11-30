package com.mergedata.server.impl;

import com.mergedata.constants.ReqConstant;
import com.mergedata.dto.*;
import com.mergedata.entity.BusinessException;
import com.mergedata.model.HisIncomeDTO;
import com.mergedata.server.HisDataService;
import com.mergedata.util.RestApiUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

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
    public List<HisIncomeDTO> findByDate(String reportdate) {

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
                new ParameterizedTypeReference<ApiResponse<HisIncomeDTO>>() {
                };

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
                        apiResponse.getResult().getCode(),apiResponse.getResult().getMsg());
                log.error(errMsg);
                throw new BusinessException(errMsg);
            }

        } catch (BusinessException e) {
            log.error("HIS 收入 API 调用失败: {}", e.getMessage());
            return Collections.emptyList();
        }
    }
}
