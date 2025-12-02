package com.mergedata;

import com.mergedata.model.HisIncomeDTO;
import com.mergedata.server.HisDataService;
import com.mergedata.server.ReportService;
import com.mergedata.server.YQCashService;
import com.mergedata.server.YQHolidayService;
import com.mergedata.server.impl.YQOperatorServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class MessageDataApplicationTests {


    @Autowired
    YQOperatorServiceImpl hisOperatorService;

    @Autowired
    HisDataService hisDataService;

    @Autowired
    YQCashService yqCashService;

    @Autowired
    YQHolidayService yqHolidayService;

    @Autowired
    ReportService reportService;

    @Test
    void contextLoads()  {


        List<HisIncomeDTO> byDate = hisDataService.findByDate("2023-01-30");

    }

}
