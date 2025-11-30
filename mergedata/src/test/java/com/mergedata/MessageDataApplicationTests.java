package com.mergedata;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mergedata.model.ReportDTO;
import com.mergedata.server.HisDataService;
import com.mergedata.server.ReportService;
import com.mergedata.server.YQCashService;
import com.mergedata.server.YQHolidayService;
import com.mergedata.server.impl.ExternalApiRequestService;
import com.mergedata.server.impl.YQOperatorServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

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

    @Autowired
    ReportService reportService;

    @Test
    void contextLoads()  {


//        yqCashService.findByDate("2025-11-30");
//        hisOperatorService.batchInsert(operatorList);
//             hisOperatorService.findData()  ;

        List<ReportDTO> results = reportService.getAll("2025-11-20");
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonStringPretty = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(results);
            //打印results的json格式
            String jsonStringCompact = objectMapper.writeValueAsString(results);
            System.out.println("\n--- 美化 JSON 格式 ---");
            System.out.println(jsonStringCompact);
        } catch (JsonProcessingException e) {
        e.printStackTrace();
        System.err.println("JSON 序列化失败!");
    }
 //            hisDataService.findByDate("2023-01-30");
//        externalApiRequestService.getHisIncomeList_String("2023-01-30");

    }

}
