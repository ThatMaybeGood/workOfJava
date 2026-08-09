package com.etl.controller;

import com.etl.dto.ApiResponse;
import com.etl.entity.EtlExecutionLog;
import com.etl.service.admin.DatasourceConfigService;
import com.etl.service.admin.EtlExecutionLogService;
import com.etl.service.admin.PipelineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/etl/monitor")
@Tag(name = "ETL监控", description = "执行日志和进度监控")
public class MonitorController {

    @Autowired
    private EtlExecutionLogService executionLogService;

    @Autowired
    private PipelineService pipelineService;

    @Autowired
    private DatasourceConfigService datasourceConfigService;

    @GetMapping("/logs/task/{taskCode}")
    @Operation(summary = "获取任务/管线的执行日志")
    public ApiResponse<List<EtlExecutionLog>> getLogsByTask(@PathVariable String taskCode) {
        return ApiResponse.success(executionLogService.listByTaskCode(taskCode));
    }

    @GetMapping("/logs/all")
    @Operation(summary = "获取全部执行日志（按开始时间倒序）")
    public ApiResponse<List<EtlExecutionLog>> getAllLogs(@RequestParam(required = false, defaultValue = "100") int limit) {
        return ApiResponse.success(executionLogService.listAll(limit));
    }

    @GetMapping("/logs/execution/{executionId}")
    @Operation(summary = "获取执行日志详情")
    public ApiResponse<EtlExecutionLog> getLogByExecutionId(@PathVariable String executionId) {
        return ApiResponse.success(executionLogService.getByExecutionId(executionId));
    }

    @GetMapping("/logs/running")
    @Operation(summary = "获取正在运行的任务/管线")
    public ApiResponse<List<EtlExecutionLog>> getRunningTasks() {
        return ApiResponse.success(executionLogService.listRunning());
    }

    @GetMapping("/dashboard")
    @Operation(summary = "获取监控仪表盘数据（管线维度）")
    public ApiResponse<Map<String, Object>> getDashboard() {
        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("totalTasks", pipelineService.count());
        dashboard.put("totalPipelines", pipelineService.count());
        dashboard.put("runningTasks", executionLogService.countByStatus("RUNNING"));
        dashboard.put("successTasks", executionLogService.countByStatus("SUCCESS"));
        dashboard.put("failedTasks", executionLogService.countByStatus("FAILED"));
        dashboard.put("totalDatasources", datasourceConfigService.count());
        return ApiResponse.success(dashboard);
    }
}
