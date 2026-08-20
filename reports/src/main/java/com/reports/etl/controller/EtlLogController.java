package com.reports.etl.controller;

import com.reports.dto.common.ApiResponse;
import com.reports.etl.entity.EtlLogConfig;
import com.reports.etl.service.core.EtlMetaDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * ETL 日志管理：保留策略 + 执行历史/步骤日志查询
 */
@Slf4j
@RestController
@RequestMapping("/api/etl/log")
public class EtlLogController {

    private final EtlMetaDao metaDao;

    public EtlLogController(EtlMetaDao metaDao) {
        this.metaDao = metaDao;
    }

    /** 日志保留策略 */
    @GetMapping("/config")
    public ApiResponse<EtlLogConfig> config() {
        return ApiResponse.success(metaDao.getLogConfig());
    }

    /** 保存日志保留策略 */
    @PutMapping("/config")
    public ApiResponse<String> saveConfig(@RequestBody EtlLogConfig config) {
        metaDao.saveLogConfig(config);
        return ApiResponse.success("日志保留策略已保存");
    }

    /** 全任务执行历史（分页） */
    @GetMapping("/history")
    public ApiResponse<?> history(
            @RequestParam(required = false) Long taskId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "1000") int size,
            @RequestParam(required = false) String keyword) {
        List<com.reports.etl.entity.EtlTaskLog> records = metaDao.listTaskLogs(taskId, page, size);
        int total = metaDao.countTaskLogs(taskId);
        Map<String, Object> wrap = new java.util.LinkedHashMap<>();
        wrap.put("records", records);
        wrap.put("total", total);
        wrap.put("page", page);
        wrap.put("size", size);
        return ApiResponse.success(wrap);
    }

    /** 指定执行日志的步骤详情 */
    @GetMapping("/steps/{logId}")
    public ApiResponse<List<com.reports.etl.entity.EtlStepLog>> steps(@PathVariable Long logId) {
        return ApiResponse.success(metaDao.listStepLogs(logId));
    }
}