package com.etl.service.reader;

import com.etl.entity.DatasourceConfig;
import com.etl.entity.EtlTaskConfig;
import com.etl.service.core.DataSourceManager;
import com.etl.util.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
public abstract class AbstractJdbcReader implements DataSourceReader {

    protected EtlTaskConfig taskConfig;
    protected DataSourceManager dataSourceManager;
    protected JdbcTemplate jdbcTemplate;

    @Override
    public void init(EtlTaskConfig task, DataSourceManager dataSourceManager) {
        this.taskConfig = task;
        this.dataSourceManager = dataSourceManager;
        this.jdbcTemplate = dataSourceManager.getJdbcTemplate(task.getSourceDsName());
    }

    @Override
    public List<Map<String, Object>> readBatch(int batchSize) {
        throw new UnsupportedOperationException("readBatch not implemented");
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
            log.warn("数据源连接测试失败: {}", config.getDsName(), e);
            return false;
        }
    }

    @Override
    public List<Map<String, Object>> preview(int limit) {
        return Collections.emptyList();
    }

    @Override
    public void close() {
        // 默认不关闭，由DataSourceManager管理连接池生命周期
    }

    protected Map<String, Object> parseSourceParams() {
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
