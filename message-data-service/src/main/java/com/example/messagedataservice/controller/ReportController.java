package com.example.messagedataservice.controller;

import com.example.messagedataservice.dto.ReportDTO;
import com.example.messagedataservice.server.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * @author Mine
 * @version 1.0
 * 描述:
 * @date 2025/11/10 17:03
 */

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @GetMapping("/data")
    public Result<List<ReportDTO>> getData(@PathVariable LocalDate reportDate) {

        return Result.success(reportService.getAll(reportDate));
    }

}
