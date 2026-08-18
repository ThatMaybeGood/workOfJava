package com.reports.etl.controller;

import com.reports.dto.common.ApiResponse;
import com.reports.etl.entity.*;
import com.reports.etl.service.core.EtlEngine;
import com.reports.etl.service.core.EtlMetaDao;
import com.reports.etl.service.scheduler.EtlScheduler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/etl/task")
public class EtlTaskController {

    private final EtlMetaDao metaDao;
    private final EtlEngine etlEngine;
    private final EtlScheduler scheduler;

    public EtlTaskController(EtlMetaDao metaDao, EtlEngine etlEngine, EtlScheduler scheduler) {
        this.metaDao = metaDao;
        this.etlEngine = etlEngine;
        this.scheduler = scheduler;
    }

    @GetMapping("/list")
    public ApiResponse<?> list() {
        List<EtlTask> all = metaDao.listTasks();
        Map<String, Object> wrap = new LinkedHashMap<>();
        wrap.put("records", all);
        wrap.put("total", all.size());
        return ApiResponse.success(wrap);
    }

    @GetMapping("/{id}")
    public ApiResponse<EtlTask> get(@PathVariable Long id) {
        return ApiResponse.success(metaDao.getTask(id));
    }

    /** 任务详情（含关联抽取来源，供前端编辑回显） */
    @GetMapping("/{id}/detail")
    public ApiResponse<Map<String, Object>> detail(@PathVariable Long id) {
        EtlTask task = metaDao.getTask(id);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("task", task);
        EtlSource source = (task != null && task.getSourceId() != null)
                ? metaDao.getSource(task.getSourceId()) : null;
        result.put("source", source);
        result.put("mappings", metaDao.listMappings(id));
        return ApiResponse.success(result);
    }

    /**
     * 任务配置接口（Source 改造后：仅收 task 子对象，含 sourceId；
     * 旧请求体中的 wsConfig/procConfig 子对象兼容解析但忽略，不再写子表）
     * 请求体: { task: { ..., sourceId } }
     */
    @PostMapping
    public ApiResponse<Long> add(@RequestBody Map<String, Object> payload) {
        @SuppressWarnings("unchecked")
        Map<String, Object> taskMap = (Map<String, Object>) payload.getOrDefault("task", payload);
        EtlTask task = new com.fasterxml.jackson.databind.ObjectMapper()
                .configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .convertValue(taskMap, EtlTask.class);

        Long taskId = metaDao.insertTask(task);
        task.setId(taskId);

        // 注册调度
        if (task.getEnabled() != null && task.getEnabled() == 1) {
            scheduler.scheduleTask(metaDao.getTask(taskId));
        }
        return ApiResponse.success(taskId);
    }

    @PutMapping("/{id}")
    public ApiResponse<String> update(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        @SuppressWarnings("unchecked")
        Map<String, Object> taskMap = (Map<String, Object>) payload.getOrDefault("task", payload);
        EtlTask task = new com.fasterxml.jackson.databind.ObjectMapper()
                .configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .convertValue(taskMap, EtlTask.class);
        task.setId(id);
        metaDao.updateTask(task);

        // 重调度
        if (task.getEnabled() != null && task.getEnabled() == 1) {
            scheduler.scheduleTask(metaDao.getTask(id));
        } else {
            scheduler.unscheduleTask(id);
        }
        return ApiResponse.success("任务更新成功");
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> delete(@PathVariable Long id) {
        metaDao.deleteTask(id);
        metaDao.deleteWsConfigByTask(id);
        metaDao.deleteProcConfigByTask(id);
        metaDao.deleteMappingsByTask(id);
        scheduler.unscheduleTask(id);
        return ApiResponse.success("任务删除成功");
    }

    @PostMapping("/{id}/run")
    public ApiResponse<Map<String, Object>> run(@PathVariable Long id) {
        return ApiResponse.success(scheduler.triggerManually(id, "MANUAL"));
    }

    @GetMapping("/{id}/logs")
    public ApiResponse<List<EtlTaskLog>> logs(@PathVariable Long id,
                                               @RequestParam(defaultValue = "50") int size) {
        return ApiResponse.success(metaDao.listTaskLogs(id, size));
    }

    @GetMapping("/{id}/steps/{logId}")
    public ApiResponse<List<EtlStepLog>> steps(@PathVariable Long id, @PathVariable Long logId) {
        return ApiResponse.success(metaDao.listStepLogs(logId));
    }
}