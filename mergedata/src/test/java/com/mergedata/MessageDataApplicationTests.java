package com.mergedata;

import com.mergedata.model.YQOperator;
import com.mergedata.server.HisDataService;
import com.mergedata.server.YQHolidayService;
import com.mergedata.server.YQCashService;
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

    @Autowired
    HisDataService hisDataService;

    @Autowired
    YQCashService yqCashService;

    @Autowired
    YQHolidayService yqHolidayService;

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


//        yqCashService.findByDate("2025-11-30");
//        hisOperatorService.batchInsert(operatorList);
//             hisOperatorService.findData()  ;

        yqHolidayService.findByDate("2025-11-25");
//            hisDataService.findByDate("2023-01-30");
//        externalApiRequestService.getHisIncomeList_String("2023-01-30");
//        log.info("测试通过");

    }

}
