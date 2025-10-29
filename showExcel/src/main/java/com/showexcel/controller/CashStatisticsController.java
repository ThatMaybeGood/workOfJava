package com.showexcel.controller;

import com.showexcel.model.CashStatistics;
import com.showexcel.dao.CashStatisticsDTO;
import com.showexcel.server.CashStatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cash-statistics")
@CrossOrigin(origins = "*")
public class CashStatisticsController {

    @Autowired
    private CashStatisticsService cashStatisticsService;

    @GetMapping
    public List<CashStatisticsDTO> getAllStatistics() {
        return cashStatisticsService.getAllStatistics();
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
}