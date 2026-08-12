package com.etl.service.core;

import com.etl.dto.DebugResult;
import com.etl.dto.StepConfig;
import com.etl.dto.StepResult;
import com.etl.entity.*;
import com.etl.service.admin.*;
import com.etl.service.reader.DataSourceReader;
import com.etl.service.reader.ReaderFactory;
import com.etl.service.writer.DataWriter;
import com.etl.service.writer.WriterFactory;
import com.etl.util.PrimaryKeyGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Pipeline 执行引擎：加载管线的步骤和连线，拓扑排序后按 DAG 顺序执行。
 * 替代旧版硬编码 3 步的 StepEngine。
 */
@Slf4j
@Component
public class PipelineExecutor {

    @Autowired
    private PipelineService pipelineService;

    @Autowired
    private StepService stepService;

    @Autowired
    private EdgeService edgeService;

    @Autowired
    private StepColumnMappingService stepColumnMappingService;

    @Autowired
    private EtlExecutionLogService executionLogService;

    @Autowired
    private ReaderFactory readerFactory;

    @Autowired
    private WriterFactory writerFactory;

    @Autowired
    private DataSourceManager dataSourceManager;

    /**
     * 分步调试执行管线。
     *
     * @param pipelineCode 管线编码
     * @param limit        抽取行数上限（<=0 表示不限）
     * @param write        是否执行 LOAD 步骤
     */
    public DebugResult debug(String pipelineCode, int limit, boolean write) {
        long start = System.currentTimeMillis();
        String executionId = "DEBUG_" + PrimaryKeyGenerator.nextIdStr();
        DebugResult result = new DebugResult();
        result.setTaskCode(pipelineCode);
        result.setExecutionId(executionId);
        result.setStatus("SUCCESS");
        List<StepResult> steps = new ArrayList<>();
        EtlPipeline pipeline = null;

        try {
            pipeline = pipelineService.getByPipelineCode(pipelineCode);
            if (pipeline == null) {
                throw new RuntimeException("管线不存在: " + pipelineCode);
            }
            result.setTaskName(pipeline.getPipelineName());

            List<EtlPipelineStep> stepList = stepService.listByPipelineId(pipeline.getId());
            if (stepList.isEmpty()) {
                result.setStatus("FAILED");
                result.setErrorMessage("管线没有配置步骤");
                result.setSteps(steps);
                result.setTotalDurationMs(System.currentTimeMillis() - start);
                recordExecution(pipeline, executionId, start, result.getStatus(), result.getErrorMessage(), steps);
                return result;
            }

            List<EtlPipelineEdge> edgeList = edgeService.listByPipelineId(pipeline.getId());

            // 拓扑排序
            List<EtlPipelineStep> sorted = topologicalSort(stepList, edgeList);
            if (sorted == null) {
                result.setStatus("FAILED");
                result.setErrorMessage("管线中存在循环依赖，无法执行");
                result.setSteps(steps);
                result.setTotalDurationMs(System.currentTimeMillis() - start);
                recordExecution(pipeline, executionId, start, result.getStatus(), result.getErrorMessage(), steps);
                return result;
            }

            // 若管线可流式执行（线性 EXTRACT→FIELD_MAP→LOAD、上游 reader 支持），走 chunk 边读边写
            if (canStreamPipeline(sorted, edgeList) && limit <= 0) {
                List<StepResult> streamed = executeStreaming(sorted, edgeList, write, null);
                steps.addAll(streamed);
                for (StepResult sr : streamed) {
                    if (!"SUCCESS".equals(sr.getStatus()) && !"SKIPPED".equals(sr.getStatus())) {
                        result.setStatus("FAILED");
                        result.setErrorMessage(sr.getErrorMessage());
                        break;
                    }
                }
            } else {
                // 步骤输出缓存：stepId → 数据集
                Map<Long, List<Map<String, Object>>> stepOutputs = new HashMap<>();

                for (EtlPipelineStep step : sorted) {
                    StepResult stepResult = executeStep(step, edgeList, stepOutputs, limit, write);
                    steps.add(stepResult);

                    if (!"SUCCESS".equals(stepResult.getStatus())) {
                        // 检查下游步骤是否允许失败继续
                        boolean hasFailTolerant = edgeList.stream()
                                .anyMatch(e -> e.getFromStepId().equals(step.getId()) && "FAIL_CONTINUE".equals(e.getEdgeType()));
                        if (!hasFailTolerant) {
                            result.setStatus("FAILED");
                            result.setErrorMessage(stepResult.getErrorMessage());
                            break;
                        }
                    }
                }
            }

        } catch (Exception e) {
            log.error("管线 [{}] 调试执行失败", pipelineCode, e);
            result.setStatus("FAILED");
            result.setErrorMessage(e.getMessage());
        }

        result.setSteps(steps);
        result.setTotalDurationMs(System.currentTimeMillis() - start);
        recordExecution(pipeline, executionId, start, result.getStatus(), result.getErrorMessage(), steps);
        return result;
    }

