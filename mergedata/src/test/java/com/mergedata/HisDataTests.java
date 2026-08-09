package com.mergedata;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mergedata.constants.Constant;
import com.mergedata.model.dto.ApiRequest;
import com.mergedata.model.dto.ApiRequestHead;
import com.mergedata.model.dto.external.HisDataRequestBodyDTO;
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

    @Autowired
    private ApiRequestHead apiRequestHead; // 🔥 注入这个Bean

    // 🔥 创建ObjectMapper实例
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 🎯 新增测试方法：专门用于打印请求JSON格式
     */
    @Test
    void testPrintRequestJson() throws Exception {
        System.out.println("========== 开始打印请求JSON ==========");

        // 1. 构造门诊请求
        HisDataRequestBodyDTO comBody = new HisDataRequestBodyDTO();
        comBody.setReportDate("2026-08-09");

        ApiRequest<HisDataRequestBodyDTO> apiRequest = new ApiRequest<>();

        // 1.1 复制基础请求头配置
        ApiRequestHead headConfig = new ApiRequestHead();
        // 从注入的Bean复制属性（和Service里逻辑一致）
        org.springframework.beans.BeanUtils.copyProperties(apiRequestHead, headConfig);

        // 1.2 设置method（门诊）
        headConfig.setMethod(Constant.HIS_METHOD_OUTP);
        apiRequest.setHead(headConfig);
        apiRequest.setBody(comBody);

        // 2. 序列化为JSON并美化打印
        String requestJson = objectMapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(apiRequest);

        System.out.println("📤 请求URL: " + "你的HIS接口地址");
        System.out.println("📤 请求JSON:");
        System.out.println(requestJson);
        System.out.println("========== 打印完成 ==========");
    }

    /**
     * 🔥 新增测试方法：测试住院请求JSON
     */
    @Test
    void testPrintInpRequestJson() throws Exception {
        System.out.println("========== 开始打印住院请求JSON ==========");

        HisDataRequestBodyDTO comBody = new HisDataRequestBodyDTO();
        comBody.setReportDate("2026-08-09");

        ApiRequest<HisDataRequestBodyDTO> apiRequest = new ApiRequest<>();

        ApiRequestHead headConfig = new ApiRequestHead();
        org.springframework.beans.BeanUtils.copyProperties(apiRequestHead, headConfig);

        // 设置method为住院
        headConfig.setMethod(Constant.HIS_METHOD_INP);
        apiRequest.setHead(headConfig);
        apiRequest.setBody(comBody);

        String requestJson = objectMapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(apiRequest);

        System.out.println("📤 住院请求JSON:");
        System.out.println(requestJson);
        System.out.println("========== 打印完成 ==========");
    }

    /**
     * 原有的测试方法（保持不变）
     */
    @Test
    void contextLoads() {
        OperatorServiceImpl operatorService = new OperatorServiceImpl();

        String startDate = "2026-07-20";
        String endDate = "2026-07-20";

        try {
            java.time.LocalDate current = java.time.LocalDate.parse(startDate);
            java.time.LocalDate end = java.time.LocalDate.parse(endDate);

            while (!current.isAfter(end)) {
                String targetDate = current.toString();

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