package com.etl.service.reader;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class ViewReader extends AbstractJdbcReader {

    @Override
    public String getSourceType() {
        return "VIEW";
    }

    @Override
    public List<Map<String, Object>> readAll() {
        String viewName = taskConfig.getSourceView();
        if (viewName == null || viewName.trim().isEmpty()) {
            throw new IllegalArgumentException("视图名称为空");
        }
        String sql = "SELECT * FROM " + viewName;
        log.info("查询视图: {}", viewName);
        return jdbcTemplate.queryForList(sql);
    }

    @Override
    public List<Map<String, Object>> preview(int limit) {
        String viewName = taskConfig.getSourceView();
        String sql = "SELECT * FROM " + viewName + " WHERE ROWNUM <= " + limit;
        return jdbcTemplate.queryForList(sql);
    }
}