    /**
     * 记录一次管线执行日志（复用 etl_execution_log 表，task_code 存管线编码）。
     */
    private void recordExecution(EtlPipeline pipeline, String executionId, long startMs,
                                 String status, String errorMessage, List<StepResult> steps) {
        try {
            EtlExecutionLog log = new EtlExecutionLog();
            log.setTaskCode(pipeline != null ? pipeline.getPipelineCode() : executionId);
            log.setTaskName(pipeline != null ? pipeline.getPipelineName() : null);
            log.setExecutionId(executionId);
            long durationMs = System.currentTimeMillis() - startMs;
            log.setStartTime(LocalDateTime.now().minusNanos(durationMs * 1_000_000L));
            log.setEndTime(LocalDateTime.now());
            log.setStatus(status);
            log.setTriggerType("DEBUG");
            log.setTriggerUser("admin");
            log.setErrorMessage(errorMessage);
            log.setExecutionDuration(durationMs);
            long totalRows = 0;
            if (steps != null) {
                for (StepResult sr : steps) {
                    totalRows += sr.getOutputRows();
                }
            }
            log.setTotalRows(totalRows);
            log.setSuccessRows("SUCCESS".equals(status) ? totalRows : 0);
            log.setFailedRows("FAILED".equals(status) ? totalRows : 0);
            executionLogService.save(log);
        } catch (Exception e) {
            log.warn("记录管线执行日志失败: {}", e.getMessage());
        }
    }

