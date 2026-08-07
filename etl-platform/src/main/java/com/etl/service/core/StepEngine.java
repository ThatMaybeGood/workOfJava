package com.etl.service.core;

import com.etl.dto.DebugResult;
import com.etl.dto.StepResult;
import com.etl.entity.EtlColumnMapping;
import com.etl.entity.EtlTaskConfig;
import com.etl.service.admin.EtlColumnMappingService;
import com.etl.service.admin.EtlTaskConfigService;
import com.etl.service.reader.DataSourceReader;
import com.etl.service.reader.ReaderFactory;
import com.etl.service.writer.DataWriter;
import com.etl.service.writer.WriterFactory;
import com.etl.util.PrimaryKeyGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 步骤编排引擎：将 ETL 任务拆分为 抽取(EXTRACT) → 转换(TRANSFORM) → 写入(LOAD)
 * 三个可独立观察的步骤，支持单步调试与成功/失败分流。
 */
@Slf4j
@Component
public class StepEngine {

    @Autowired
    private ReaderFactory readerFactory;

    @Autowired
    private WriterFactory writerFactory;

    @Autowired
    private DataSourceManager dataSourceManager;

    @Autowired
    private EtlTaskConfigService taskConfigService;

    @Autowired
    private EtlColumnMappingService columnMappingService;

    /**
     * 分步调试运行。
     *
     * @param taskCode 任务编码
     * @param limit    抽取行数上限（<=0 表示不限）
     * @param write    是否执行写入步骤
     * @return 步骤执行结果
     */
    public DebugResult debug(String taskCode, int limit, boolean write) {
        long start = System.currentTimeMillis();
        DebugResult result = new DebugResult();
        result.setTaskCode(taskCode);
        result.setExecutionId("DEBUG_" + PrimaryKeyGenerator.nextIdStr());
        result.setStatus("SUCCESS");
        List<StepResult> steps = new ArrayList<>();

        try {
            EtlTaskConfig task = taskConfigService.getByTaskCode(taskCode);
            if (task == null) {
                throw new RuntimeException("任务不存在: " + taskCode);
            }
            result.setTaskName(task.getTaskName());
            List<EtlColumnMapping> mappings = columnMappingService.listByTaskCode(taskCode);

            // ── Step 1: EXTRACT 抽取 ──
            StepResult extract = new StepResult();
            extract.setStepOrder(1);
            extract.setStepType("EXTRACT");
            extract.setStepName("数据抽取");
            extract.setNextOnSuccess(true);
            extract.setNextOnFail(false);
            executeExtract(task, extract, limit);
            steps.add(extract);

            // 分流：抽取失败 → 停止，不执行后续步骤
            if (!"SUCCESS".equals(extract.getStatus())) {
                result.setStatus("FAILED");
                result.setErrorMessage(extract.getErrorMessage());
                result.setSteps(steps);
                result.setTotalDurationMs(System.currentTimeMillis() - start);
                return result;
            }

            List<Map<String, Object>> transformed = extract.getOutputData();

            // ── Step 2: TRANSFORM 转换（字段映射） ──
            StepResult transform = new StepResult();
            transform.setStepOrder(2);
            transform.setStepType("TRANSFORM");
            transform.setStepName("字段转换");
            transform.setNextOnSuccess(true);
            transform.setNextOnFail(true);
            transformed = executeTransform(task, mappings, extract, transform);
            steps.add(transform);

            // ── Step 3: LOAD 写入（可选） ──
            if (write) {
                StepResult load = new StepResult();
                load.setStepOrder(3);
                load.setStepType("LOAD");
                load.setStepName("目标写入");
                load.setNextOnSuccess(false);
                load.setNextOnFail(false);
                executeLoad(task, mappings, transformed, load);
                steps.add(load);
                if (!"SUCCESS".equals(load.getStatus())) {
                    result.setStatus("FAILED");
                    result.setErrorMessage(load.getErrorMessage());
                }
            }

        } catch (Exception e) {
            log.error("任务 [{}] 调试执行失败", taskCode, e);
            result.setStatus("FAILED");
            result.setErrorMessage(e.getMessage());
        }

        result.setSteps(steps);
        result.setTotalDurationMs(System.currentTimeMillis() - start);
        return result;
    }

