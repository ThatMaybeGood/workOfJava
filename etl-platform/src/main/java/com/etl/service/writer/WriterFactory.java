package com.etl.service.writer;

import com.etl.entity.EtlTaskConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@Component
public class WriterFactory {

    @Autowired
    private InsertWriter insertWriter;

    @Autowired
    private MergeWriter mergeWriter;

    private final Map<String, DataWriter> writerMap = new HashMap<>();

    @PostConstruct
    public void init() {
        writerMap.put(insertWriter.getWriteMode(), insertWriter);
        writerMap.put(mergeWriter.getWriteMode(), mergeWriter);
    }

    public DataWriter getWriter(String writeMode) {
        DataWriter writer = writerMap.get(writeMode.toUpperCase());
        if (writer == null) {
            throw new IllegalArgumentException("不支持的写入模式: " + writeMode);
        }
        return writer;
    }
}
