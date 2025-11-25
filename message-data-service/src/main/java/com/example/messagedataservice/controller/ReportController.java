package com.example.messagedataservice.controller;

import com.example.messagedataservice.dto.ReportDTO;
import com.example.messagedataservice.server.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

import static com.example.messagedataservice.util.MultiFormatDateParser.parseMultiFormatDate;

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
    public Result<List<ReportDTO>> getData(@RequestParam String reportDate) {
        // 1. 尝试将字符串转换为 LocalDate，同时处理多种格式
        LocalDate parsedDate;
        try {
            // 使用之前讨论的多格式解析工具方法
            parsedDate = parseMultiFormatDate(reportDate);
        } catch (DateTimeParseException e) {
            // 如果解析失败，返回清晰的错误信息
            return Result.error("日期格式错误: " + reportDate + "，请使用支持的日期格式。");
        }

        // 2. 避免重复调用服务，并使用转换后的 LocalDate
        List<ReportDTO> resultList = reportService.getAll(parsedDate);

        // 3. 检查结果
        if (resultList == null || resultList.isEmpty()) {
            return Result.error("查询失败或无数据");
        }

        // 4. 返回结果
        return Result.success(resultList);
    }


    @GetMapping("/")
    public Result<String> getHolidayApiInfo() {
        return Result.success("API服务已启动，可用端点：/R /data, /insert");
    }

}