    /**
     * 流式调试执行：每完成一个步骤就回调一次，用于 SSE 实时推送。
     * 回调按阶段发送 event: "init"(总步骤数), "step"(每一步完成), "done"(全部完成)。
     */
    public void debugStream(String pipelineCode, int limit, boolean write,
                            Consumer<Map<String, Object>> onEvent) {
        long start = System.currentTimeMillis();
        String executionId = "DEBUG_" + PrimaryKeyGenerator.nextIdStr();
        EtlPipeline pipeline = null;
        List<StepResult> stepResults = new ArrayList<>();

        try {
            pipeline = pipelineService.getByPipelineCode(pipelineCode);
            if (pipeline == null) {
                sendEvent(onEvent, "error", newMap("message", "管线不存在:" + pipelineCode));
                return;
            }

            List<EtlPipelineStep> stepList = stepService.listByPipelineId(pipeline.getId());
            if (stepList.isEmpty()) {
                sendEvent(onEvent, "error", newMap("message", "管线没有配置步骤"));
                return;
            }

            List<EtlPipelineEdge> edgeList = edgeService.listByPipelineId(pipeline.getId());
            List<EtlPipelineStep> sorted = topologicalSort(stepList, edgeList);
            if (sorted == null) {
                sendEvent(onEvent, "error", newMap("message", "管线中存在循环依赖"));
                return;
            }

            // 发送初始事件：管线名 + 总步骤信息
            List<Map<String, Object>> stepInfos = new ArrayList<>();
            for (EtlPipelineStep s : sorted) {
                Map<String, Object> info = new LinkedHashMap<>();
                info.put("stepId", s.getId());
                info.put("stepName", s.getStepName());
                info.put("stepType", s.getStepType());
                info.put("orderIndex", s.getOrderIndex());
                stepInfos.add(info);
            }
            Map<String, Object> initEvent = new LinkedHashMap<>();
            initEvent.put("executionId", executionId);
            initEvent.put("pipelineName", pipeline.getPipelineName());
            initEvent.put("totalSteps", sorted.size());
            initEvent.put("steps", stepInfos);
            sendEvent(onEvent, "init", initEvent);

            // 流式判定：若可流式则走 chunk 边读边写路径，并推送 chunk_progress 事件
            if (canStreamPipeline(sorted, edgeList) && limit <= 0) {
                // 流式 chunk 路径
                List<StepResult> streamed = executeStreaming(sorted, edgeList, write, onEvent);
                stepResults.addAll(streamed);
                boolean streamFailed = false;
                for (StepResult sr : streamed) {
                    Map<String, Object> stepEvt = new LinkedHashMap<>();
                    stepEvt.put("stepId", sr.getStepId());
                    stepEvt.put("stepOrder", sr.getStepOrder());
                    stepEvt.put("stepType", sr.getStepType());
                    stepEvt.put("stepName", sr.getStepName());
                    stepEvt.put("status", sr.getStatus());
                    stepEvt.put("inputRows", sr.getInputRows());
                    stepEvt.put("outputRows", sr.getOutputRows());
                    stepEvt.put("durationMs", sr.getDurationMs());
                    stepEvt.put("errorMessage", sr.getErrorMessage());
                    stepEvt.put("outputColumns", sr.getOutputColumns());
                    stepEvt.put("outputData", sr.getOutputData());
                    stepEvt.put("rawResponse", sr.getRawResponse());
                    stepEvt.put("statusCode", sr.getStatusCode());
                    stepEvt.put("responseHeaders", sr.getResponseHeaders());
                    stepEvt.put("finalUrl", sr.getFinalUrl());
                    stepEvt.put("finalMethod", sr.getFinalMethod());
                    sendEvent(onEvent, "step", stepEvt);

                    if (!"SUCCESS".equals(sr.getStatus()) && !"SKIPPED".equals(sr.getStatus())) {
                        streamFailed = true;
                        recordExecution(pipeline, executionId, start, "FAILED", sr.getErrorMessage(), stepResults);
                        sendEvent(onEvent, "done", newMap(
                                "status", "FAILED",
                                "totalDurationMs", System.currentTimeMillis() - start,
                                "errorMessage", sr.getErrorMessage()));
                        return;
                    }
                }

                String finalStatus = streamFailed ? "FAILED" : "SUCCESS";
                recordExecution(pipeline, executionId, start, finalStatus, null, stepResults);
                sendEvent(onEvent, "done", newMap(
                        "status", finalStatus,
                        "totalDurationMs", System.currentTimeMillis() - start));
                return;
            }

            // 逐步执行并实时推送
            Map<Long, List<Map<String, Object>>> stepOutputs = new HashMap<>();
            boolean failed = false;

            for (EtlPipelineStep step : sorted) {
                // 发送"运行中"状态
                Map<String, Object> runningEvt = new LinkedHashMap<>();
                runningEvt.put("stepId", step.getId());
                runningEvt.put("stepName", step.getStepName());
                runningEvt.put("status", "RUNNING");
                sendEvent(onEvent, "step_start", runningEvt);

                StepResult stepResult = executeStep(step, edgeList, stepOutputs, limit, write);
                stepResults.add(stepResult);

                // 构建步骤结果事件
                Map<String, Object> stepEvt = new LinkedHashMap<>();
                stepEvt.put("stepId", step.getId());
                stepEvt.put("stepOrder", stepResult.getStepOrder());
                stepEvt.put("stepType", stepResult.getStepType());
                stepEvt.put("stepName", stepResult.getStepName());
                stepEvt.put("status", stepResult.getStatus());
                stepEvt.put("inputRows", stepResult.getInputRows());
                stepEvt.put("outputRows", stepResult.getOutputRows());
                stepEvt.put("durationMs", stepResult.getDurationMs());
                stepEvt.put("errorMessage", stepResult.getErrorMessage());
                stepEvt.put("outputColumns", stepResult.getOutputColumns());
                stepEvt.put("outputData", stepResult.getOutputData());
                stepEvt.put("rawResponse", stepResult.getRawResponse());
                stepEvt.put("statusCode", stepResult.getStatusCode());
                stepEvt.put("responseHeaders", stepResult.getResponseHeaders());
                stepEvt.put("finalUrl", stepResult.getFinalUrl());
                stepEvt.put("finalMethod", stepResult.getFinalMethod());
                sendEvent(onEvent, "step", stepEvt);

                if (!"SUCCESS".equals(stepResult.getStatus())) {
                    boolean hasFailTolerant = edgeList.stream()
                            .anyMatch(e -> e.getFromStepId().equals(step.getId()) && "FAIL_CONTINUE".equals(e.getEdgeType()));
                    if (!hasFailTolerant) {
                        failed = true;
                        recordExecution(pipeline, executionId, start, "FAILED", stepResult.getErrorMessage(), stepResults);
                        sendEvent(onEvent, "done", newMap(
                                "status", "FAILED",
                                "totalDurationMs", System.currentTimeMillis() - start,
                                "errorMessage", stepResult.getErrorMessage()));
                        return;
                    }
                }
            }

            String finalStatus = failed ? "FAILED" : "SUCCESS";
            recordExecution(pipeline, executionId, start, finalStatus, null, stepResults);
            sendEvent(onEvent, "done", newMap(
                    "status", finalStatus,
                    "totalDurationMs", System.currentTimeMillis() - start));

        } catch (Exception e) {
            log.error("管线 [{}] 流式执行失败", pipelineCode, e);
            recordExecution(pipeline, executionId, start, "FAILED", e.getMessage(), stepResults);
            sendEvent(onEvent, "error", newMap("message", e.getMessage()));
            sendEvent(onEvent, "done", newMap("status", "FAILED", "errorMessage", e.getMessage()));
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void sendEvent(Consumer<Map<String, Object>> onEvent, String eventName, Map data) {
        Map<String, Object> event = new LinkedHashMap<>(data);
        event.put("event", eventName);
        onEvent.accept(event);
    }

    /** 快速构建 mutable Map，替代 Map.of() */
    private static Map<String, Object> newMap(String k1, Object v1) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put(k1, v1);
        return m;
    }

    private static Map<String, Object> newMap(String k1, Object v1, String k2, Object v2) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put(k1, v1);
        m.put(k2, v2);
        return m;
    }

