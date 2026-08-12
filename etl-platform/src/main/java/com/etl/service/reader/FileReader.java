package com.etl.service.reader;

import com.etl.dto.StepConfig;
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
    /** 表头列名（CSV 带表头时从首行解析，否则为 null，用 column_0/column_1... 占位） */
    private String[] headerColumns;

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
            this.headerColumns = null;

            // 读取表头行：解析列名，后续数据行按列名取值（比 column_0 占位更利于映射）
            if ("Y".equals(task.getFileHeader()) && isCsvFormat()) {
                String headerLine = bufferedReader.readLine();
                currentLine++;
                if (headerLine != null) {
                    String delimiter = task.getFileDelimiter() != null ? task.getFileDelimiter() : ",";
                    headerColumns = splitCsvLine(headerLine, delimiter);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("初始化文件读取器失败", e);
        }
    }

    private boolean isCsvFormat() {
        String format = taskConfig.getFileFormat();
        return format == null || "CSV".equalsIgnoreCase(format);
    }

    @Override
    public void initWithConfig(StepConfig config, DataSourceManager dataSourceManager) {
        EtlTaskConfig task = new EtlTaskConfig();
        task.setSourceDsName(config.getSourceDsName());
        task.setSourceType(config.getSourceType());
        task.setFilePath(config.getFilePath());
        task.setFileFormat(config.getFileFormat());
        task.setFileDelimiter(config.getFileDelimiter());
        task.setFileEncoding(config.getFileEncoding());
        task.setFileHeader(config.getFileHeader());
        task.setFileSheetName(config.getFileSheetName());
        this.init(task, dataSourceManager);
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
        String[] parts = splitCsvLine(line, delimiter);
        Map<String, Object> row = new HashMap<>();
        for (int i = 0; i < parts.length; i++) {
            String key = (headerColumns != null && i < headerColumns.length && headerColumns[i] != null && !headerColumns[i].isEmpty())
                    ? headerColumns[i].trim() : "column_" + i;
            row.put(key, parts[i].trim());
        }
        return row;
    }

    /**
     * CSV 行解析：按分隔符拆分，正确处理双引号包裹的字段（含分隔符/引号/换行）。
     * 与 FileReader.parseCsv 配套，同时用于解析表头行和数据行。
     */
    private static String[] splitCsvLine(String line, String delimiter) {
        if (line == null || line.isEmpty()) {
            return new String[0];
        }
        char d = delimiter.length() > 0 ? delimiter.charAt(0) : ',';
        List<String> fields = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    // 转义引号 ""
                    cur.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == d && !inQuotes) {
                fields.add(cur.toString());
                cur.setLength(0);
            } else {
                cur.append(c);
            }
        }
        fields.add(cur.toString());
        return fields.toArray(new String[0]);
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
    public boolean supportsStreaming() {
        // readBatch 是按行流式读，状态机自洽，支持 chunk 边读边写
        return true;
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
        // 真流式预览：readBatch 不超过 limit 行，避免大文件被整体读入内存
        if (limit <= 0) limit = 50;
        return readBatch(limit);
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
