package com.showexcel.controller;

import com.showexcel.dao.CashStatisticsRow;
import com.showexcel.dao.CashStatisticsTableDTO;
import com.showexcel.model.CashStatistics;
import com.showexcel.dao.CashStatisticsDTO;
import com.showexcel.model.CellMergeConfig;
import com.showexcel.server.CashStatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/cash-statistics")
@CrossOrigin(origins = "*")
public class CashStatisticsController {

    private static final Logger logger = LoggerFactory.getLogger(CashStatisticsController.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private CashStatisticsService cashStatisticsService;

    @GetMapping
    public List<CashStatisticsDTO> getAllStatistics() {
        List<CashStatisticsDTO> result = cashStatisticsService.getAllStatistics();

//        try {
//            // 打印JSON串到控制台
//            String jsonString = objectMapper.writeValueAsString(result);
//            logger.info("返回的JSON数据: {}", jsonString);
//            System.out.println("=== JSON数据输出 ===");
//            System.out.println(jsonString);
//            System.out.println("===================");
//        } catch (Exception e) {
//            logger.error("JSON序列化失败: {}", e.getMessage());
//        }

        return result;
    }


    @GetMapping("/type/{type}")
    public List<CashStatistics> getDataByType(@PathVariable Integer type) {
        return cashStatisticsService.getDataByType(type);
    }

    @GetMapping("/accounting")
    public List<CashStatistics> getAccountingData() {
        return cashStatisticsService.getAccountingData();
    }

    @GetMapping("/appointment")
    public List<CashStatistics> getAppointmentData() {
        return cashStatisticsService.getAppointmentData();
    }

    @GetMapping("/{id}")
    public CashStatistics getById(@PathVariable Integer id) {
        return cashStatisticsService.getById(id);
    }

    @PostMapping
    public CashStatistics addStatistics(@RequestBody CashStatistics item) {
        return cashStatisticsService.add(item);
    }

    @PutMapping("/{id}")
    public CashStatistics updateStatistics(@PathVariable Integer id, @RequestBody CashStatistics item) {
        return cashStatisticsService.update(id, item);
    }

    @DeleteMapping("/{id}")
    public boolean deleteStatistics(@PathVariable Integer id) {
        return cashStatisticsService.delete(id);
    }




    @GetMapping("/new")
    public List<CashStatisticsTableDTO> getCashStatisticsTable() {
        List<CashStatisticsTableDTO> result = cashStatisticsService.getAllStatisticsTable();
        try {
            // 打印JSON串到控制台
            String jsonString = objectMapper.writeValueAsString(result);
            logger.info("返回的JSON数据: {}", jsonString);
            System.out.println("=== JSON数据输出 ===");
            System.out.println(jsonString);
            System.out.println("===================");
        } catch (Exception e) {
            logger.error("JSON序列化失败: {}", e.getMessage());
        }

        return result;
    }

}