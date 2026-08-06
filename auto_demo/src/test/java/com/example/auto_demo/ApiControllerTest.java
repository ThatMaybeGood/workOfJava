package com.example.auto_demo;

import com.alibaba.fastjson.JSONArray;
import com.example.auto_demo.logic.ApiLogic;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@SpringBootTest
public class ApiControllerTest {

    @Autowired
    private ApiLogic apiLogic;

    @Test
    public void testGetBillList() {
        String[] insuTypes = {"310", "3101", "390", "3901", "515253"};
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        String sessionId = System.currentTimeMillis() + "|";
        String fixmedinsCode = "TEST_CODE";
        String type = "1";

        for (int i = 14; i >= 0; i--) {
            String billDate = LocalDate.now().minusDays(i).format(formatter);

            for (String insuType : insuTypes) {
                try {
                    JSONArray list = apiLogic.getBillList(sessionId, fixmedinsCode, billDate, insuType, type);
                    System.out.println("insutype: " + insuType + ", 日期: " + billDate + ", 查询结果条数: " + list.size());
                } catch (Exception e) {
                    System.out.println("insutype: " + insuType + ", 日期: " + billDate + " 异常: " + e.getMessage());
                }
            }
        }
    }
}