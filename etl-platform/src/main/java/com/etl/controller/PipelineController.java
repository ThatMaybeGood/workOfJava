package com.etl.controller;

import com.etl.dto.ApiResponse;
import com.etl.dto.DebugResult;
import com.etl.entity.*;
import com.etl.service.admin.*;
import com.etl.service.core.PipelineExecutor;
import com.etl.service.core.TaskScheduler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/etl/pipeline")
@Tag(name = "ETL管线管理", description = "Pipeline/Step/Edge 的 CRUD 和执行管理")
public class PipelineController {

    @Autowired
    private PipelineService pipelineService;

    @Autowired
    private StepService stepService;

    @Autowired
    private EdgeService edgeService;

    @Autowired
    private StepColumnMappingService stepColumnMappingService;

    @Autowired
    private PipelineExecutor pipelineExecutor;

    @Autowired
    @Qualifier("etlTaskScheduler")
    private TaskScheduler taskScheduler;

    @GetMapping
    @Operation(summary = "列出所有管线")
    public ApiResponse<List<Map<String, Object>>> list() {
        List<EtlPipeline> pipelines = pipelineService.list();
        List<Map<String, Object>> result = new ArrayList<>();
        for (EtlPipeline p : pipelines) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", p.getId());
            item.put("pipelineCode", p.getPipelineCode());
            item.put("pipelineName", p.getPipelineName());
            item.put("cronExpr", p.getCronExpr());
            item.put("enabled", p.getEnabled());
            item.put("priority", p.getPriority());
            item.put("description", p.getDescription());
            // 计算步骤数
            long stepCount = stepService.listByPipelineId(p.getId()).size();
            item.put("stepCount", stepCount);
            item.put("createdTime", p.getCreatedTime());
            item.put("updatedTime", p.getUpdatedTime());
            result.add(item);
        }
        return ApiResponse.success(result);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取管线详情（含步骤+连线）")
    public ApiResponse<Map<String, Object>> getById(@PathVariable Long id) {
        EtlPipeline pipeline = pipelineService.getById(id);
        if (pipeline == null) return ApiResponse.error("管线不存在");

        List<EtlPipelineStep> steps = stepService.listByPipelineId(id);
        List<EtlPipelineEdge> edges = edgeService.listByPipelineId(id);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("pipeline", pipeline);
        result.put("steps", steps);
        result.put("edges", edges);
        return ApiResponse.success(result);
    }

