package com.mergedata;

import com.mergedata.model.dto.external.HisOutpIncomeResponseDTO;
import com.mergedata.server.HisDataService;
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
        List<HisOutpIncomeResponseDTO> outpData = hisDataService.findByDateOutp("2025-11-02");

//        List<HisInpIncomeResponseDTO> inpData = hisDataService.findByDateInp("2025-11-02");

         System.out.println("住院数据");
//         System.out.println(inpData.toString());

         System.out.println("门诊数据");
         System.out.println(outpData.toString());




    }
}
