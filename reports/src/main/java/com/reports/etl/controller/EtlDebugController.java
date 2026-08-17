package com.reports.etl.controller;

import com.reports.dto.common.ApiResponse;
import com.reports.etl.entity.EtlTask;
import com.reports.etl.entity.EtlWsConfig;
import com.reports.etl.entity.EtlProcConfig;
import com.reports.etl.mapper.EtlTaskMapper;
import com.reports.etl.mapper.EtlWsConfigMapper;
import com.reports.etl.mapper.EtlProcConfigMapper;
import com.reports.etl.service.extractor.ProcedureExtractor;
import com.reports.etl.service.extractor.WebServiceExtractor;
import com.reports.etl.service.transformer.EtlTransformer;
import com.reports.etl.service.writer.EtlWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/etl/debug")
public class EtlDebugController {

    private final EtlTaskMapper taskMapper;
    private final EtlWsConfigMapper wsConfigMapper;
    private final EtlProcConfigMapper procConfigMapper;
    private final WebServiceExtractor webServiceExtractor;
    private final ProcedureExtractor procedureExtractor;
    private final EtlTransformer transformer;
    private final EtlWriter writer;

    public EtlDebugController(EtlTaskMapper taskMapper, EtlWsConfigMapper wsConfigMapper,
                              EtlProcConfigMapper procConfigMapper,
                              WebServiceExtractor webServiceExtractor,
                              ProcedureExtractor procedureExtractor,
                              EtlTransformer transformer, EtlWriter writer) {
        this.taskMapper = taskMapper;
        this.wsConfigMapper = wsConfigMapper;
        this.procConfigMapper = procConfigMapper;
        this.webServiceExtractor = webServiceExtractor;
        this.procedureExtractor = procedureExtractor;
        this.transformer = transformer;
        this.writer = writer;
    }

    /**
     * 步骤1：抽取调试
     */
    @PostMapping("/extract")
    public ApiResponse<Map<String, Object>> extract(@RequestBody Map<String, Object> params) {
        Long taskId = Long.valueOf(params.get("taskId").toString());
        int batchSize = params.get("batchSize") != null ? Integer.parseInt(params.get("batchSize").toString()) : 10;

        long start = System.currentTimeMillis();
        Map<String, Object> result;
        EtlTask task = taskMapper.selectById(taskId);

        if ("WEBSERVICE".equals(task.getExtractType())) {
            result = webServiceExtractor.extract(taskId, batchSize);
        } else if ("PROCEDURE".equals(task.getExtractType())) {
            result = procedureExtractor.extract(taskId, batchSize);
        } else {
            return ApiResponse.fail("400", "未知抽取类型", "400", "任务未配置抽取类型");
        }

        long duration = System.currentTimeMillis() - start;
        result.put("durationMs", duration);
        result.put("status", "SUCCESS");
        return ApiResponse.success(result);
    }

    /**
     * 步骤2：转换映射调试
     */
    @PostMapping("/transform")
    public ApiResponse<Map<String, Object>> transform(@RequestBody Map<String, Object> params) {
        Long taskId = Long.valueOf(params.get("taskId").toString());
        @SuppressWarnings("unchecked")
        java.util.List<Map<String, Object>> sampleRows = (java.util.List<Map<String, Object>>) params.get("sampleRows");

        long start = System.currentTimeMillis();
        Map<String, Object> preview = transformer.preview(taskId, sampleRows);
        long duration = System.currentTimeMillis() - start;
        preview.put("durationMs", duration);
        preview.put("status", "SUCCESS");
        return ApiResponse.success(preview);
    }

    /**
     * 步骤3：写入调试（默认 dry-run）
     */
    @PostMapping("/load")
    public ApiResponse<Map<String, Object>> load(@RequestBody Map<String, Object> params) {
        Long taskId = Long.valueOf(params.get("taskId").toString());
        @SuppressWarnings("unchecked")
        java.util.List<Map<String, Object>> rows = (java.util.List<Map<String, Object>>) params.get("rows");
        boolean dryRun = Boolean.parseBoolean(params.getOrDefault("dryRun", "true").toString());

        long start = System.currentTimeMillis();
        int written = writer.write(taskId, rows, dryRun);
        long duration = System.currentTimeMillis() - start;

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("writtenRows", written);
        result.put("durationMs", duration);
        result.put("dryRun", dryRun);
        result.put("status", "SUCCESS");
        return ApiResponse.success(result);
    }

    /**
     * 全链路调试（依次跑抽取→转换→写入）
     */
    @PostMapping("/run-all")
    public ApiResponse<Map<String, Object>> runAll(@RequestBody Map<String, Object> params) {
        Long taskId = Long.valueOf(params.get("taskId").toString());
        boolean dryRun = Boolean.parseBoolean(params.getOrDefault("dryRun", "true").toString());
        int batchSize = params.get("batchSize") != null ? Integer.parseInt(params.get("batchSize").toString()) : 10;

        EtlTask task = taskMapper.selectById(taskId);
        Map<String, Object> fullResult = new LinkedHashMap<>();
        long totalStart = System.currentTimeMillis();

        // Step 1: 抽取
        long extractStart = System.currentTimeMillis();
        Map<String, Object> extractResult;
        if ("WEBSERVICE".equals(task.getExtractType())) {
            extractResult = webServiceExtractor.extract(taskId, batchSize);
        } else {
            extractResult = procedureExtractor.extract(taskId, batchSize);
        }
        long extractDuration = System.currentTimeMillis() - extractStart;
        fullResult.put("extract", Map.of(
                "status", "SUCCESS",
                "rows", extractResult.get("rows"),
                "totalRows", extractResult.get("totalRows"),
                "columns", extractResult.get("columns"),
                "durationMs", extractDuration
        ));

        // Step 2: 转换
        @SuppressWarnings("unchecked")
        java.util.List<Map<String, Object>> rows = (java.util.List<Map<String, Object>>) extractResult.get("rows");
        long transformStart = System.currentTimeMillis();
        java.util.List<Map<String, Object>> transformed = transformer.transform(taskId, rows);
        long transformDuration = System.currentTimeMillis() - transformStart;
        fullResult.put("transform", Map.of(
                "status", "SUCCESS",
                "rows", transformed,
                "durationMs", transformDuration
        ));

        // Step 3: 写入
        long loadStart = System.currentTimeMillis();
        int written = writer.write(taskId, transformed, dryRun);
        long loadDuration = System.currentTimeMillis() - loadStart;
        fullResult.put("load", Map.of(
                "status", "SUCCESS",
                "writtenRows", written,
                "durationMs", loadDuration
        ));

        fullResult.put("totalDurationMs", System.currentTimeMillis() - totalStart);
        fullResult.put("dryRun", dryRun);
        return ApiResponse.success(fullResult);
    }
}