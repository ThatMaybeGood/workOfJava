package com.mergedata;

import com.mergedata.server.HisDataService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class HisDataTests {

    @Autowired
    HisDataService hisDataService;

    @Test
    void contextLoads() {

//        List<HisIncome> byDate = hisDataService.findByDate("2023-01-01");
//        System.out.printf(byDate.toString());
    }
}
