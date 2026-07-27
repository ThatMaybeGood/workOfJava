package com.mergedata;

import com.mergedata.constants.Constant;
import com.mergedata.model.dto.external.HisInpIncomeResponseDTO;
import com.mergedata.model.dto.external.HisOutpIncomeResponseDTO;
import com.mergedata.model.entity.YQOperatorEntity;
import com.mergedata.server.HisDataService;
import com.mergedata.server.YQOperatorService;
import com.mergedata.server.impl.OperatorServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class HisDataTests {

    @Autowired
    HisDataService hisDataService;

    @Autowired
    YQOperatorService operatorService;

    @Test
    void contextLoads() {
//        List<HisOutpIncomeResponseDTO> outpData = hisDataService.findByDateOutp("2026-02-02");
//
////        List<HisInpIncomeResponseDTO> inpData = hisDataService.findByDateInp("2025-11-02");
////
////         System.out.println("住院数据");
////         System.out.println(inpData.toString());
//
//
//        for(HisOutpIncomeResponseDTO item:outpData) {
//            YQOperatorEntity operator = new YQOperatorEntity();
//
//            operator.setOperatorNo(item.getOperatorNo());
//            operator.setOperatorName(item.getOperatorName());
//            operator.setCategory("0");
//            operator.setDbUser(item.getDbUser());
//
//            operatorService.syncUpdate(operator);
//        }
//
//         System.out.println("门诊数据");
////         System.out.println(outpData.toString());



        OperatorServiceImpl operatorService = new OperatorServiceImpl();

        String startDate = "2026-07-20";
        String endDate = "2026-07-20";

        try {
            java.time.LocalDate current = java.time.LocalDate.parse(startDate);
            java.time.LocalDate end = java.time.LocalDate.parse(endDate);

            // 3. 循环判断：只要当前日期小于等于终止日期
            while (!current.isAfter(end)) {
                String targetDate = current.toString(); // 直接转为 "yyyy-MM-dd" 字符串

                // --- 原有的业务逻辑 ---
                List<HisOutpIncomeResponseDTO> outpData = hisDataService.findByDateOutp(targetDate);

                if (outpData != null && !outpData.isEmpty()) {
                    System.out.println("正在处理: " + targetDate + "，数量: " + outpData.size());
                    for (HisOutpIncomeResponseDTO item : outpData) {
                        YQOperatorEntity operator = new YQOperatorEntity();
                        operator.setOperatorNo(item.getDbUser());
                        operator.setOperatorName(item.getOperatorName());
                        operator.setCategory("0");
                        operator.setDbUser(item.getDbUser());
                        operatorService.syncUpdate(operator, Constant.TYPE_OUTP);
                    }
                } else {
                    System.out.println("日期: " + targetDate + " 无数据");
                }
                current = current.plusDays(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("全部处理完成");
    }
}
