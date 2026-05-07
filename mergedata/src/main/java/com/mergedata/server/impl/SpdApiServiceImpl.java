package com.mergedata.server.impl;


import com.mergedata.model.dto.SpdPrescExpressRequest;
import com.mergedata.model.dto.SpdPrescExpressResponse;
import com.mergedata.server.SpdApiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * SPD接口调用服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SpdApiServiceImpl  implements SpdApiService {

    private final RestTemplate restTemplate;

    @Value("${spd.api.url:http://168.168.230.220:8011/api/SpdOpWeb/outpDrugMasterStatus}")
    private String spdApiUrl;

    /**
     * 调用SPD接口更新处方物流方式
     *
     * @param request 请求实体
     * @return 响应实体
     */
    public SpdPrescExpressResponse updatePrescExpressStatus(SpdPrescExpressRequest request) {
        SpdPrescExpressResponse response = new SpdPrescExpressResponse();

        try {
            log.info("调用SPD接口更新处方物流方式，请求参数：{}", request);

            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // 封装请求实体
            HttpEntity<SpdPrescExpressRequest> httpEntity = new HttpEntity<>(request, headers);

            // 发送POST请求
            ResponseEntity<SpdPrescExpressResponse> responseEntity = restTemplate.exchange(
                    spdApiUrl,
                    HttpMethod.POST,
                    httpEntity,
                    SpdPrescExpressResponse.class
            );

            // 处理响应
            if (responseEntity.getStatusCode() == HttpStatus.OK) {
                response = responseEntity.getBody();
                if (response != null && response.isSuccess()) {
                    log.info("SPD接口调用成功，处方号：{}，物流方式：{}",
                            request.getPrescNo(),
                            "1".equals(request.getIsExpress()) ? "快递" : "自取");
                } else {
                    log.warn("SPD接口调用失败，响应：{}", response);
                }
            } else {
                response.setResultInfo("9999");
                response.setResultMessage("HTTP调用失败，状态码：" + responseEntity.getStatusCodeValue());
                log.error("SPD接口HTTP调用失败，状态码：{}", responseEntity.getStatusCodeValue());
            }

        } catch (Exception e) {
            log.error("调用SPD接口异常，处方号：{}", request.getPrescNo(), e);
            response.setResultInfo("9999");
            response.setResultMessage("系统异常：" + e.getMessage());
        }

        return response;
    }

    /**
     * 更新处方为快递
     */
    public SpdPrescExpressResponse updateToExpress(String storeCode, String prescDate, String prescNo) {
        SpdPrescExpressRequest request = new SpdPrescExpressRequest();
        request.setStoreCode(storeCode);
        request.setPrescDate(prescDate);
        request.setPrescNo(prescNo);
        request.setIsExpress("1");
        return updatePrescExpressStatus(request);
    }

    /**
     * 更新处方为自取
     */
    public SpdPrescExpressResponse updateToPickup(String storeCode, String prescDate, String prescNo) {
        SpdPrescExpressRequest request = new SpdPrescExpressRequest();
        request.setStoreCode(storeCode);
        request.setPrescDate(prescDate);
        request.setPrescNo(prescNo);
        request.setIsExpress("2");
        return updatePrescExpressStatus(request);
    }
}