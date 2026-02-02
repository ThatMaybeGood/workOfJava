package com.mergedata.server.impl;

import com.mergedata.constants.ResConstant;
import com.mergedata.model.dto.ApiRequest;
import com.mergedata.model.dto.ApiRequestHead;
import com.mergedata.model.dto.external.HisInpIncomeResponseDTO;
import com.mergedata.model.vo.ApiResponse;
import com.mergedata.model.dto.external.HisDataRequestBodyDTO;
import com.mergedata.exception.BusinessException;
import com.mergedata.model.dto.external.HisOutpIncomeResponseDTO;
import com.mergedata.model.vo.ApiResponseBodyList;
import com.mergedata.server.HisDataService;
import com.mergedata.util.RestApiUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
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
    public List<HisOutpIncomeResponseDTO> findByDateOutp(String reportdate) {

        log.info("开始调用 HIS 门诊现金报表收入 API: {}", URL_API_HISINCOME);

        return callHisIncomeApi(reportdate,
                ResConstant.HIS_METHOD_OUTP,
                new ParameterizedTypeReference<ApiResponse<ApiResponseBodyList<HisOutpIncomeResponseDTO>>>() {},
                ResConstant.REPORT_NAME_OUTP);
    }
    @Override
    public List<HisInpIncomeResponseDTO> findByDateInp(String reportdate) {

        log.info("开始调用 HIS 住院现金报表收入 API: {}", URL_API_HISINCOME);

        return callHisIncomeApi(reportdate,
                ResConstant.HIS_METHOD_INP,
                new ParameterizedTypeReference<ApiResponse<ApiResponseBodyList<HisInpIncomeResponseDTO>>>() {},
                ResConstant.REPORT_NAME_INP);
    }

    /**
     * 通用的 HIS 收入 API 调用方法
     * @param reportdate 报表日期
     * @param method HIS 方法类型
     * @param typeRef 返回类型引用
     * @param apiType API 类型描述（用于日志和错误信息）
     * @return 数据列表
     * @param <T> 具体的 DTO 类型
     */
    private <T> List<T> callHisIncomeApi(String reportdate,
                                         String method,
                                         ParameterizedTypeReference<ApiResponse<ApiResponseBodyList<T>>> typeRef,
                                         String apiType) {

        try {
            // 1. 组装请求对象
            HisDataRequestBodyDTO comBody = new HisDataRequestBodyDTO();
            comBody.setReportDate(reportdate);

            ApiRequest<HisDataRequestBodyDTO> apiRequest = new ApiRequest<>();
            headConfig.setMethod(method);
            apiRequest.setHead(headConfig);
            apiRequest.setBody(comBody);

            // 2. 调用 API
            log.debug("调用 {} HIS {}现金报表 API", apiType, method);
            ApiResponse<ApiResponseBodyList<T>> apiResponse = restApiUtil.postForObject(
                    URL_API_HISINCOME,
                    apiRequest,
                    typeRef
            );

            // 3. 检查业务状态码
            if (apiResponse.getResult().isSuccess()) {
                log.info("{} HIS API 调用成功，业务处理成功。", apiType);
                return Optional.ofNullable(apiResponse.getBody())
                        .map(ApiResponseBodyList::getList)
                        .orElse(Collections.emptyList());
            } else {
                String errMsg = String.format("%s HIS API 业务失败。Code: %s, Msg: %s",
                        apiType, apiResponse.getResult().getCode(), apiResponse.getResult().getMsg());
                log.error(errMsg);
                throw new BusinessException(errMsg);
            }

        } catch (BusinessException e) {
            log.error("{} HIS API 调用失败: {}", apiType, e.getMessage());
            // 根据业务需求，可以选择抛异常或返回空列表
            return Collections.emptyList();
        } catch (Exception e) {
            log.error("{} HIS API 调用发生未知异常: {}", apiType, e.getMessage(), e);
            return Collections.emptyList();
        }
    }
}
