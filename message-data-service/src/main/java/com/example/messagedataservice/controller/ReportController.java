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
@RequestMapping()
@CrossOrigin(origins = "*")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @GetMapping("/data")
    public Result<List<ReportDTO>> getData(@RequestParam LocalDate reportDate) {
        if (reportDate == null) {
            return Result.error("报表日期不能为空");
        }

        if (reportService.getAll(reportDate) == null || reportService.getAll(reportDate).isEmpty() ) {
            return Result.error("查询失败");
        }
        return Result.success(reportService.getAll(reportDate));
    }


    @GetMapping("/")
    public Result<String> getHolidayApiInfo() {
        return Result.success("API服务已启动，可用端点：/R /data, /insert");
    }

}
