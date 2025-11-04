package com.showexcel.dto;

import com.showexcel.model.CellMergeConfig;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Mine
 * @version 1.0
 * 描述: 用于返回扁平化的现金统计表数据，rows和mergeConfigs字段直接展开为对象
 * @date 2025/10/30 16:30
 */
public class CashStatisticsTableDTO {
    private String title; // 表格标题
    private List<String> headers; // 表头
    private Map<String, Object> rows; // 数据行（直接展开的对象）
    private Map<String, Object> mergeConfigs; // 合并配置（直接展开的对象）
    private int rowCount; // 行数
    private int colCount; // 列数

    // 构造函数
    public CashStatisticsTableDTO() {
        this.headers = new ArrayList<>();
        this.rows = new LinkedHashMap<>();
        this.mergeConfigs = new LinkedHashMap<>();
    }

    // 添加表头
    public void addHeader(String header) {
        this.headers.add(header);
    }

    // 添加数据行（直接添加对象）
    public void addRow(String key, Object row) {
        this.rows.put(key, row);
    }

    // 添加合并配置（直接添加对象）
    public void addMergeConfig(String key, Object config) {
        this.mergeConfigs.put(key, config);
    }

    // getter 和 setter
    public List<String> getHeaders() { return headers; }
    public void setHeaders(List<String> headers) { this.headers = headers; }

    public Map<String, Object> getRows() { return rows; }
    public void setRows(Map<String, Object> rows) { this.rows = rows; }

    public Map<String, Object> getMergeConfigs() { return mergeConfigs; }
    public void setMergeConfigs(Map<String, Object> mergeConfigs) { this.mergeConfigs = mergeConfigs; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }


    public int getRowCount() { return rowCount; }
    public void setRowCount(int rowCount) { this.rowCount = rowCount; }

    public int getColCount() { return colCount; }
    public void setColCount(int colCount) { this.colCount = colCount; }
}
