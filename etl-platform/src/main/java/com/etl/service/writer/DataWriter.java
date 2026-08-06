package com.etl.service.writer;

import com.etl.entity.EtlColumnMapping;
import com.etl.entity.EtlTaskConfig;
import com.etl.service.core.DataSourceManager;

import java.util.List;
import java.util.Map;

public interface DataWriter {

    String getWriteMode();

    void write(List<Map<String, Object>> data, EtlTaskConfig task,
               List<EtlColumnMapping> mappings, DataSourceManager dataSourceManager);
}
