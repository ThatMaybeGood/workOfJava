package com.reports.etl.service.core;

import com.reports.etl.dto.enums.StepName;
import com.reports.etl.entity.*;
import com.reports.etl.mapper.*;
import com.reports.etl.service.extractor.ProcedureExtractor;
import com.reports.etl.service.extractor.WebServiceExtractor;
import com.reports.etl.service.transformer.EtlTransformer;
import com.reports.etl.service.writer.EtlWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;

@Slf4j
@Component
public class EtlEngine {

    private final EtlTaskMapper taskMapper;
    private final EtlWsConfigMapper wsConfigMapper;
    private final EtlProcConfigMapper procConfigMapper;
    private final EtlMappingMapper mappingMapper;
    private final EtlTaskLogMapper taskLogMapper;
    private final EtlStepLogMapper stepLogMapper;
    private final WebServiceExtractor webServiceExtractor;
    private final ProcedureExtractor procedureExtractor;
    private final EtlTransformer transformer;
    private final EtlWriter writer;

    public EtlEngine(EtlTaskMapper taskMapper, EtlWsConfigMapper wsConfigMapper,
                     EtlProcConfigMapper procConfigMapper, EtlMappingMapper mappingMapper,
                     EtlTaskLogMapper taskLogMapper, EtlStepLogMapper stepLogMapper,
                     WebServiceExtractor webServiceExtractor, ProcedureExtractor procedureExtractor,
                     EtlTransformer transformer, EtlWriter writer) {
        this.taskMapper = taskMapper;
        this.wsConfigMapper = wsConfigMapper;
        this.procConfigMapper = procConfigMapper;
        this.mappingMapper = mappingMapper;
        this.taskLogMapper = taskLogMapper;
        this.stepLogMapper = stepLogMapper;
        this.webServiceExtractor = webServiceExtractor;
        this.procedureExtractor = procedureExtractor;
        this.transformer = transformer;
        this.writer = writer;
    }

    @PostConstruct
    public void init() {
        log.info("EtlEngine 初始化完成");
    }

    /**
     * 执行任务（完整链路：抽取 → 转换 → 写入）
     */
    public EtlTaskLog execute(Long taskId, String triggerType) {
        EtlTask task = taskMapper.selectById(taskId);
        if (task == null) throw new RuntimeException("任务不存在: " + taskId);

        EtlTaskLog taskLog = createTaskLog(task, triggerType);
        long startTime = System.currentTimeMillis();
        taskLog.setStartTime(new Date(startTime));

        try {
            // Step 1: 抽取
            Map<String, Object> extractResult = extract(task, taskLog);
            List<Map<String, Object>> rows = (List<Map<String, Object>>) extractResult.get("rows");
            int extractedCount = (int) extractResult.getOrDefault("totalRows", 0);

            // Step 2: 转换映射
            List<Map<String, Object>> transformed = transformer.transform(taskId, rows);
            saveStepLog(taskLog.getId(), StepName.TRANSFORM, "SUCCESS", System.currentTimeMillis(), null, transformed.size(), null);

            // Step 3: 写入
            int writtenCount = writer.write(taskId, transformed, false);
            saveStepLog(taskLog.getId(), StepName.LOAD, "SUCCESS", System.currentTimeMillis(), null, writtenCount, null);

            taskLog.setStatus("SUCCESS");
            taskLog.setExtractedRows(extractedCount);
            taskLog.setWrittenRows(writtenCount);

        } catch (Exception e) {
            taskLog.setStatus("FAILED");
            taskLog.setErrorMsg(e.getMessage());
            taskLog.setEndTime(new Date());
            taskLogMapper.updateById(taskLog);
            saveStepLog(taskLog.getId(), StepName.EXTRACT, "FAILED", startTime, System.currentTimeMillis(), 0, e.getMessage());
            log.error("任务执行失败 taskId=" + taskId + " error=" + e.getMessage(), e);
        }

        taskLog.setEndTime(new Date());
        taskLogMapper.updateById(taskLog);
        return taskLog;
    }

    private Map<String, Object> extract(EtlTask task, EtlTaskLog log) {
        long start = System.currentTimeMillis();
        Map<String, Object> result;

        if ("WEBSERVICE".equals(task.getExtractType())) {
            result = webServiceExtractor.extract(task.getId(), task.getBatchSize());
        } else if ("PROCEDURE".equals(task.getExtractType())) {
            result = procedureExtractor.extract(task.getId(), task.getBatchSize());
        } else {
            throw new RuntimeException("未知抽取类型: " + task.getExtractType());
        }

        saveStepLog(log.getId(), StepName.EXTRACT, "SUCCESS", start, System.currentTimeMillis(),
                (int) result.getOrDefault("totalRows", 0), null);
        return result;
    }

    private EtlTaskLog createTaskLog(EtlTask task, String triggerType) {
        EtlTaskLog log = new EtlTaskLog();
        log.setTaskId(task.getId());
        log.setTaskName(task.getName());
        log.setTriggerType(triggerType);
        log.setStatus("RUNNING");
        taskLogMapper.insert(log);
        return log;
    }

    private void saveStepLog(Long logId, StepName step, String status,
                             long startMs, Long endMs, int rows, String detail) {
        EtlStepLog stepLog = new EtlStepLog();
        stepLog.setLogId(logId);
        stepLog.setStepName(step.getValue());
        stepLog.setStatus(status);
        stepLog.setStartTime(new Date(startMs));
        if (endMs != null) {
            stepLog.setEndTime(new Date(endMs));
            stepLog.setDurationMs(endMs - startMs);
        }
        stepLog.setRowsCount(rows);
        stepLog.setDetail(detail);
        stepLogMapper.insert(stepLog);
    }
}