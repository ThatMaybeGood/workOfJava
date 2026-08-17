package com.reports.etl.service.extractor;

import com.reports.etl.entity.EtlProcConfig;
import com.reports.etl.entity.EtlTask;
import com.reports.etl.mapper.EtlProcConfigMapper;
import com.reports.etl.mapper.EtlTaskMapper;
import com.reports.etl.service.registry.EtlDataSourceRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.sql.*;
import java.util.*;

@Slf4j
@Component
public class ProcedureExtractor {

    private final EtlProcConfigMapper procConfigMapper;
    private final EtlTaskMapper taskMapper;
    private final EtlDataSourceRegistry registry;

    public ProcedureExtractor(EtlProcConfigMapper procConfigMapper, EtlTaskMapper taskMapper,
                              EtlDataSourceRegistry registry) {
        this.procConfigMapper = procConfigMapper;
        this.taskMapper = taskMapper;
        this.registry = registry;
    }

    @PostConstruct
    public void init() {
        log.info("ProcedureExtractor 初始化完成");
    }

    public Map<String, Object> extract(Long taskId, int batchSize) {
        EtlProcConfig procConfig = procConfigMapper.selectById(taskId);
        if (procConfig == null) throw new RuntimeException("存储过程配置不存在: " + taskId);

        EtlTask task = findTask(taskId);
        Long sourceDsId = task != null ? task.getSourceDsId() : null;
        if (sourceDsId == null) throw new RuntimeException("任务未配置抽取源数据源");

        DataSource pool = registry.getPool(sourceDsId);
        int maxPages = procConfig.getMaxPages() != null ? procConfig.getMaxPages() : 10;
        int maxRows = procConfig.getMaxRows() != null ? procConfig.getMaxRows() : 10000;

        List<Map<String, Object>> allRows = new ArrayList<>();

        try (Connection conn = pool.getConnection()) {
            String callTemplate = procConfig.getCallTemplate();
            if (callTemplate == null || callTemplate.isEmpty()) {
                callTemplate = "{call " + procConfig.getProcName() + "()}";
            }

            String cursorParamName = procConfig.getCursorParamName() != null ? procConfig.getCursorParamName() : "p_cursor";

            CallableStatement stmt = conn.prepareCall(callTemplate);

            // 注册游标参数
            int cursorParamIdx = procConfig.getCursorParamIdx() != null ? procConfig.getCursorParamIdx() : 1;
            stmt.registerOutParameter(cursorParamIdx, Types.OTHER); // Oracle REF CURSOR

            // 设置 IN 参数
            if (procConfig.getInParamsJson() != null && !procConfig.getInParamsJson().isEmpty()) {
                // 简单实现：按顺序绑定
                // 实际应解析 JSON 数组
            }

            stmt.execute();

            // 分批读取游标
            for (int page = 0; page < maxPages && allRows.size() < maxRows; page++) {
                ResultSet cursor = (ResultSet) stmt.getObject(cursorParamIdx);
                if (cursor == null) break;

                ResultSetMetaData meta = cursor.getMetaData();
                int columnCount = meta.getColumnCount();

                while (cursor.next() && allRows.size() < maxRows) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    for (int i = 1; i <= columnCount; i++) {
                        String colName = meta.getColumnLabel(i);
                        Object value = cursor.getObject(i);
                        row.put(colName, value);
                    }
                    allRows.add(row);
                }
                cursor.close();

                // 刷新游标获取下一批（如果是分页游标）
                if (page < maxPages - 1) {
                    // 重新执行或刷新游标逻辑取决于存储过程设计
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("存储过程执行失败: " + e.getMessage(), e);
        }

        List<Map<String, Object>> batch = allRows.subList(0, Math.min(batchSize, allRows.size()));
        boolean more = allRows.size() > batchSize;

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("rows", batch);
        result.put("totalRows", allRows.size());
        result.put("more", more);
        result.put("columns", batch.isEmpty() ? new ArrayList<>() : new ArrayList<>(batch.get(0).keySet()));
        return result;
    }

    private EtlTask findTask(Long taskId) {
        return taskMapper.selectById(taskId);
    }
}