package com.mergedata;

import com.mergedata.model.dto.external.HisInpIncomeResponseDTO;
import com.mergedata.model.dto.external.HisOutpIncomeResponseDTO;
import com.mergedata.model.entity.YQOperatorEntity;
import com.mergedata.server.HisDataService;
import com.mergedata.server.impl.OperatorServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class HisDataTests {

    @Autowired
    HisDataService hisDataService;

    @Test
    void contextLoads() {
        OperatorServiceImpl operatorService = new OperatorServiceImpl();
        List<HisOutpIncomeResponseDTO> outpData = hisDataService.findByDateOutp("2026-02-02");

//        List<HisInpIncomeResponseDTO> inpData = hisDataService.findByDateInp("2025-11-02");
//
//         System.out.println("住院数据");
//         System.out.println(inpData.toString());


        for(HisOutpIncomeResponseDTO item:outpData) {
            YQOperatorEntity operator = new YQOperatorEntity();

            operator.setOperatorNo(item.getOperatorNo());
            operator.setOperatorName(item.getOperatorName());
            operator.setCategory("0");
            operator.setDbUser(item.getDbUser());

            operatorService.syncUpdate(operator);
        }

         System.out.println("门诊数据");
//         System.out.println(outpData.toString());




    }
}
