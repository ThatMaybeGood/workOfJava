package com.showexcel.controller;

import com.showexcel.dto.CashStatisticsTableDTO;
import com.showexcel.model.CashStatistics;
import com.showexcel.dto.CashStatisticsDTO;
import com.showexcel.response.CashStatisticsResponse;
import com.showexcel.service.CashStatisticsNewService;
import com.showexcel.service.CashStatisticsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

@RestController
@RequestMapping("/api/cash-statistics")
@CrossOrigin(origins = "*")
@Slf4j
public class CashStatisticsController {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private CashStatisticsService cashStatisticsService;

    @Autowired
    private CashStatisticsNewService cashStatisticsNewService;


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


    @GetMapping("/table")
    public List<CashStatisticsTableDTO> getCashStatisticsTable() {
        List<CashStatisticsTableDTO> result = cashStatisticsService.getAllStatisticsTable();
        try {
            // 打印JSON串到控制台
            String jsonString = objectMapper.writeValueAsString(result);
            log.info("返回的JSON数据: {}", jsonString);
            System.out.println("=== JSON数据输出 ===");
            System.out.println(jsonString);
            System.out.println("===================");
        } catch (Exception e) {
            log.error("JSON序列化失败: {}", e.getMessage());
        }

        return result;
    }

    @GetMapping("/date/{date}")
    public CashStatisticsTableDTO getDataByDate(@PathVariable String date) {
        return cashStatisticsService.getAllStatisticsTableByDate(date);
    }


    @GetMapping("/datenew/{date}")
    public Result<CashStatisticsResponse> getDataByDateNew(@PathVariable String date) {
//        log.info(Result.success("调用接口成功"));

        return Result.success( cashStatisticsNewService.getAllStatisticsTableByDate(date));
    }



}