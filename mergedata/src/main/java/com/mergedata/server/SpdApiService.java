package com.mergedata.server;

import com.mergedata.model.dto.SpdPrescExpressRequest;
import com.mergedata.model.dto.SpdPrescExpressResponse;

public interface SpdApiService {
    /*
     *  调用SPD接口更新处方物流方式
     *
     * @param request 请求实体
     * @return 响应实体
     */
    SpdPrescExpressResponse updatePrescExpressStatus(SpdPrescExpressRequest  request);
}
