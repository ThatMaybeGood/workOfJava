package com.etl.service.writer;

import com.etl.dto.StepConfig;
import com.etl.entity.EtlColumnMapping;
import com.etl.entity.EtlStepColumnMapping;
import com.etl.entity.EtlTaskConfig;
import com.etl.service.core.DataSourceManager;

import java.util.List;
import java.util.Map;

public interface DataWriter {

    String getWriteMode();

    /** @deprecated 旧版 write，保留兼容，新代码请使用 {@link #writeWithConfig} */
    @Deprecated
    void write(List<Map<String, Object>> data, EtlTaskConfig task,
               List<EtlColumnMapping> mappings, DataSourceManager dataSourceManager);

    /** 新版 write：从 StepConfig 获取目标配置 */
    void writeWithConfig(List<Map<String, Object>> data, StepConfig config,
                         List<EtlStepColumnMapping> mappings, DataSourceManager dataSourceManager);
}
