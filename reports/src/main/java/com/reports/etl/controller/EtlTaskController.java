package com.reports.etl.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.reports.dto.common.ApiResponse;
import com.reports.etl.entity.*;
import com.reports.etl.mapper.*;
import com.reports.etl.service.core.EtlEngine;
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

    private final EtlTaskMapper taskMapper;
    private final EtlWsConfigMapper wsConfigMapper;
    private final EtlProcConfigMapper procConfigMapper;
    private final EtlMappingMapper mappingMapper;
    private final EtlTaskLogMapper logMapper;
    private final EtlEngine etlEngine;
    private final EtlScheduler scheduler;

    public EtlTaskController(EtlTaskMapper taskMapper, EtlWsConfigMapper wsConfigMapper,
                             EtlProcConfigMapper procConfigMapper, EtlMappingMapper mappingMapper,
                             EtlTaskLogMapper logMapper, EtlEngine etlEngine, EtlScheduler scheduler) {
        this.taskMapper = taskMapper;
        this.wsConfigMapper = wsConfigMapper;
        this.procConfigMapper = procConfigMapper;
        this.mappingMapper = mappingMapper;
        this.logMapper = logMapper;
        this.etlEngine = etlEngine;
        this.scheduler = scheduler;
    }

    @GetMapping("/list")
    public ApiResponse<?> list(@RequestParam(defaultValue = "1") int page,
                               @RequestParam(defaultValue = "20") int size) {
        Page<EtlTask> result = taskMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<EtlTask>().orderByDesc(EtlTask::getCreateTime));
        Map<String, Object> wrap = new LinkedHashMap<>();
        wrap.put("records", result.getRecords());
        wrap.put("total", result.getTotal());
        ApiResponse<Map<String, Object>> resp = ApiResponse.success(wrap);
        return resp;
    }

    @GetMapping("/{id}")
    public ApiResponse<EtlTask> get(@PathVariable Long id) {
        return ApiResponse.success(taskMapper.selectById(id));
    }

    @PostMapping
    public ApiResponse<String> add(@RequestBody EtlTask task) {
        taskMapper.insert(task);
        if ("WEBSERVICE".equals(task.getExtractType())) {
            EtlWsConfig wsConfig = buildWsConfig(task);
            wsConfig.setId(task.getId());
            wsConfigMapper.insert(wsConfig);
        } else if ("PROCEDURE".equals(task.getExtractType())) {
            EtlProcConfig procConfig = buildProcConfig(task);
            procConfig.setId(task.getId());
            procConfigMapper.insert(procConfig);
        }
        if ("1".equals(String.valueOf(task.getEnabled()))) {
            scheduler.scheduleTask(task);
        }
        return ApiResponse.success("任务创建成功");
    }

    @PutMapping("/{id}")
    public ApiResponse<String> update(@PathVariable Long id, @RequestBody EtlTask task) {
        task.setId(id);
        taskMapper.updateById(task);
        if ("1".equals(String.valueOf(task.getEnabled()))) {
            scheduler.scheduleTask(task);
        } else {
            scheduler.unscheduleTask(id);
        }
        return ApiResponse.success("任务更新成功");
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> delete(@PathVariable Long id) {
        taskMapper.deleteById(id);
        wsConfigMapper.deleteById(id);
        procConfigMapper.deleteById(id);
        mappingMapper.delete(new LambdaQueryWrapper<EtlMapping>().eq(EtlMapping::getTaskId, id));
        scheduler.unscheduleTask(id);
        return ApiResponse.success("任务删除成功");
    }

    @PostMapping("/{id}/run")
    public ApiResponse<EtlTaskLog> run(@PathVariable Long id) {
        return ApiResponse.success(scheduler.triggerManually(id, "MANUAL"));
    }

    @GetMapping("/{id}/logs")
    public ApiResponse<List<EtlTaskLog>> logs(@PathVariable Long id,
                                               @RequestParam(defaultValue = "1") int page,
                                               @RequestParam(defaultValue = "50") int size) {
        List<EtlTaskLog> list = logMapper.selectList(
                new LambdaQueryWrapper<EtlTaskLog>()
                        .eq(EtlTaskLog::getTaskId, id)
                        .orderByDesc(EtlTaskLog::getCreateTime)
                        .last("LIMIT " + size)
        );
        return ApiResponse.success(list != null ? list : java.util.Collections.emptyList());
    }

    private EtlWsConfig buildWsConfig(EtlTask task) {
        EtlWsConfig config = new EtlWsConfig();
        config.setTaskId(task.getId());
        config.setWsType("REST");
        config.setMaxPages(task.getMaxRows() != null ? task.getMaxRows() / 500 : 10);
        config.setMaxRows(task.getMaxRows());
        config.setBatchSize(task.getBatchSize());
        return config;
    }

    private EtlProcConfig buildProcConfig(EtlTask task) {
        EtlProcConfig config = new EtlProcConfig();
        config.setTaskId(task.getId());
        config.setProcName("");
        config.setMaxPages(task.getMaxRows() != null ? task.getMaxRows() / 500 : 10);
        config.setMaxRows(task.getMaxRows());
        config.setBatchSize(task.getBatchSize());
        return config;
    }
}