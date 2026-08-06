package com.etl.service.reader;

import com.etl.entity.DatasourceConfig;
import com.etl.entity.EtlTaskConfig;
import com.etl.service.core.DataSourceManager;

import java.util.List;
import java.util.Map;

public interface DataSourceReader {

    String getSourceType();

    void init(EtlTaskConfig task, DataSourceManager dataSourceManager);

    List<Map<String, Object>> readBatch(int batchSize);

    List<Map<String, Object>> readAll();

    long getTotalCount();

    boolean testConnection(DatasourceConfig config, DataSourceManager dataSourceManager);

    List<Map<String, Object>> preview(int limit);

    void close();
}
