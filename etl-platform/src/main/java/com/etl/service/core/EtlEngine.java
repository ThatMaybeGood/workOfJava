package com.etl.service.core;

import com.etl.entity.EtlColumnMapping;
import com.etl.entity.EtlExecutionLog;
import com.etl.entity.EtlTaskConfig;
import com.etl.service.admin.EtlColumnMappingService;
import com.etl.service.admin.EtlExecutionLogService;
import com.etl.service.admin.EtlTaskConfigService;
import com.etl.service.reader.DataSourceReader;
import com.etl.service.reader.ReaderFactory;
import com.etl.service.writer.DataWriter;
import com.etl.service.writer.WriterFactory;
import com.etl.util.PrimaryKeyGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
public class EtlEngine {

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

    @Autowired
    private EtlExecutionLogService executionLogService;

    public void execute(String taskCode, String triggerType, String triggerUser) {
        long startTime = System.currentTimeMillis();
        String executionId = triggerType + "_" + PrimaryKeyGenerator.nextIdStr();
        log.info("开始执行ETL任务: {}, 触发方式: {}", taskCode, triggerType);

        DataSourceReader reader = null;
        EtlTaskConfig task = null;
        int totalRows = 0;
        try {
            // 1. 加载任务配置
            task = taskConfigService.getByTaskCode(taskCode);
            if (task == null) {
                throw new RuntimeException("任务不存在: " + taskCode);
            }
            if (!"Y".equals(task.getEnabled())) {
                log.warn("任务 [{}] 已禁用，跳过执行", taskCode);
                recordExecution(task, executionId, startTime, "SKIPPED", "任务已禁用", 0, triggerType, triggerUser);
                return;
            }

            // 2. 加载字段映射
            List<EtlColumnMapping> mappings = columnMappingService.listByTaskCode(taskCode);
            log.info("任务 [{}] 加载 {} 个字段映射", taskCode, mappings.size());

            // 3. 获取Reader和Writer
            reader = readerFactory.getReader(task.getSourceType());
            DataWriter writer = writerFactory.getWriter(task.getWriteMode());

            // 4. 初始化Reader
            reader.init(task, dataSourceManager);

            // 5. 读取数据
            log.info("任务 [{}] 开始读取数据, 类型: {}", taskCode, task.getSourceType());
            List<Map<String, Object>> data = reader.readAll();
            totalRows = data.size();
            log.info("任务 [{}] 读取完成, 共 {} 条数据", taskCode, totalRows);

            if (data.isEmpty()) {
                log.info("任务 [{}] 读取到0条数据, 跳过写入", taskCode);
                recordExecution(task, executionId, startTime, "SUCCESS", "读取到0条数据", 0, triggerType, triggerUser);
                return;
            }

            // 6. 写入数据
            log.info("任务 [{}] 开始写入数据, 模式: {}", taskCode, task.getWriteMode());
            writer.write(data, task, mappings, dataSourceManager);

            long duration = (System.currentTimeMillis() - startTime) / 1000;
            log.info("任务 [{}] 执行完成, 耗时 {} 秒, 共处理 {} 条数据", taskCode, duration, totalRows);
            recordExecution(task, executionId, startTime, "SUCCESS", null, totalRows, triggerType, triggerUser);

        } catch (Exception e) {
            log.error("任务 [{}] 执行失败", taskCode, e);
            recordExecution(task, executionId, startTime, "FAILED", e.getMessage(), totalRows, triggerType, triggerUser);
            throw new RuntimeException("ETL任务执行失败: " + e.getMessage(), e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception ex) {
                    log.warn("关闭读取器失败: {}", ex.getMessage());
                }
            }
        }
    }

    private void recordExecution(EtlTaskConfig task, String executionId, long startMs,
 String status, String errorMessage, int totalRows,
 String triggerType, String triggerUser) {
        if (task == null) {
            log.warn("recordExecution skipped: task is null");
            return;
        }
        try {
            EtlExecutionLog elog = new EtlExecutionLog();
            elog.setTaskCode(task.getTaskCode());
            elog.setTaskName(task.getTaskName());
            elog.setExecutionId(executionId);
            LocalDateTime now = LocalDateTime.now();
            elog.setStartTime(now.minusSeconds((System.currentTimeMillis() - startMs) / 1000));
            elog.setEndTime(now);
            elog.setCreatedTime(now);
            elog.setStatus(status);
            elog.setTriggerType(triggerType);
            elog.setTriggerUser(triggerUser);
            elog.setErrorMessage(errorMessage);
            elog.setExecutionDuration((System.currentTimeMillis() - startMs) / 1000);
            elog.setTotalRows((long) totalRows);
            elog.setSuccessRows("SUCCESS".equals(status) ? (long) totalRows : 0L);
            elog.setFailedRows("FAILED".equals(status) ? 1L : 0L);
            executionLogService.save(elog);
        } catch (Exception e) {
            log.warn("Failed to record execution log: {}", e.getMessage(), e);
        }
    }

    public void executePreview(String taskCode, int limit) {
        log.info("预览任务 [{}], 限制 {} 条", taskCode, limit);
        DataSourceReader reader = null;
        try {
            EtlTaskConfig task = taskConfigService.getByTaskCode(taskCode);
            if (task == null) {
                throw new RuntimeException("任务不存在: " + taskCode);
            }

            reader = readerFactory.getReader(task.getSourceType());
            reader.init(task, dataSourceManager);

            List<Map<String, Object>> previewData = reader.preview(limit);
            log.info("任务 [{}] 预览数据 {} 条", taskCode, previewData.size());

        } catch (Exception e) {
            log.error("任务 [{}] 预览失败", taskCode, e);
            throw new RuntimeException("预览失败: " + e.getMessage(), e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception ex) {
                    log.warn("关闭读取器失败: {}", ex.getMessage());
                }
            }
        }
    }
}
