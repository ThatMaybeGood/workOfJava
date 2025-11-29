package com.mergedata;

import com.mergedata.model.YQOperator;
import com.mergedata.server.impl.ExternalApiRequestService;
import com.mergedata.server.impl.YQOperatorServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest
class MessageDataApplicationTests {

    @Autowired
    ExternalApiRequestService externalApiRequestService;

    @Autowired
    YQOperatorServiceImpl hisOperatorService;



    @Test
    void contextLoads() {
        //查询数据
//        hisOperatorService.findData();
        List<YQOperator> operatorList = new ArrayList<>();

        YQOperator y=new YQOperator();
        y.setOperatorNo("123");
        y.setOperatorName("张三");

        YQOperator y1=new YQOperator();
        y1.setOperatorNo("456");
        y1.setOperatorName("李四");

        operatorList.add(y1);

        operatorList.add(y);
        operatorList.add(y);

        hisOperatorService.batchInsert(operatorList);
        System.out.println("测试通过");

//        externalApiRequestService.getHisIncomeList("2023-01-30");
//        externalApiRequestService.getHisIncomeList_String("2023-01-30");
//        log.info("测试通过");

    }

}
