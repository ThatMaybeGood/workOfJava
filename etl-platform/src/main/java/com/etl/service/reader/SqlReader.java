package com.etl.service.reader;

import com.etl.entity.EtlTaskConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class SqlReader extends AbstractJdbcReader {

    @Override
    public String getSourceType() {
        return "SQL";
    }

    @Override
    public List<Map<String, Object>> readAll() {
        String sql = taskConfig.getSourceSql();
        if (sql == null || sql.trim().isEmpty()) {
            throw new IllegalArgumentException("SQL语句为空");
        }
        log.info("执行SQL查询: {}", sql);
        return jdbcTemplate.queryForList(sql);
    }

    @Override
    public List<Map<String, Object>> readBatch(int batchSize) {
        // 对于SQL，简单实现：先读取全部，再分页
        List<Map<String, Object>> all = readAll();
        return all;
    }

    @Override
    public List<Map<String, Object>> preview(int limit) {
        String sql = taskConfig.getSourceSql();
        if (sql == null || sql.trim().isEmpty()) {
            throw new IllegalArgumentException("SQL语句为空");
        }
        // Oracle中使用ROWNUM或FETCH FIRST
        String previewSql = wrapLimitSql(sql, limit);
        return jdbcTemplate.queryForList(previewSql);
    }

    private String wrapLimitSql(String sql, int limit) {
        if (sql.toUpperCase().contains("ROWNUM")) {
            return sql;
        }
        return "SELECT * FROM (" + sql + ") WHERE ROWNUM <= " + limit;
    }
}
