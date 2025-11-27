package com.mergedata;

import com.mergedata.server.impl.ExternalApiRequestService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class MessageDataServiceApplicationTests {

    @Autowired
    ExternalApiRequestService externalApiRequestService;

    @Test
    void contextLoads() {

        externalApiRequestService.getHisIncomeList("2023-10-01");
//        log.info("测试通过");

    }

}
