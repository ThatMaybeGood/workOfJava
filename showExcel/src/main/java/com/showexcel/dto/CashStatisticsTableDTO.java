package com.showexcel.dto;

import com.showexcel.model.CellMergeConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Mine
 * @version 1.0
 * 描述:
 * @date 2025/10/30 16:30
 */
// 表格数据包装类
public class CashStatisticsTableDTO {
    private List<String> headers; // 表头（包含序号列）
    private List<CashStatisticsRow> rows; // 数据行
    private List<CellMergeConfig> mergeConfigs; // 合并配置
    private String title = "现金统计表"; // 表格标题

    // 构造函数、getter、setter
    public CashStatisticsTableDTO() {
        this.headers = new ArrayList<>();
        this.rows = new ArrayList<>();
        this.mergeConfigs = new ArrayList<>();
    }

    // 添加表头
    public void addHeader(String header) {
        this.headers.add(header);
    }

    // 添加数据行
    public void addRow(CashStatisticsRow row) {
        this.rows.add(row);
    }

    // 添加合并配置
    public void addMergeConfig(CellMergeConfig config) {
        this.mergeConfigs.add(config);
    }

    // getter 和 setter
    public List<String> getHeaders() { return headers; }
    public void setHeaders(List<String> headers) { this.headers = headers; }

    public List<CashStatisticsRow> getRows() { return rows; }
    public void setRows(List<CashStatisticsRow> rows) { this.rows = rows; }

    public List<CellMergeConfig> getMergeConfigs() { return mergeConfigs; }
    public void setMergeConfigs(List<CellMergeConfig> mergeConfigs) { this.mergeConfigs = mergeConfigs; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
}
