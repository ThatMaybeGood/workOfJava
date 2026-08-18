package com.reports.etl.controller;

import com.reports.dto.common.ApiResponse;
import com.reports.etl.entity.EtlSource;
import com.reports.etl.entity.EtlTask;
import com.reports.etl.service.core.EtlMetaDao;
import com.reports.etl.service.extractor.ProcedureExtractor;
import com.reports.etl.service.extractor.SourceExtractorFacade;
import com.reports.etl.service.extractor.WebServiceExtractor;
import com.reports.etl.service.transformer.EtlTransformer;
import com.reports.etl.service.writer.EtlWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/etl/debug")
public class EtlDebugController {

    private final EtlMetaDao metaDao;
    private final WebServiceExtractor webServiceExtractor;
    private final ProcedureExtractor procedureExtractor;
    private final SourceExtractorFacade sourceExtractorFacade;
    private final EtlTransformer transformer;
    private final EtlWriter writer;

    public EtlDebugController(EtlMetaDao metaDao,
                              WebServiceExtractor webServiceExtractor,
                              ProcedureExtractor procedureExtractor,
                              SourceExtractorFacade sourceExtractorFacade,
                              EtlTransformer transformer, EtlWriter writer) {
        this.metaDao = metaDao;
        this.webServiceExtractor = webServiceExtractor;
        this.procedureExtractor = procedureExtractor;
        this.sourceExtractorFacade = sourceExtractorFacade;
        this.transformer = transformer;
        this.writer = writer;
    }

    /**
     * 统一抽取入口：sourceId 非空走来源实体，为空回退旧子表配置
     */
    private Map<String, Object> doExtract(EtlTask task, int batchSize) {
        if (task.getSourceId() != null) {
            EtlSource source = metaDao.getSource(task.getSourceId());
            if (source == null) throw new RuntimeException("抽取来源不存在: " + task.getSourceId());
            return sourceExtractorFacade.extract(source, batchSize);
        }
        if ("WEBSERVICE".equals(task.getExtractType())) {
            return webServiceExtractor.extract(task.getId(), batchSize);
        } else if ("PROCEDURE".equals(task.getExtractType())) {
            return procedureExtractor.extract(task.getId(), batchSize);
        }
        throw new RuntimeException("未知抽取类型: " + task.getExtractType());
    }

    /**
     * 步骤1：抽取调试（单独跑抽取环节）
     */
    @PostMapping("/extract")
    public ApiResponse<Map<String, Object>> extract(@RequestBody Map<String, Object> params) {
        Long taskId = Long.valueOf(params.get("taskId").toString());
        int batchSize = params.get("batchSize") != null ? Integer.parseInt(params.get("batchSize").toString()) : 20;

        long start = System.currentTimeMillis();
        EtlTask task = metaDao.getTask(taskId);
        if (task == null) return ApiResponse.fail("400", "任务不存在", "400", "taskId=" + taskId);

        Map<String, Object> result = doExtract(task, batchSize);

        long duration = System.currentTimeMillis() - start;
        result.put("durationMs", duration);
        result.put("status", "SUCCESS");
        return ApiResponse.success(result);
    }

    /**
     * 步骤2：转换映射调试（单独跑转换环节）
     * 入参: { taskId, rows? }
     */
    @PostMapping("/transform")
    public ApiResponse<Map<String, Object>> transform(@RequestBody Map<String, Object> params) {
        Long taskId = Long.valueOf(params.get("taskId").toString());
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> sampleRows = (List<Map<String, Object>>) params.getOrDefault("rows", java.util.Collections.emptyList());

        long start = System.currentTimeMillis();
        Map<String, Object> preview = transformer.preview(taskId, sampleRows);
        long duration = System.currentTimeMillis() - start;
        preview.put("durationMs", duration);
        preview.put("status", "SUCCESS");
        preview.put("inputRows", sampleRows.size());
        return ApiResponse.success(preview);
    }

    /**
     * 步骤3：写入调试（默认 dry-run，force=true 才真实写入）
     * 入参: { taskId, rows, dryRun? }
     */
    @PostMapping("/load")
    public ApiResponse<Map<String, Object>> load(@RequestBody Map<String, Object> params) {
        Long taskId = Long.valueOf(params.get("taskId").toString());
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> rows = (List<Map<String, Object>>) params.getOrDefault("rows", java.util.Collections.emptyList());
        boolean dryRun = Boolean.parseBoolean(params.getOrDefault("dryRun", "true").toString());

        long start = System.currentTimeMillis();
        try {
            int written = writer.write(taskId, rows, dryRun);
            long duration = System.currentTimeMillis() - start;
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("writtenRows", written);
            result.put("durationMs", duration);
            result.put("dryRun", dryRun);
            result.put("status", "SUCCESS");
            return ApiResponse.success(result);
        } catch (Exception e) {
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("status", "FAILED");
            result.put("error", e.getMessage());
            result.put("dryRun", dryRun);
            return ApiResponse.success(result);
        }
    }

