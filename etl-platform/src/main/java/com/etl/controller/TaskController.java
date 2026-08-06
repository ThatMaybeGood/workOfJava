package com.etl.controller;

import com.etl.dto.ApiResponse;
import com.etl.entity.EtlTaskConfig;
import com.etl.service.admin.EtlTaskConfigService;
import com.etl.service.core.EtlEngine;
import com.etl.service.core.TaskScheduler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/etl/task")
@Tag(name = "ETL任务管理", description = "ETL任务配置和执行管理")
public class TaskController {

    @Autowired
    private EtlTaskConfigService taskConfigService;

    @Autowired
    @Qualifier("etlTaskScheduler")
    private TaskScheduler taskScheduler;

    @Autowired
    private EtlEngine etlEngine;

    @PostMapping
    @Operation(summary = "新增任务")
    public ApiResponse<EtlTaskConfig> add(@RequestBody EtlTaskConfig task) {
        taskConfigService.save(task);
        return ApiResponse.success(task, "任务添加成功");
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新任务")
    public ApiResponse<EtlTaskConfig> update(@PathVariable Long id, @RequestBody EtlTaskConfig task) {
        task.setId(id);
        taskConfigService.updateById(task);
        return ApiResponse.success(task, "任务更新成功");
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除任务")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        taskConfigService.removeById(id);
        return ApiResponse.success("任务删除成功");
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取任务详情")
    public ApiResponse<EtlTaskConfig> getById(@PathVariable Long id) {
        return ApiResponse.success(taskConfigService.getById(id));
    }

    @GetMapping
    @Operation(summary = "获取所有任务")
    public ApiResponse<List<EtlTaskConfig>> list() {
        return ApiResponse.success(taskConfigService.list());
    }

    @PostMapping("/{taskCode}/execute")
    @Operation(summary = "手动执行任务")
    public ApiResponse<Void> execute(@PathVariable String taskCode) {
        etlEngine.execute(taskCode, "MANUAL", "admin");
        return ApiResponse.success("任务执行完成");
    }

    @PostMapping("/{taskCode}/schedule")
    @Operation(summary = "启动定时调度")
    public ApiResponse<Void> schedule(@PathVariable String taskCode) throws Exception {
        EtlTaskConfig task = taskConfigService.getByTaskCode(taskCode);
        if (task == null) {
            return ApiResponse.error("任务不存在");
        }
        taskScheduler.scheduleTask(task);
        return ApiResponse.success("定时任务已启动");
    }

    @PostMapping("/{taskCode}/pause")
    @Operation(summary = "暂停定时任务")
    public ApiResponse<Void> pause(@PathVariable String taskCode) throws Exception {
        taskScheduler.pauseTask(taskCode);
        return ApiResponse.success("定时任务已暂停");
    }

    @PostMapping("/{taskCode}/resume")
    @Operation(summary = "恢复定时任务")
    public ApiResponse<Void> resume(@PathVariable String taskCode) throws Exception {
        taskScheduler.resumeTask(taskCode);
        return ApiResponse.success("定时任务已恢复");
    }

    @PostMapping("/{taskCode}/remove-schedule")
    @Operation(summary = "移除定时调度")
    public ApiResponse<Void> removeSchedule(@PathVariable String taskCode) throws Exception {
        taskScheduler.removeTask(taskCode);
        return ApiResponse.success("定时任务已移除");
    }

    @PostMapping("/reload-schedules")
    @Operation(summary = "重新加载所有定时任务")
    public ApiResponse<Void> reloadSchedules() throws Exception {
        taskScheduler.reload();
        return ApiResponse.success("定时任务已重新加载");
    }

    @GetMapping("/{taskCode}/preview/{limit}")
    @Operation(summary = "预览任务数据")
    public ApiResponse<Void> preview(@PathVariable String taskCode, @PathVariable int limit) {
        etlEngine.executePreview(taskCode, limit);
        return ApiResponse.success("预览完成");
    }
}
