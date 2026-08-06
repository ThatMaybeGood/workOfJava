package com.etl.service.reader;

import com.etl.entity.DatasourceConfig;
import com.etl.entity.EtlTaskConfig;
import com.etl.service.core.DataSourceManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class FileReader implements DataSourceReader {

    private EtlTaskConfig taskConfig;
    private java.io.BufferedReader bufferedReader;
    private int currentLine = 0;

    @Override
    public String getSourceType() {
        return "FILE";
    }

    @Override
    public void init(EtlTaskConfig task, DataSourceManager dataSourceManager) {
        this.taskConfig = task;
        try {
            File file = new File(task.getFilePath());
            String encoding = task.getFileEncoding() != null ? task.getFileEncoding() : "UTF-8";
            this.bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(file), encoding));
            this.currentLine = 0;

            // 跳过标题行
            if ("Y".equals(task.getFileHeader())) {
                bufferedReader.readLine();
                currentLine++;
            }
        } catch (Exception e) {
            throw new RuntimeException("初始化文件读取器失败", e);
        }
    }

    @Override
    public List<Map<String, Object>> readAll() {
        List<Map<String, Object>> allResults = new ArrayList<>();
        String line;
        try {
            while ((line = bufferedReader.readLine()) != null) {
                Map<String, Object> row = parseLine(line);
                if (row != null) {
                    allResults.add(row);
                }
                currentLine++;
            }
        } catch (Exception e) {
            log.error("读取文件失败", e);
        }
        return allResults;
    }

    @Override
    public List<Map<String, Object>> readBatch(int batchSize) {
        List<Map<String, Object>> batch = new ArrayList<>();
        try {
            String line;
            int count = 0;
            while (count < batchSize && (line = bufferedReader.readLine()) != null) {
                Map<String, Object> row = parseLine(line);
                if (row != null) {
                    batch.add(row);
                    count++;
                }
                currentLine++;
            }
        } catch (Exception e) {
            log.error("批量读取文件失败", e);
        }
        return batch;
    }

    private Map<String, Object> parseLine(String line) {
        String format = taskConfig.getFileFormat();
        if ("CSV".equalsIgnoreCase(format)) {
            return parseCsv(line);
        } else if ("JSON".equalsIgnoreCase(format)) {
            return parseJson(line);
        }
        return null;
    }

    private Map<String, Object> parseCsv(String line) {
        String delimiter = taskConfig.getFileDelimiter() != null ? taskConfig.getFileDelimiter() : ",";
        String[] parts = line.split(delimiter);
        Map<String, Object> row = new HashMap<>();
        for (int i = 0; i < parts.length; i++) {
            row.put("column_" + i, parts[i].trim());
        }
        return row;
    }

    private Map<String, Object> parseJson(String line) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(line, HashMap.class);
        } catch (Exception e) {
            log.warn("JSON行解析失败: {}", line);
            return null;
        }
    }

    @Override
    public long getTotalCount() {
        return -1;
    }

    @Override
    public boolean testConnection(DatasourceConfig config, DataSourceManager dataSourceManager) {
        try {
            File file = new File(config.getJdbcUrl());
            return file.exists() && file.canRead();
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public List<Map<String, Object>> preview(int limit) {
        return readAll().subList(0, Math.min(limit, readAll().size()));
    }

    @Override
    public void close() {
        if (bufferedReader != null) {
            try {
                bufferedReader.close();
            } catch (Exception e) {
                log.warn("关闭文件读取器失败", e);
            }
        }
    }
}
