package com.etl.service.reader;

import com.etl.entity.DatasourceConfig;
import com.etl.entity.EtlTaskConfig;
import com.etl.service.core.DataSourceManager;
import com.etl.util.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import oracle.jdbc.OracleTypes;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class ProcedureReader implements DataSourceReader {

    private EtlTaskConfig taskConfig;
    private DataSourceManager dataSourceManager;
    private JdbcTemplate jdbcTemplate;
    private ResultSet currentResultSet;
    private boolean hasMore = true;

    @Override
    public String getSourceType() {
        return "PROCEDURE";
    }

    @Override
    public void init(EtlTaskConfig task, DataSourceManager dataSourceManager) {
        this.taskConfig = task;
        this.dataSourceManager = dataSourceManager;
        this.jdbcTemplate = dataSourceManager.getJdbcTemplate(task.getSourceDsName());
    }

    @Override
    public List<Map<String, Object>> readAll() {
        String procedureName = taskConfig.getSourceProcedure();
        if (procedureName == null || procedureName.trim().isEmpty()) {
            throw new IllegalArgumentException("存储过程名称为空");
        }
        log.info("调用存储过程: {}", procedureName);

        Map<String, Object> params = parseSourceParams();
        String callSql = buildCallSql(procedureName, params);

        return jdbcTemplate.execute(callSql, (CallableStatement cs) -> {
            // 注册OUT参数为游标
            cs.registerOutParameter(1, OracleTypes.CURSOR);

            // 设置IN参数
            int paramIndex = 2;
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                cs.setObject(paramIndex++, entry.getValue());
            }

            cs.execute();

            ResultSet rs = (ResultSet) cs.getObject(1);
            List<Map<String, Object>> result = new ArrayList<>();
            if (rs != null) {
                try {
                    ResultSetMetaData metaData = rs.getMetaData();
                    int columnCount = metaData.getColumnCount();
                    while (rs.next()) {
                        Map<String, Object> row = new HashMap<>();
                        for (int i = 1; i <= columnCount; i++) {
                            row.put(metaData.getColumnLabel(i), rs.getObject(i));
                        }
                        result.add(row);
                    }
                } finally {
                    rs.close();
                }
            }
            return result;
        });
    }

    @Override
    public List<Map<String, Object>> readBatch(int batchSize) {
        return readAll();
    }

    @Override
    public long getTotalCount() {
        return -1;
    }

    @Override
    public boolean testConnection(DatasourceConfig config, DataSourceManager dataSourceManager) {
        try {
            JdbcTemplate testTemplate = dataSourceManager.getJdbcTemplate(config.getDsName());
            testTemplate.queryForObject(config.getValidationQuery() != null ? config.getValidationQuery() : "SELECT 1 FROM DUAL", Integer.class);
            return true;
        } catch (Exception e) {
            log.warn("存储过程数据源连接测试失败: {}", config.getDsName(), e);
            return false;
        }
    }

    @Override
    public List<Map<String, Object>> preview(int limit) {
        return readAll().subList(0, Math.min(limit, readAll().size()));
    }

    @Override
    public void close() {
        // 游标在readAll中已关闭
    }

    private String buildCallSql(String procedureName, Map<String, Object> params) {
        StringBuilder sb = new StringBuilder();
        sb.append("{call ").append(procedureName).append("(?");
        for (int i = 0; i < params.size(); i++) {
            sb.append(", ?");
        }
        sb.append(")}");
        return sb.toString();
    }

    private Map<String, Object> parseSourceParams() {
        if (taskConfig.getSourceParams() != null && !taskConfig.getSourceParams().isEmpty()) {
            try {
                return JsonUtil.toMap(taskConfig.getSourceParams());
            } catch (Exception e) {
                log.warn("解析sourceParams失败", e);
            }
        }
        return Collections.emptyMap();
    }
}
