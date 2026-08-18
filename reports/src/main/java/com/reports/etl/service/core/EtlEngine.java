package com.reports.etl.service.core;

import com.reports.etl.dto.enums.StepName;
import com.reports.etl.entity.*;
import com.reports.etl.service.extractor.ProcedureExtractor;
import com.reports.etl.service.extractor.SourceExtractorFacade;
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

    private final EtlMetaDao metaDao;
    private final WebServiceExtractor webServiceExtractor;
    private final ProcedureExtractor procedureExtractor;
    private final SourceExtractorFacade sourceExtractorFacade;
    private final EtlTransformer transformer;
    private final EtlWriter writer;

    public EtlEngine(EtlMetaDao metaDao,
                     WebServiceExtractor webServiceExtractor, ProcedureExtractor procedureExtractor,
                     SourceExtractorFacade sourceExtractorFacade,
                     EtlTransformer transformer, EtlWriter writer) {
        this.metaDao = metaDao;
        this.webServiceExtractor = webServiceExtractor;
        this.procedureExtractor = procedureExtractor;
        this.sourceExtractorFacade = sourceExtractorFacade;
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
        EtlTask task = metaDao.getTask(taskId);
        if (task == null) throw new RuntimeException("任务不存在: " + taskId);

        EtlTaskLog taskLog = new EtlTaskLog();
        taskLog.setTaskId(task.getId());
        taskLog.setTaskName(task.getName());
        taskLog.setTriggerType(triggerType);
        taskLog.setStatus("RUNNING");
        Long logId = metaDao.insertTaskLog(taskLog);
        taskLog.setId(logId);

        long startTime = System.currentTimeMillis();

        try {
            // Step 1: 抽取
            Map<String, Object> extractResult = extract(task, logId, startTime);
            List<Map<String, Object>> rows = (List<Map<String, Object>>) extractResult.get("rows");
            int extractedCount = (int) extractResult.getOrDefault("totalRows", 0);

            // Step 2: 转换映射
            List<Map<String, Object>> transformed = transformer.transform(taskId, rows);
            saveStepLog(logId, StepName.TRANSFORM, "SUCCESS", System.currentTimeMillis(), null,
                    transformed.size(), null);

            // Step 3: 写入
            int writtenCount = writer.write(taskId, transformed, false);
            saveStepLog(logId, StepName.LOAD, "SUCCESS", System.currentTimeMillis(), null,
                    writtenCount, null);

            taskLog.setStatus("SUCCESS");
            taskLog.setExtractedRows(extractedCount);
            taskLog.setWrittenRows(writtenCount);

        } catch (Exception e) {
            taskLog.setStatus("FAILED");
            taskLog.setErrorMsg(e.getMessage());
            taskLog.setEndTime(new Date());
            metaDao.updateTaskLog(taskLog);
            saveStepLog(logId, StepName.EXTRACT, "FAILED", startTime, System.currentTimeMillis(), 0, e.getMessage());
            log.error("任务执行失败 taskId=" + taskId + " error=" + e.getMessage(), e);
        }

        taskLog.setEndTime(new Date());
        metaDao.updateTaskLog(taskLog);
        return taskLog;
    }

    private Map<String, Object> extract(EtlTask task, Long logId, long start) {
        Map<String, Object> result;
        int batchSize = task.getBatchSize() != null ? task.getBatchSize() : 100;

        if (task.getSourceId() != null) {
            // Source 改造路径：经独立来源实体抽取
            EtlSource source = metaDao.getSource(task.getSourceId());
            if (source == null) throw new RuntimeException("抽取来源不存在: " + task.getSourceId());
            result = sourceExtractorFacade.extract(source, batchSize);
        } else if ("WEBSERVICE".equals(task.getExtractType())) {
            // 兼容路径：旧任务子表配置
            result = webServiceExtractor.extract(task.getId(), batchSize);
        } else if ("PROCEDURE".equals(task.getExtractType())) {
            result = procedureExtractor.extract(task.getId(), batchSize);
        } else {
            throw new RuntimeException("未知抽取类型: " + task.getExtractType());
        }

        saveStepLog(logId, StepName.EXTRACT, "SUCCESS", start, System.currentTimeMillis(),
                (int) result.getOrDefault("totalRows", 0), null);
        return result;
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
        metaDao.insertStepLog(stepLog);
    }
}