    /** 抽取步骤 */
    private void executeExtract(EtlTaskConfig task, StepResult step, int limit) {
        long s = System.currentTimeMillis();
        try {
            DataSourceReader reader = readerFactory.getReader(task.getSourceType());
            reader.init(task, dataSourceManager);
            List<Map<String, Object>> data = (limit > 0) ? reader.preview(limit) : reader.readAll();
            step.setStatus("SUCCESS");
            step.setOutputRows(data.size());
            step.setOutputColumns(collectColumns(data));
            step.setOutputData(data);
            step.setDurationMs(System.currentTimeMillis() - s);
        } catch (Exception e) {
            log.error("抽取步骤失败", e);
            step.setStatus("FAILED");
            step.setErrorMessage(e.getMessage());
            step.setDurationMs(System.currentTimeMillis() - s);
        }
    }

    /** 转换步骤：按字段映射重组数据 */
    private List<Map<String, Object>> executeTransform(EtlTaskConfig task,
                                                       List<EtlColumnMapping> mappings,
                                                       StepResult extract,
                                                       StepResult step) {
        long s = System.currentTimeMillis();
        List<Map<String, Object>> source = extract.getOutputData();
        if (source == null || source.isEmpty()) {
            step.setStatus("SUCCESS");
            step.setInputRows(0);
            step.setOutputRows(0);
            step.setOutputColumns(new ArrayList<>());
            step.setOutputData(source);
            step.setDurationMs(System.currentTimeMillis() - s);
            return source == null ? new ArrayList<>() : source;
        }

        step.setInputRows(source.size());
        List<Map<String, Object>> transformed;
        if (mappings == null || mappings.isEmpty()) {
            transformed = source;
            step.setStatus("SUCCESS");
            step.setOutputRows(transformed.size());
            step.setOutputColumns(extract.getOutputColumns());
            step.setOutputData(transformed);
            step.setDurationMs(System.currentTimeMillis() - s);
            return transformed;
        }

        transformed = new ArrayList<>();
        for (Map<String, Object> row : source) {
            Map<String, Object> out = new LinkedHashMap<>();
            for (EtlColumnMapping m : mappings) {
                if (!"Y".equals(m.getEnabled())) {
                    continue;
                }
                Object value = row.get(m.getSourceColumn());
                if (value == null && m.getDefaultValue() != null) {
                    value = m.getDefaultValue();
                }
                out.put(m.getTargetColumn(), value);
            }
            transformed.add(out);
        }
        step.setStatus("SUCCESS");
        step.setOutputRows(transformed.size());
        step.setOutputColumns(collectColumns(transformed));
        step.setOutputData(transformed);
        step.setDurationMs(System.currentTimeMillis() - s);
        return transformed;
    }

    /** 写入步骤 */
    private void executeLoad(EtlTaskConfig task, List<EtlColumnMapping> mappings,
                             List<Map<String, Object>> data, StepResult step) {
        long s = System.currentTimeMillis();
        try {
            step.setInputRows(data == null ? 0 : data.size());
            if (data == null || data.isEmpty()) {
                step.setStatus("SUCCESS");
                step.setOutputRows(0);
                step.setDurationMs(System.currentTimeMillis() - s);
                return;
            }
            DataWriter writer = writerFactory.getWriter(task.getWriteMode());
            writer.write(data, task, mappings, dataSourceManager);
            step.setStatus("SUCCESS");
            step.setOutputRows(data.size());
            step.setDurationMs(System.currentTimeMillis() - s);
        } catch (Exception e) {
            log.error("写入步骤失败", e);
            step.setStatus("FAILED");
            step.setErrorMessage(e.getMessage());
            step.setDurationMs(System.currentTimeMillis() - s);
        }
    }

    /** 提取列名 */
    private List<String> collectColumns(List<Map<String, Object>> data) {
        List<String> columns = new ArrayList<>();
        if (data != null && !data.isEmpty()) {
            columns.addAll(data.get(0).keySet());
        }
        return columns;
    }
}
