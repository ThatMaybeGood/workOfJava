package com.reports.etl.service.extractor;

import com.reports.etl.entity.EtlProcConfig;
import com.reports.etl.entity.EtlTask;
import com.reports.etl.service.core.EtlMetaDao;
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

    private final EtlMetaDao metaDao;
    private final EtlDataSourceRegistry registry;

    public ProcedureExtractor(EtlMetaDao metaDao, EtlDataSourceRegistry registry) {
        this.metaDao = metaDao;
        this.registry = registry;
    }

    @PostConstruct
    public void init() {
        log.info("ProcedureExtractor 初始化完成");
    }

    public Map<String, Object> extract(Long taskId, int batchSize) {
        EtlProcConfig procConfig = metaDao.getProcConfig(taskId);
        if (procConfig == null) throw new RuntimeException("存储过程配置不存在: " + taskId);

        EtlTask task = metaDao.getTask(taskId);
        Long sourceDsId = task != null ? task.getSourceDsId() : null;
        return extract(procConfig, sourceDsId, batchSize);
    }

    /**
     * 以配置对象为入参的抽取（供 SourceExtractorFacade 复用）
     */
    public Map<String, Object> extract(EtlProcConfig procConfig, Long sourceDsId, int batchSize) {
        int maxRows = procConfig.getMaxRows() != null ? procConfig.getMaxRows() : 10000;
        Map<String, Object> callResult = callProc(procConfig, sourceDsId, maxRows);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> allRows = (List<Map<String, Object>>) callResult.get("rows");

        List<Map<String, Object>> batch = allRows.subList(0, Math.min(batchSize, allRows.size()));
        boolean more = allRows.size() > batchSize;

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("rows", batch);
        result.put("totalRows", allRows.size());
        result.put("more", more);
        result.put("columns", batch.isEmpty() ? new ArrayList<>() : new ArrayList<>(batch.get(0).keySet()));
        return result;
    }

    /**
     * 以游标 ResultSetMetaData 输出列元数据（供结构树分析：平铺，每列一个节点）
     * 返回 [{name, jdbcType, typeName}]
     */
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> describeColumns(EtlProcConfig procConfig, Long sourceDsId) {
        Map<String, Object> callResult = callProc(procConfig, sourceDsId, 1);
        return (List<Map<String, Object>>) callResult.get("columnsMeta");
    }

    /**
     * 执行存储过程，返回 rows + columnsMeta（基于游标 ResultSetMetaData）
     */
    private Map<String, Object> callProc(EtlProcConfig procConfig, Long sourceDsId, int maxRows) {
        if (sourceDsId == null) throw new RuntimeException("任务未配置抽取源数据源");

        DataSource pool = registry.getPool(sourceDsId);
        List<Map<String, Object>> allRows = new ArrayList<>();
        List<Map<String, Object>> columnsMeta = new ArrayList<>();

        try (Connection conn = pool.getConnection()) {
            String callTemplate = procConfig.getCallTemplate();
            if (callTemplate == null || callTemplate.isEmpty()) {
                callTemplate = "{call " + procConfig.getProcName() + "()}";
            }

            int cursorParamIdx = procConfig.getCursorParamIdx() != null ? procConfig.getCursorParamIdx() : 1;

            CallableStatement stmt = conn.prepareCall(callTemplate);

            // 注册游标出参（Oracle REF CURSOR）
            stmt.registerOutParameter(cursorParamIdx, Types.OTHER);

            // 设置 IN 参数（from inParamsJson, 逐个设置）
            setInParams(stmt, procConfig.getInParamsJson(), cursorParamIdx);

            stmt.execute();

            // 读取游标结果
            try (ResultSet cursor = (ResultSet) stmt.getObject(cursorParamIdx)) {
                if (cursor == null) throw new RuntimeException("存储过程游标为空");
                ResultSetMetaData meta = cursor.getMetaData();
                int columnCount = meta.getColumnCount();

                for (int i = 1; i <= columnCount; i++) {
                    Map<String, Object> col = new LinkedHashMap<>();
                    col.put("name", meta.getColumnLabel(i));
                    col.put("jdbcType", meta.getColumnType(i));
                    col.put("typeName", meta.getColumnTypeName(i));
                    columnsMeta.add(col);
                }

                while (cursor.next() && allRows.size() < maxRows) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    for (int i = 1; i <= columnCount; i++) {
                        String colName = meta.getColumnLabel(i);
                        Object value = cursor.getObject(i);
                        row.put(colName, value);
                    }
                    allRows.add(row);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("存储过程执行失败: " + e.getMessage(), e);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("rows", allRows);
        result.put("columnsMeta", columnsMeta);
        return result;
    }

    private void setInParams(CallableStatement stmt, String inParamsJson, int cursorParamIdx) {
        if (inParamsJson == null || inParamsJson.isEmpty()) return;
        try {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> params = (List<Map<String, Object>>)
                    new com.fasterxml.jackson.databind.ObjectMapper()
                            .readValue(inParamsJson, List.class);
            for (int i = 0; i < params.size(); i++) {
                Map<String, Object> param = params.get(i);
                int idx = param.get("index") != null
                        ? Integer.parseInt(param.get("index").toString())
                        : i + 1;
                // 跳过游标出参位置（通常游标是最后一个参数）
                if (idx == cursorParamIdx) continue;
                Object value = param.get("value");
                stmt.setObject(idx, value);
            }
        } catch (Exception e) {
            log.warn("IN参数绑定失败（忽略）: {}", e.getMessage());
        }
    }
}