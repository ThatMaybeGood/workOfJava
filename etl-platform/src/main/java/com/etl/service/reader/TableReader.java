package com.etl.service.reader;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class TableReader extends AbstractJdbcReader {

    @Override
    public String getSourceType() {
        return "TABLE";
    }

    @Override
    public List<Map<String, Object>> readAll() {
        String tableName = taskConfig.getSourceTable();
        if (tableName == null || tableName.trim().isEmpty()) {
            throw new IllegalArgumentException("表名为空");
        }
        String sql = "SELECT * FROM " + tableName;
        log.info("查询表: {}", tableName);
        return jdbcTemplate.queryForList(sql);
    }

    @Override
    public List<Map<String, Object>> preview(int limit) {
        String tableName = taskConfig.getSourceTable();
        String sql = "SELECT * FROM " + tableName + " WHERE ROWNUM <= " + limit;
        return jdbcTemplate.queryForList(sql);
    }
}