    private static Map<String, Object> newMap(String k1, Object v1, String k2, Object v2,
                                               String k3, Object v3) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put(k1, v1);
        m.put(k2, v2);
        m.put(k3, v3);
        return m;
    }

    /**
     * 执行单个步骤。
     */
    private StepResult executeStep(EtlPipelineStep step, List<EtlPipelineEdge> edges,
                                    Map<Long, List<Map<String, Object>>> stepOutputs,
                                    int limit, boolean write) {
        long s = System.currentTimeMillis();
        StepResult result = new StepResult();
        result.setStepOrder(step.getOrderIndex() != null ? step.getOrderIndex() : 0);
        result.setStepType(step.getStepType());
        result.setStepName(step.getStepName());
        result.setNextOnSuccess(true);

        try {
            StepConfig config = StepConfig.fromStep(step);

            switch (step.getStepType()) {
                case "EXTRACT":
                    executeExtractStep(config, result, limit);
                    break;
                case "TRANSFORM":
                    executeTransformStep(config, step, edges, stepOutputs, result);
                    break;
                case "LOAD":
                    if (write) {
                        executeLoadStep(config, step, edges, stepOutputs, result);
                    } else {
                        result.setStatus("SKIPPED");
                        result.setErrorMessage("调试模式跳过写入");
                    }
                    break;
                default:
                    result.setStatus("FAILED");
                    result.setErrorMessage("未知步骤类型: " + step.getStepType());
            }

            // 缓存当前步骤的输出
            if ("SUCCESS".equals(result.getStatus()) && result.getOutputData() != null) {
                stepOutputs.put(step.getId(), result.getOutputData());
            }

        } catch (Exception e) {
            log.error("步骤 [{}] 执行失败", step.getStepName(), e);
            result.setStatus("FAILED");
            result.setErrorMessage(e.getMessage());
        }

        result.setDurationMs(System.currentTimeMillis() - s);
        return result;
    }

    /** 抽取步骤 */
    private void executeExtractStep(StepConfig config, StepResult result, int limit) {
        DataSourceReader reader = readerFactory.getReader(config.getSourceType());
        reader.initWithConfig(config, dataSourceManager);

        List<Map<String, Object>> data = (limit > 0) ? reader.preview(limit) : reader.readAll();

        // 捕获 HTTP/SOAP 原始响应
        if (reader instanceof com.etl.service.reader.HttpReader) {
            com.etl.service.reader.HttpReader hr = (com.etl.service.reader.HttpReader) reader;
            result.setRawResponse(hr.getLastRawResponse());
            result.setStatusCode(hr.getLastStatusCode());
            result.setResponseHeaders(hr.getLastResponseHeaders());
            result.setFinalUrl(hr.getLastRequestUrl());
            result.setFinalMethod(hr.getLastRequestMethod());
        }

        result.setStatus("SUCCESS");
        result.setOutputRows(data.size());
        result.setOutputColumns(collectColumns(data));
        result.setOutputData(data);
    }

    /** 转换步骤 */
    private void executeTransformStep(StepConfig config, EtlPipelineStep step,
                                       List<EtlPipelineEdge> edges,
                                       Map<Long, List<Map<String, Object>>> stepOutputs,
                                       StepResult result) {
        String subType = step.getStepSubType();
        if (subType == null) subType = "FIELD_MAP";

        // 查找所有输入到当前步骤的上游
        List<EtlPipelineEdge> incoming = edges.stream()
                .filter(e -> e.getToStepId().equals(step.getId()))
                .collect(Collectors.toList());

        if (incoming.isEmpty()) {
            result.setStatus("FAILED");
            result.setErrorMessage("转换步骤没有上游数据来源");
            return;
        }

        // 收集上游数据
        List<Map<String, Object>> allInputData;
        if (incoming.size() == 1) {
            allInputData = stepOutputs.getOrDefault(incoming.get(0).getFromStepId(), new ArrayList<>());
        } else {
            allInputData = new ArrayList<>();
            for (EtlPipelineEdge edge : incoming) {
                List<Map<String, Object>> upstream = stepOutputs.getOrDefault(edge.getFromStepId(), new ArrayList<>());
                allInputData.addAll(upstream);
            }
        }

        if (allInputData.isEmpty()) {
            result.setStatus("SUCCESS");
            result.setInputRows(0);
            result.setOutputRows(0);
            result.setOutputColumns(new ArrayList<>());
            result.setOutputData(new ArrayList<>());
            return;
        }

        result.setInputRows(allInputData.size());

        List<Map<String, Object>> transformed;
        switch (subType) {
            case "FIELD_MAP":
                List<EtlStepColumnMapping> mappings = stepColumnMappingService.listByStepId(step.getId());
                transformed = applyFieldMapping(allInputData, mappings);
                break;
            case "JOIN":
                transformed = applyJoin(stepOutputs, incoming, edges);
                break;
            case "UNION":
                transformed = applyUnion(stepOutputs, incoming);
                break;
            default:
                transformed = allInputData;
        }

        result.setStatus("SUCCESS");
        result.setOutputRows(transformed.size());
        result.setOutputColumns(collectColumns(transformed));
        result.setOutputData(transformed);
    }

    /** 加载步骤 */
    private void executeLoadStep(StepConfig config, EtlPipelineStep step,
                                  List<EtlPipelineEdge> edges,
                                  Map<Long, List<Map<String, Object>>> stepOutputs,
                                  StepResult result) {
        // 找到上游数据
        List<EtlPipelineEdge> incoming = edges.stream()
                .filter(e -> e.getToStepId().equals(step.getId()))
                .collect(Collectors.toList());

        if (incoming.isEmpty()) {
            result.setStatus("FAILED");
            result.setErrorMessage("加载步骤没有上游数据来源");
            return;
        }

        List<Map<String, Object>> data = new ArrayList<>();
        for (EtlPipelineEdge edge : incoming) {
            List<Map<String, Object>> upstream = stepOutputs.getOrDefault(edge.getFromStepId(), new ArrayList<>());
            data.addAll(upstream);
        }

        result.setInputRows(data.size());
        if (data.isEmpty()) {
            result.setStatus("SUCCESS");
            result.setOutputRows(0);
            return;
        }

        String subType = step.getStepSubType();
        if (subType == null || "DB_INSERT".equals(subType) || "DB_MERGE".equals(subType)) {
            // 数据库写入
            String mode = config.getWriteMode();
            if (mode == null) mode = "INSERT";
            DataWriter writer = writerFactory.getWriter(mode);
            List<EtlStepColumnMapping> mappings = stepColumnMappingService.listByStepId(step.getId());
            writer.writeWithConfig(data, config, mappings, dataSourceManager);
        } else if (subType != null && subType.startsWith("FILE_")) {
            // 文件写入
            DataWriter writer = writerFactory.getWriter(subType);
            writer.writeWithConfig(data, config, null, dataSourceManager);
        }

        result.setStatus("SUCCESS");
        result.setOutputRows(data.size());
    }

    /** 拓扑排序（Kahn 算法） */
    private List<EtlPipelineStep> topologicalSort(List<EtlPipelineStep> steps, List<EtlPipelineEdge> edges) {
        Map<Long, EtlPipelineStep> stepMap = steps.stream()
                .collect(Collectors.toMap(EtlPipelineStep::getId, s -> s));
        Map<Long, Integer> inDegree = new HashMap<>();
        Map<Long, List<Long>> adj = new HashMap<>();

        for (EtlPipelineStep step : steps) {
            inDegree.put(step.getId(), 0);
            adj.put(step.getId(), new ArrayList<>());
        }

        for (EtlPipelineEdge edge : edges) {
            if (stepMap.containsKey(edge.getFromStepId()) && stepMap.containsKey(edge.getToStepId())) {
                adj.get(edge.getFromStepId()).add(edge.getToStepId());
                inDegree.merge(edge.getToStepId(), 1, Integer::sum);
            }
        }

        Queue<Long> queue = new LinkedList<>();
        for (Map.Entry<Long, Integer> entry : inDegree.entrySet()) {
            if (entry.getValue() == 0) {
                queue.offer(entry.getKey());
            }
        }

        List<EtlPipelineStep> sorted = new ArrayList<>();
        while (!queue.isEmpty()) {
            Long id = queue.poll();
            sorted.add(stepMap.get(id));
            for (Long neighbor : adj.get(id)) {
                int newDegree = inDegree.merge(neighbor, -1, Integer::sum);
                if (newDegree == 0) {
                    queue.offer(neighbor);
                }
            }
        }

        if (sorted.size() != steps.size()) {
            return null; // 存在环
        }
        return sorted;
    }

    /** 字段映射（支持 dot-path sourceColumn） */
    private List<Map<String, Object>> applyFieldMapping(List<Map<String, Object>> source,
                                                         List<EtlStepColumnMapping> mappings) {
        if (mappings == null || mappings.isEmpty()) {
            return source;
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> row : source) {
            Map<String, Object> out = new LinkedHashMap<>();
            for (EtlStepColumnMapping m : mappings) {
                if (!"Y".equals(m.getEnabled())) continue;
                Object value = resolveDotPath(row, m.getSourceColumn());
                if (value == null && m.getDefaultValue() != null) {
                    value = m.getDefaultValue();
                }
                out.put(m.getTargetColumn(), value);
            }
            result.add(out);
        }
        return result;
    }

    /** 按 dot-path 从 map 中取值 */
    private Object resolveDotPath(Map<String, Object> root, String path) {
        if (path == null || path.isEmpty() || root == null) return null;
        Object current = root;
        for (String segment : path.split("\\.")) {
            if (current == null) return null;
            if (segment.startsWith("[") || segment.equals("*")) continue;
            if (!(current instanceof Map)) return null;
            current = ((Map<String, Object>) current).get(segment);
        }
        return current;
    }

    /** 横向合并 JOIN（基于 edgeConfig 中的 join 条件） */
    private List<Map<String, Object>> applyJoin(Map<Long, List<Map<String, Object>>> stepOutputs,
                                                 List<EtlPipelineEdge> incoming,
                                                 List<EtlPipelineEdge> allEdges) {
        if (incoming.size() < 2) {
            // 单输入 JOIN 退化为直传
            return stepOutputs.getOrDefault(incoming.get(0).getFromStepId(), new ArrayList<>());
        }
        // 简单实现：按行索引横向拼接（后续可由 edgeConfig 的 joinKey 驱动）
        List<Map<String, Object>> left = stepOutputs.getOrDefault(incoming.get(0).getFromStepId(), new ArrayList<>());
        List<Map<String, Object>> right = stepOutputs.getOrDefault(incoming.get(1).getFromStepId(), new ArrayList<>());
        List<Map<String, Object>> merged = new ArrayList<>();
        int maxSize = Math.max(left.size(), right.size());
        for (int i = 0; i < maxSize; i++) {
            Map<String, Object> row = new LinkedHashMap<>();
            if (i < left.size()) row.putAll(left.get(i));
            if (i < right.size()) row.putAll(right.get(i));
            merged.add(row);
        }
        return merged;
    }

    /** 纵向合并 UNION（按列名对齐） */
    private List<Map<String, Object>> applyUnion(Map<Long, List<Map<String, Object>>> stepOutputs,
                                                  List<EtlPipelineEdge> incoming) {
        List<Map<String, Object>> all = new ArrayList<>();
        for (EtlPipelineEdge edge : incoming) {
            List<Map<String, Object>> data = stepOutputs.getOrDefault(edge.getFromStepId(), new ArrayList<>());
            all.addAll(data);
        }
        return all;
    }

    /** 提取列名 */
    private List<String> collectColumns(List<Map<String, Object>> data) {
        List<String> columns = new ArrayList<>();
        if (data != null && !data.isEmpty()) {
            columns.addAll(data.get(0).keySet());
        }
        return columns;
    }

    // ===== 中等优化：chunk 边读边写 =====

    /**
     * 判定整条管线能否走流式 chunk 路径。
     * 条件：节点链路严格线性（每步只有一个上游也只有一个下游，除末端），
     * 起点是 EXTRACT 且对应 reader.supportsStreaming()，
     * 中间若有 TRANSFORM 只能是 FIELD_MAP（JOIN/UNION 必须物化全量，不支持），
     * 终点是 LOAD 且 write=true；末端 LOAD 不多于一个。
     * limit>0（调试预览）一律不走流式，保留 preview 老路径。
     */
    private boolean canStreamPipeline(List<EtlPipelineStep> sorted, List<EtlPipelineEdge> edges) {
        if (sorted.isEmpty()) return false;
        // 端点：起点必须 EXTRACT，终点必须 LOAD
        EtlPipelineStep head = sorted.get(0);
        EtlPipelineStep tail = sorted.get(sorted.size() - 1);
        if (!"EXTRACT".equals(head.getStepType())) return false;
        if (!"LOAD".equals(tail.getStepType())) return false;

        // 每个中间节点的入度必须 ≤1、出度必须 ≤1（严格线性）
        Map<Long, Integer> inDeg = new HashMap<>();
        Map<Long, Integer> outDeg = new HashMap<>();
        for (EtlPipelineStep s : sorted) { inDeg.put(s.getId(), 0); outDeg.put(s.getId(), 0); }
        for (EtlPipelineEdge e : edges) {
            if (!inDeg.containsKey(e.getFromStepId()) || !outDeg.containsKey(e.getToStepId())) continue;
            inDeg.merge(e.getToStepId(), 1, Integer::sum);
            outDeg.merge(e.getFromStepId(), 1, Integer::sum);
        }
        // 起点 inDeg=0、终点 outDeg=0、其余 inDeg=outDeg=1
        if (inDeg.get(head.getId()) != 0) return false;
        if (outDeg.get(tail.getId()) != 0) return false;
        for (EtlPipelineStep s : sorted) {
            if (s.getId().equals(head.getId()) || s.getId().equals(tail.getId())) continue;
            if (inDeg.get(s.getId()) != 1 || outDeg.get(s.getId()) != 1) return false;
            // 中间步骤只能是 TRANSFORM/FIELD_MAP，禁止 JOIN/UNION
            if (!"TRANSFORM".equals(s.getStepType())) return false;
            String sub = s.getStepSubType();
            if (sub == null || "FIELD_MAP".equals(sub)) continue;
            return false;
        }
        // EXTRACT 对应 reader 必须支持流式
        StepConfig headCfg = StepConfig.fromStep(head);
        DataSourceReader reader;
        try {
            reader = readerFactory.getReader(headCfg.getSourceType());
        } catch (Exception e) {
            log.warn("流式判定读取 reader 失败: {}", e.getMessage());
            return false;
        }
        if (reader == null || !reader.supportsStreaming()) return false;

        // 末端 LOAD 子类型必须是 DB_INSERT/DB_MERGE/FILE_*（与现有 executeLoadStep 对齐）
        String tailSub = tail.getStepSubType();
        if (tailSub == null) return false;
        if (!"DB_INSERT".equals(tailSub) && !"DB_MERGE".equals(tailSub) && !tailSub.startsWith("FILE_")) {
            return false;
        }
        return true;
    }

    /**
     * 流式执行：拉 chunk → 跑 transform 链 → 写 LOAD；每个 chunk 处理完立即释放。
     * 不在 stepOutputs 缓存全量。SSE 回调非空时推送 chunk_progress 事件。
     */
    private List<StepResult> executeStreaming(List<EtlPipelineStep> sorted,
                                                List<EtlPipelineEdge> edges,
                                                boolean write,
                                                Consumer<Map<String, Object>> onEvent) {
        EtlPipelineStep head = sorted.get(0);
        EtlPipelineStep tail = sorted.get(sorted.size() - 1);
        List<EtlPipelineStep> transforms = sorted.size() > 2
                ? new ArrayList<>(sorted.subList(1, sorted.size() - 1))
                : Collections.emptyList();

        StepResult extractResult = new StepResult();
        extractResult.setStepId(head.getId());
        extractResult.setStepOrder(head.getOrderIndex() != null ? head.getOrderIndex() : 0);
        extractResult.setStepType("EXTRACT");
        extractResult.setStepName(head.getStepName());
        extractResult.setNextOnSuccess(true);

        StepResult loadResult = new StepResult();
        loadResult.setStepId(tail.getId());
        loadResult.setStepOrder(tail.getOrderIndex() != null ? tail.getOrderIndex() : 0);
        loadResult.setStepType("LOAD");
        loadResult.setStepName(tail.getStepName());
        loadResult.setNextOnSuccess(true);

        // 中间 transform 每个也准备一个 StepResult（行数累加）
        List<StepResult> transformResults = new ArrayList<>();
        for (EtlPipelineStep t : transforms) {
            StepResult r = new StepResult();
            r.setStepId(t.getId());
            r.setStepOrder(t.getOrderIndex() != null ? t.getOrderIndex() : 0);
            r.setStepType("TRANSFORM");
            r.setStepName(t.getStepName());
            r.setNextOnSuccess(true);
            transformResults.add(r);
        }

        long extractStart = System.currentTimeMillis();
        long totalExtracted = 0;
        long totalWritten = 0;
        long chunkSeq = 0;

        try {
            StepConfig extractCfg = StepConfig.fromStep(head);
            int chunkSize = extractCfg.getBatchSize() != null && extractCfg.getBatchSize() > 0
                    ? extractCfg.getBatchSize() : 2000;

            DataSourceReader reader = readerFactory.getReader(extractCfg.getSourceType());
            reader.initWithConfig(extractCfg, dataSourceManager);
            try {
                // 预取字段映射：每个 TRANSFORM 步骤的字段映射，缓存避免每 chunk 查库
                Map<Long, List<EtlStepColumnMapping>> mappingsCache = new HashMap<>();
                for (EtlPipelineStep t : transforms) {
                    mappingsCache.put(t.getId(), stepColumnMappingService.listByStepId(t.getId()));
                }

                // LOAD writer 预初始化（DB_INSERT 等模式下，writer 复用）
                StepConfig loadCfg = StepConfig.fromStep(tail);
                DataWriter writer = null;
                List<EtlStepColumnMapping> loadMappings = null;
                if (write) {
                    String mode = loadCfg.getWriteMode();
                    if (mode == null) mode = "INSERT";
                    String subType = tail.getStepSubType();
                    String writerKey = subType != null && subType.startsWith("FILE_") ? subType : mode;
                    writer = writerFactory.getWriter(writerKey);
                    if (subType != null && (subType.equals("DB_INSERT") || subType.equals("DB_MERGE"))) {
                        loadMappings = stepColumnMappingService.listByStepId(tail.getId());
                    }
                }

                // chunk 循环
                List<Map<String, Object>> chunk;
                while (true) {
                    chunk = reader.readBatch(chunkSize);
                    if (chunk == null || chunk.isEmpty()) break;

                    long chunkRows = chunk.size();
                    totalExtracted += chunkRows;

                    // expose 原始响应一次性记录到 extractResult
                    if (chunkSeq == 0 && reader instanceof com.etl.service.reader.HttpReader) {
                        com.etl.service.reader.HttpReader hr = (com.etl.service.reader.HttpReader) reader;
                        extractResult.setRawResponse(hr.getLastRawResponse());
                        extractResult.setStatusCode(hr.getLastStatusCode());
                        extractResult.setResponseHeaders(hr.getLastResponseHeaders());
                        extractResult.setFinalUrl(hr.getLastRequestUrl());
                        extractResult.setFinalMethod(hr.getLastRequestMethod());
                    }

                    // 跑 transform 链
                    List<Map<String, Object>> current = chunk;
                    for (int i = 0; i < transforms.size(); i++) {
                        EtlPipelineStep tStep = transforms.get(i);
                        List<EtlStepColumnMapping> m = mappingsCache.get(tStep.getId());
                        current = applyFieldMapping(current, m);
                        // 累加 transformResult 行数（输入=上一步输出，输出=current.size()）
                        StepResult tr = transformResults.get(i);
                        tr.setInputRows(tr.getInputRows() + chunkRows);
                        tr.setOutputRows(tr.getOutputRows() + current.size());
                    }

                    // 写入 LOAD
                    if (write && writer != null) {
                        if (loadMappings != null) {
                            writer.writeWithConfig(current, loadCfg, loadMappings, dataSourceManager);
                        } else {
                            writer.writeWithConfig(current, loadCfg, null, dataSourceManager);
                        }
                        totalWritten += current.size();
                    }

                    // 第一个 chunk 顺手记录 outputColumns / 输出样本，便于前端展示
                    if (chunkSeq == 0) {
                        extractResult.setOutputColumns(collectColumns(chunk));
                        // 输出样本：仅取前 5 行作签名，避免内存累积
                        List<Map<String, Object>> sample = chunk.size() > 5
                                ? new ArrayList<>(chunk.subList(0, 5)) : chunk;
                        extractResult.setOutputData(sample);
                    }
                    extractResult.setOutputRows(totalExtracted);

                    chunkSeq++;
                    if (onEvent != null) {
                        Map<String, Object> evt = new LinkedHashMap<>();
                        evt.put("chunkSeq", chunkSeq);
                        evt.put("chunkRows", chunkRows);
                        evt.put("extractedTotal", totalExtracted);
                        evt.put("writtenTotal", totalWritten);
                        sendEvent(onEvent, "chunk_progress", evt);
                    }

                    // 释放 chunk 引用
                    chunk = null;
                    current = null;
                }

                // EXTRACT 步骤完成
                extractResult.setStatus("SUCCESS");
                extractResult.setDurationMs(System.currentTimeMillis() - extractStart);

                // TRANSFORM 步骤完成
                for (StepResult tr : transformResults) {
                    tr.setStatus("SUCCESS");
                    // 没有 outputData 累加字段（chunk 已释放），仅保留列样本便于调试
                    tr.setDurationMs(0);
                }

                // LOAD 步骤完成
                if (write) {
                    loadResult.setStatus("SUCCESS");
                    loadResult.setInputRows((int) totalExtracted);
                    loadResult.setOutputRows((int) totalWritten);
                } else {
                    loadResult.setStatus("SKIPPED");
                    loadResult.setErrorMessage("调试模式跳过写入");
                }
                loadResult.setDurationMs(System.currentTimeMillis() - extractStart);

            } finally {
                reader.close();
            }

        } catch (Exception e) {
            log.error("流式执行失败", e);
            extractResult.setStatus("FAILED");
            extractResult.setErrorMessage(e.getMessage());
            // 把后续步骤都标记 FAILED 已无意义，但保留 step 列表完整性
            for (StepResult tr : transformResults) {
                tr.setStatus("FAILED");
                tr.setErrorMessage(e.getMessage());
            }
            loadResult.setStatus("FAILED");
            loadResult.setErrorMessage(e.getMessage());
        }

        List<StepResult> all = new ArrayList<>();
        all.add(extractResult);
        all.addAll(transformResults);
        all.add(loadResult);
        return all;
    }
}
