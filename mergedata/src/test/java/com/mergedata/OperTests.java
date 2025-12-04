package com.mergedata;

import com.mergedata.server.impl.YQOperatorServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class OperTests {


    @Autowired
    YQOperatorServiceImpl yqOperatorService;

    @Test
    void contextLoads() {
//        yqOperatorService.findAll();
    }


}