    /**
     * 全链路调试（依次跑抽取→转换→写入）
     * 入参: { taskId, dryRun?, batchSize? }
     */
    @PostMapping("/run-all")
    public ApiResponse<Map<String, Object>> runAll(@RequestBody Map<String, Object> params) {
        Long taskId = Long.valueOf(params.get("taskId").toString());
        boolean dryRun = Boolean.parseBoolean(params.getOrDefault("dryRun", "true").toString());
        int batchSize = params.get("batchSize") != null ? Integer.parseInt(params.get("batchSize").toString()) : 20;

        EtlTask task = metaDao.getTask(taskId);
        if (task == null) return ApiResponse.fail("400", "任务不存在", "400", "taskId=" + taskId);
        Map<String, Object> fullResult = new LinkedHashMap<>();
        long totalStart = System.currentTimeMillis();
        boolean overallOk = true;

        // Step 1: 抽取
        long s1 = System.currentTimeMillis();
        try {
            Map<String, Object> extractResult = doExtract(task, batchSize);
            Map<String, Object> extractStep = new LinkedHashMap<>();
            extractStep.put("status", "SUCCESS");
            extractStep.put("totalRows", extractResult.get("totalRows"));
            extractStep.put("columns", extractResult.get("columns"));
            extractStep.put("rows", extractResult.get("rows"));
            extractStep.put("durationMs", System.currentTimeMillis() - s1);
            fullResult.put("extract", extractStep);
        } catch (Exception e) {
            overallOk = false;
            Map<String, Object> extractStep = new LinkedHashMap<>();
            extractStep.put("status", "FAILED");
            extractStep.put("error", e.getMessage());
            extractStep.put("durationMs", System.currentTimeMillis() - s1);
            fullResult.put("extract", extractStep);
        }

        // Step 2: 转换
        long s2 = System.currentTimeMillis();
        try {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> rows = (List<Map<String, Object>>)
                    ((Map<String, Object>) fullResult.get("extract")).getOrDefault("rows", java.util.Collections.emptyList());
            List<Map<String, Object>> transformed = transformer.transform(taskId, rows);
            Map<String, Object> transformStep = new LinkedHashMap<>();
            transformStep.put("status", "SUCCESS");
            transformStep.put("rows", transformed);
            transformStep.put("rowCount", transformed.size());
            transformStep.put("durationMs", System.currentTimeMillis() - s2);
            fullResult.put("transform", transformStep);
        } catch (Exception e) {
            overallOk = false;
            Map<String, Object> transformStep = new LinkedHashMap<>();
            transformStep.put("status", "FAILED");
            transformStep.put("error", e.getMessage());
            transformStep.put("durationMs", System.currentTimeMillis() - s2);
            fullResult.put("transform", transformStep);
        }

        // Step 3: 写入
        long s3 = System.currentTimeMillis();
        try {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> transformedRows = (List<Map<String, Object>>)
                    ((Map<String, Object>) fullResult.get("transform")).getOrDefault("rows", java.util.Collections.emptyList());
            int written = writer.write(taskId, transformedRows, dryRun);
            Map<String, Object> loadStep = new LinkedHashMap<>();
            loadStep.put("status", "SUCCESS");
            loadStep.put("writtenRows", written);
            loadStep.put("dryRun", dryRun);
            loadStep.put("durationMs", System.currentTimeMillis() - s3);
            fullResult.put("load", loadStep);
        } catch (Exception e) {
            overallOk = false;
            Map<String, Object> loadStep = new LinkedHashMap<>();
            loadStep.put("status", "FAILED");
            loadStep.put("error", e.getMessage());
            loadStep.put("dryRun", dryRun);
            loadStep.put("durationMs", System.currentTimeMillis() - s3);
            fullResult.put("load", loadStep);
        }

        fullResult.put("overallStatus", overallOk ? "SUCCESS" : "FAILED");
        fullResult.put("totalDurationMs", System.currentTimeMillis() - totalStart);
        fullResult.put("dryRun", dryRun);
        return ApiResponse.success(fullResult);
    }
}