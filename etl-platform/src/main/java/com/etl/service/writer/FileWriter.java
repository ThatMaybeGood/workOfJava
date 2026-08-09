package com.etl.service.writer;

import com.etl.dto.StepConfig;
import com.etl.entity.EtlColumnMapping;
import com.etl.entity.EtlStepColumnMapping;
import com.etl.entity.EtlTaskConfig;
import com.etl.service.core.DataSourceManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * 文件写入器：支持 CSV / JSON / Excel 格式输出。
 * writeMode 对应 FILE_CSV / FILE_JSON / FILE_EXCEL。
 */
@Slf4j
@Component
public class FileWriter implements DataWriter {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public String getWriteMode() {
        return "FILE_CSV"; // 默认注册为 FILE_CSV，同时也处理 FILE_JSON / FILE_EXCEL
    }

    @Override
    public void write(List<Map<String, Object>> data, EtlTaskConfig task,
                      List<EtlColumnMapping> mappings, DataSourceManager dataSourceManager) {
        String filePath = task.getFilePath();
        String format = task.getFileFormat();
        String delimiter = task.getFileDelimiter() != null ? task.getFileDelimiter() : ",";
        String encoding = task.getFileEncoding() != null ? task.getFileEncoding() : "UTF-8";
        String header = task.getFileHeader();

        writeData(data, filePath, format, delimiter, encoding, header, mappings);
    }

    @Override
    public void writeWithConfig(List<Map<String, Object>> data, StepConfig config,
                                 List<EtlStepColumnMapping> mappings, DataSourceManager dataSourceManager) {
        String filePath = config.getFilePath();
        String format = config.getFileFormat();
        String delimiter = config.getFileDelimiter() != null ? config.getFileDelimiter() : ",";
        String encoding = config.getFileEncoding() != null ? config.getFileEncoding() : "UTF-8";
        String header = config.getFileHeader();

        List<EtlColumnMapping> oldMappings = convertMappings(mappings);
        writeData(data, filePath, format, delimiter, encoding, header, oldMappings);
    }

    private void writeData(List<Map<String, Object>> data, String filePath, String format,
                           String delimiter, String encoding, String headerFlag,
                           List<EtlColumnMapping> mappings) {
        if (data == null || data.isEmpty()) {
            log.warn("无数据可写入文件");
            return;
        }
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new IllegalArgumentException("文件路径为空");
        }

        String fmt = (format != null) ? format.toUpperCase() : "CSV";
        try {
            switch (fmt) {
                case "CSV":
                    writeCsv(data, filePath, delimiter, encoding, headerFlag, mappings);
                    break;
                case "JSON":
                    writeJson(data, filePath, encoding);
                    break;
                default:
                    throw new IllegalArgumentException("不支持的文件格式: " + format);
            }
            log.info("数据已写入文件 [{}], 共 {} 条", filePath, data.size());
        } catch (IOException e) {
            throw new RuntimeException("文件写入失败: " + e.getMessage(), e);
        }
    }

    private void writeCsv(List<Map<String, Object>> data, String filePath, String delimiter,
                          String encoding, String headerFlag, List<EtlColumnMapping> mappings)
            throws IOException {
        Charset charset = Charset.forName(encoding);
        List<String> columns = getOutputColumns(data.get(0), mappings);

        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(filePath), charset))) {
            // 写表头
            if (!"N".equals(headerFlag)) {
                writer.write(String.join(delimiter, columns));
                writer.newLine();
            }
            // 写数据行
            for (Map<String, Object> row : data) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < columns.size(); i++) {
                    if (i > 0) sb.append(delimiter);
                    Object value = row.get(columns.get(i));
                    if (value != null) {
                        String str = value.toString();
                        // 如果包含分隔符或引号，用双引号包裹
                        if (str.contains(delimiter) || str.contains("\"") || str.contains("\n")) {
                            str = "\"" + str.replace("\"", "\"\"") + "\"";
                        }
                        sb.append(str);
                    }
                }
                writer.write(sb.toString());
                writer.newLine();
            }
        }
    }

    private void writeJson(List<Map<String, Object>> data, String filePath, String encoding)
            throws IOException {
        Charset charset = Charset.forName(encoding);
        OBJECT_MAPPER.writerWithDefaultPrettyPrinter()
                .writeValue(new OutputStreamWriter(new FileOutputStream(filePath), charset), data);
    }

    private List<String> getOutputColumns(Map<String, Object> sampleRow, List<EtlColumnMapping> mappings) {
        if (mappings != null && !mappings.isEmpty()) {
            List<String> columns = new ArrayList<>();
            for (EtlColumnMapping m : mappings) {
                if ("Y".equals(m.getEnabled())) {
                    columns.add(m.getTargetColumn());
                }
            }
            return columns;
        }
        return new ArrayList<>(sampleRow.keySet());
    }

    private List<EtlColumnMapping> convertMappings(List<EtlStepColumnMapping> stepMappings) {
        if (stepMappings == null) return null;
        List<EtlColumnMapping> list = new ArrayList<>();
        for (EtlStepColumnMapping sm : stepMappings) {
            EtlColumnMapping m = new EtlColumnMapping();
            m.setSourceColumn(sm.getSourceColumn());
            m.setTargetColumn(sm.getTargetColumn());
            m.setEnabled(sm.getEnabled());
            list.add(m);
        }
        return list;
    }
}
