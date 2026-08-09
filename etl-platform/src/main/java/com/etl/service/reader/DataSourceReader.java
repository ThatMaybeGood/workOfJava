package com.etl.service.reader;

import com.etl.dto.StepConfig;
import com.etl.entity.DatasourceConfig;
import com.etl.entity.EtlTaskConfig;
import com.etl.service.core.DataSourceManager;

import java.util.List;
import java.util.Map;

public interface DataSourceReader {

    String getSourceType();

    /** @deprecated 旧版 init，保留兼容，新代码请使用 {@link #initWithConfig} */
    @Deprecated
    void init(EtlTaskConfig task, DataSourceManager dataSourceManager);

    /** 新版 init：从 StepConfig 初始化读取器 */
    void initWithConfig(StepConfig config, DataSourceManager dataSourceManager);

    List<Map<String, Object>> readBatch(int batchSize);

    List<Map<String, Object>> readAll();

    long getTotalCount();

    boolean testConnection(DatasourceConfig config, DataSourceManager dataSourceManager);

    List<Map<String, Object>> preview(int limit);

    void close();
}