    @PostMapping
    @Operation(summary = "创建管线（含步骤+连线）")
    @Transactional
    public ApiResponse<Map<String, Object>> create(@RequestBody Map<String, Object> body) {
        // 解析管线
        EtlPipeline pipeline = new EtlPipeline();
        pipeline.setPipelineCode((String) body.get("pipelineCode"));
        pipeline.setPipelineName((String) body.get("pipelineName"));
        pipeline.setCronExpr((String) body.get("cronExpr"));
        pipeline.setRetryTimes((Integer) body.getOrDefault("retryTimes", 0));
        pipeline.setRetryInterval((Integer) body.getOrDefault("retryInterval", 60));
        pipeline.setEnabled((String) body.getOrDefault("enabled", "Y"));
        pipeline.setPriority((Integer) body.getOrDefault("priority", 5));
        pipeline.setDescription((String) body.get("description"));
        pipelineService.save(pipeline);

        Long pipelineId = pipeline.getId();

        // 解析步骤
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> stepList = (List<Map<String, Object>>) body.getOrDefault("steps", new ArrayList<>());
        Map<Object, Long> tempIdMap = new HashMap<>(); // 前端临时ID → 真实ID
        for (Map<String, Object> s : stepList) {
            EtlPipelineStep step = new EtlPipelineStep();
            step.setPipelineId(pipelineId);
            step.setStepCode((String) s.get("stepCode"));
            step.setStepName((String) s.get("stepName"));
            step.setStepType((String) s.get("stepType"));
            step.setStepSubType((String) s.get("stepSubType"));
            step.setOrderIndex((Integer) s.getOrDefault("orderIndex", 0));
            step.setSourceDsName((String) s.get("sourceDsName"));
            step.setSourceType((String) s.get("sourceType"));
            step.setSourceConfig((String) s.get("sourceConfig"));
            step.setTargetDsName((String) s.get("targetDsName"));
            step.setTargetConfig((String) s.get("targetConfig"));
            step.setWriteMode((String) s.get("writeMode"));
            step.setBatchSize((Integer) s.getOrDefault("batchSize", 2000));
            step.setTimeoutSeconds((Integer) s.getOrDefault("timeoutSeconds", 1800));
            step.setEnabled((String) s.getOrDefault("enabled", "Y"));
            step.setDescription((String) s.get("description"));
            stepService.save(step);

            Object tempId = s.get("_tempId");
            if (tempId != null) {
                tempIdMap.put(tempId.toString(), step.getId());
            }
        }

        // 解析连线
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> edgeList = (List<Map<String, Object>>) body.getOrDefault("edges", new ArrayList<>());
        for (Map<String, Object> e : edgeList) {
            EtlPipelineEdge edge = new EtlPipelineEdge();
            edge.setPipelineId(pipelineId);

            Object fromRaw = e.get("fromStepId");
            Object toRaw = e.get("toStepId");
            edge.setFromStepId(resolveStepId(fromRaw, tempIdMap));
            edge.setToStepId(resolveStepId(toRaw, tempIdMap));
            edge.setEdgeType((String) e.getOrDefault("edgeType", "PASS"));
            edge.setEdgeConfig((String) e.get("edgeConfig"));
            edgeService.save(edge);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("pipelineId", pipelineId);
        result.put("tempIdMap", tempIdMap);
        return ApiResponse.success(result, "管线创建成功");
    }

    private Long resolveStepId(Object val, Map<Object, Long> tempIdMap) {
        if (val == null) return null;
        if (val instanceof Number) {
            long num = ((Number) val).longValue();
            // 先尝试从临时ID映射中解析
            Long mapped = tempIdMap.get(String.valueOf(num));
            return mapped != null ? mapped : num;
        }
        return tempIdMap.get(val.toString());
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新管线（含步骤+连线，全量替换）")
    @Transactional
    public ApiResponse<Map<String, Object>> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        EtlPipeline pipeline = pipelineService.getById(id);
        if (pipeline == null) return ApiResponse.error("管线不存在");

        // 更新基本信息
        if (body.containsKey("pipelineCode")) pipeline.setPipelineCode((String) body.get("pipelineCode"));
        if (body.containsKey("pipelineName")) pipeline.setPipelineName((String) body.get("pipelineName"));
        if (body.containsKey("cronExpr")) pipeline.setCronExpr((String) body.get("cronExpr"));
        if (body.containsKey("enabled")) pipeline.setEnabled((String) body.get("enabled"));
        if (body.containsKey("description")) pipeline.setDescription((String) body.get("description"));
        pipelineService.updateById(pipeline);

        // 删除旧的步骤和连线，重新创建
        edgeService.deleteByPipelineId(id);
        // 清理旧步骤的映射
        List<EtlPipelineStep> oldSteps = stepService.listByPipelineId(id);
        for (EtlPipelineStep s : oldSteps) {
            stepColumnMappingService.remove(
                    new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<EtlStepColumnMapping>()
                            .eq("step_id", s.getId()));
        }
        stepService.deleteByPipelineId(id);

        // 重建步骤
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> stepList = (List<Map<String, Object>>) body.getOrDefault("steps", new ArrayList<>());
        Map<Object, Long> tempIdMap = new HashMap<>();
        for (Map<String, Object> s : stepList) {
            EtlPipelineStep step = new EtlPipelineStep();
            step.setPipelineId(id);
            step.setStepCode((String) s.get("stepCode"));
            step.setStepName((String) s.get("stepName"));
            step.setStepType((String) s.get("stepType"));
            step.setStepSubType((String) s.get("stepSubType"));
            step.setOrderIndex((Integer) s.getOrDefault("orderIndex", 0));
            step.setSourceDsName((String) s.get("sourceDsName"));
            step.setSourceType((String) s.get("sourceType"));
            step.setSourceConfig((String) s.get("sourceConfig"));
            step.setTargetDsName((String) s.get("targetDsName"));
            step.setTargetConfig((String) s.get("targetConfig"));
            step.setWriteMode((String) s.get("writeMode"));
            step.setBatchSize((Integer) s.getOrDefault("batchSize", 2000));
            step.setTimeoutSeconds((Integer) s.getOrDefault("timeoutSeconds", 1800));
            step.setEnabled((String) s.getOrDefault("enabled", "Y"));
            step.setDescription((String) s.get("description"));
            stepService.save(step);

            Object tempId = s.get("_tempId");
            if (tempId != null) {
                tempIdMap.put(tempId.toString(), step.getId());
            }
        }

        // 重建连线
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> edgeList = (List<Map<String, Object>>) body.getOrDefault("edges", new ArrayList<>());
        for (Map<String, Object> e : edgeList) {
            EtlPipelineEdge edge = new EtlPipelineEdge();
            edge.setPipelineId(id);
            edge.setFromStepId(resolveStepId(e.get("fromStepId"), tempIdMap));
            edge.setToStepId(resolveStepId(e.get("toStepId"), tempIdMap));
            edge.setEdgeType((String) e.getOrDefault("edgeType", "PASS"));
            edge.setEdgeConfig((String) e.get("edgeConfig"));
            if (edge.getFromStepId() != null && edge.getToStepId() != null) {
                edgeService.save(edge);
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("pipelineId", id);
        result.put("tempIdMap", tempIdMap);
        return ApiResponse.success(result, "管线更新成功");
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除管线（级联删除步骤+连线+映射）")
    @Transactional
    public ApiResponse<Void> delete(@PathVariable Long id) {
        // 级联删除步骤的映射
        List<EtlPipelineStep> steps = stepService.listByPipelineId(id);
        for (EtlPipelineStep step : steps) {
            stepColumnMappingService.remove(
                    new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<EtlStepColumnMapping>()
                            .eq("step_id", step.getId()));
        }
        edgeService.deleteByPipelineId(id);
        stepService.deleteByPipelineId(id);
        pipelineService.removeById(id);
        return ApiResponse.success("管线删除成功");
    }

    @PostMapping("/{pipelineCode}/execute")
    @Operation(summary = "手动执行管线")
    public ApiResponse<Void> execute(@PathVariable String pipelineCode) {
        DebugResult result = pipelineExecutor.debug(pipelineCode, 0, true);
        if (result == null || !"SUCCESS".equals(result.getStatus())) {
            return ApiResponse.error("管线执行失败: " + (result != null ? result.getErrorMessage() : "未知错误"));
        }
        return ApiResponse.success("管线执行完成");
    }

    @PostMapping("/{pipelineCode}/debug")
    @Operation(summary = "分步调试管线")
    public ApiResponse<DebugResult> debug(@PathVariable String pipelineCode,
                                          @RequestParam(required = false, defaultValue = "100") int limit,
                                          @RequestParam(required = false, defaultValue = "false") boolean write) {
        DebugResult result = pipelineExecutor.debug(pipelineCode, limit, write);
        return ApiResponse.success(result, "调试执行完成");
    }

    @PostMapping("/{pipelineCode}/debug/extract")
    @Operation(summary = "仅调试抽取步骤")
    public ApiResponse<DebugResult> debugExtract(@PathVariable String pipelineCode,
                                                  @RequestParam(required = false, defaultValue = "100") int limit) {
        DebugResult result = pipelineExecutor.debug(pipelineCode, limit, false);
        return ApiResponse.success(result, "抽取调试完成");
    }

    @GetMapping(value = "/{pipelineCode}/debug/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "流式调试管线（SSE实时推送每步结果）")
    public SseEmitter debugStream(@PathVariable String pipelineCode,
                                  @RequestParam(required = false, defaultValue = "100") int limit,
                                  @RequestParam(required = false, defaultValue = "false") boolean write) {
        SseEmitter emitter = new SseEmitter(3600000L); // 1小时超时
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                pipelineExecutor.debugStream(pipelineCode, limit, write, event -> {
                    try {
                        emitter.send(SseEmitter.event()
                                .name((String) event.get("event"))
                                .data(event, MediaType.APPLICATION_JSON));
                        if ("done".equals(event.get("event")) || "error".equals(event.get("event"))) {
                            emitter.complete();
                        }
                    } catch (Exception e) {
                        log.error("SSE 推送失败", e);
                        emitter.completeWithError(e);
                    }
                });
            } catch (Exception e) {
                log.error("流式执行异常", e);
                emitter.completeWithError(e);
            }
        });
        emitter.onCompletion(() -> executor.shutdown());
        emitter.onTimeout(() -> { executor.shutdown(); emitter.complete(); });
        emitter.onError(e -> { executor.shutdown(); });
        return emitter;
    }

    @PostMapping("/{pipelineCode}/schedule")
    @Operation(summary = "启动管线定时调度")
    public ApiResponse<Void> schedule(@PathVariable String pipelineCode) {
        EtlPipeline pipeline = pipelineService.getByPipelineCode(pipelineCode);
        if (pipeline == null) return ApiResponse.error("管线不存在");
        try {
            taskScheduler.schedulePipeline(pipeline);
        } catch (Exception e) {
            return ApiResponse.error("调度失败: " + e.getMessage());
        }
        return ApiResponse.success("定时调度已启动");
    }

    @PostMapping("/{pipelineCode}/pause")
    @Operation(summary = "暂停管线调度")
    public ApiResponse<Void> pause(@PathVariable String pipelineCode) {
        try {
            taskScheduler.pausePipeline(pipelineCode);
        } catch (Exception e) {
            return ApiResponse.error("暂停失败: " + e.getMessage());
        }
        return ApiResponse.success("调度已暂停");
    }

    @PostMapping("/{pipelineCode}/resume")
    @Operation(summary = "恢复管线调度")
    public ApiResponse<Void> resume(@PathVariable String pipelineCode) {
        try {
            taskScheduler.resumePipeline(pipelineCode);
        } catch (Exception e) {
            return ApiResponse.error("恢复失败: " + e.getMessage());
        }
        return ApiResponse.success("调度已恢复");
    }

    @PostMapping("/{pipelineCode}/remove-schedule")
    @Operation(summary = "移除管线调度")
    public ApiResponse<Void> removeSchedule(@PathVariable String pipelineCode) {
        try {
            taskScheduler.removePipeline(pipelineCode);
        } catch (Exception e) {
            return ApiResponse.error("移除失败: " + e.getMessage());
        }
        return ApiResponse.success("调度已移除");
    }

    @PostMapping("/reload-schedules")
    @Operation(summary = "重新加载所有定时调度")
    public ApiResponse<Void> reloadSchedules() {
        try {
            taskScheduler.reload();
        } catch (Exception e) {
            return ApiResponse.error("重载失败: " + e.getMessage());
        }
        return ApiResponse.success("定时任务已重新加载");
    }
}
