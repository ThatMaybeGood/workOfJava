package com.mergedata;


import com.mergedata.model.dto.SpdPrescExpressRequest;
import com.mergedata.model.dto.SpdPrescExpressResponse;
import com.mergedata.server.impl.SpdApiServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest
class SpdApiServiceTest {

    @Autowired
    private SpdApiServiceImpl spdApiService;

    @Test
    void testUpdatePrescExpressStatus() {
        // 构建请求
        SpdPrescExpressRequest request = new SpdPrescExpressRequest();
        request.setStoreCode("410110");
        request.setPrescDate("2025-07-29 09:57:46");
        request.setPrescNo("16268");
        request.setIsExpress("1");

        // 调用接口
        SpdPrescExpressResponse response = spdApiService.updatePrescExpressStatus(request);

        // 输出结果
        log.info("调用结果：ResultInfo={}, ResultMessage={}",
                response.getResultInfo(),
                response.getResultMessage());

        // 断言
        assert response.isSuccess() : "接口调用应该成功";
    }

    @Test
    void testUpdateToExpress() {
        SpdPrescExpressResponse response = spdApiService.updateToExpress(
                "410110",
                "2025-07-29 09:57:46",
                "16269"
        );
        log.info("快递更新结果：{}", response.isSuccess());
    }

    @Test
    void testUpdateToPickup() {
        SpdPrescExpressResponse response = spdApiService.updateToPickup(
                "410110",
                "2025-07-29 09:57:46",
                "16270"
        );
        log.info("自取更新结果：{}", response.isSuccess());
    }
}