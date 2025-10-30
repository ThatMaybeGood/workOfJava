package com.showexcel.dao;

import com.showexcel.model.CashStatistics;

/**
 * @author Mine
 * @version 1.0
 * 描述:
 * @date 2025/10/30 16:31
 */
// 数据行类
public class CashStatisticsRow {
    private Integer rowType; // 1: row1, 2: row2
    private CashStatistics data;
    private Integer rowIndex; // 在表格中的实际行索引

    public CashStatisticsRow(Integer rowType, CashStatistics data) {
        this.rowType = rowType;
        this.data = data;
    }

    // getter 和 setter
    public Integer getRowType() { return rowType; }
    public void setRowType(Integer rowType) { this.rowType = rowType; }

    public CashStatistics getData() { return data; }
    public void setData(CashStatistics data) { this.data = data; }

    public Integer getRowIndex() { return rowIndex; }
    public void setRowIndex(Integer rowIndex) { this.rowIndex = rowIndex; }
}