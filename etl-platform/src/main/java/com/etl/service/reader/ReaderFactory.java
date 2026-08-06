package com.etl.service.reader;

import com.etl.entity.EtlTaskConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@Component
public class ReaderFactory {

    @Autowired
    private SqlReader sqlReader;

    @Autowired
    private ViewReader viewReader;

    @Autowired
    private TableReader tableReader;

    @Autowired
    private ProcedureReader procedureReader;

    @Autowired
    private HttpReader httpReader;

    @Autowired
    private FileReader fileReader;

    private final Map<String, DataSourceReader> readerMap = new HashMap<>();

    @PostConstruct
    public void init() {
        readerMap.put(sqlReader.getSourceType(), sqlReader);
        readerMap.put(viewReader.getSourceType(), viewReader);
        readerMap.put(tableReader.getSourceType(), tableReader);
        readerMap.put(procedureReader.getSourceType(), procedureReader);
        readerMap.put(httpReader.getSourceType(), httpReader);
        readerMap.put(fileReader.getSourceType(), fileReader);
    }

    public DataSourceReader getReader(String sourceType) {
        DataSourceReader reader = readerMap.get(sourceType.toUpperCase());
        if (reader == null) {
            throw new IllegalArgumentException("不支持的读取类型: " + sourceType);
        }
        return reader;
    }
}
