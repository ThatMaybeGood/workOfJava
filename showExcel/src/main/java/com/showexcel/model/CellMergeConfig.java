package com.showexcel.model;

/**
 * @author Mine
 * @version 1.0
 * 描述:
 * @date 2025/10/30 16:32
 */
// 合并单元格配置类
public class CellMergeConfig {
    private int startRow;    // 起始行（从0开始）
    private int startCol;    // 起始列（从0开始）
    private int rowSpan;     // 合并行数
    private int colSpan;     // 合并列数
    private String content;  // 合并单元格内容

    public CellMergeConfig(int startRow, int startCol, int rowSpan, int colSpan) {
        this.startRow = startRow;
        this.startCol = startCol;
        this.rowSpan = rowSpan;
        this.colSpan = colSpan;
    }

    public CellMergeConfig(int startRow, int startCol, int rowSpan, int colSpan, String content) {
        this(startRow, startCol, rowSpan, colSpan);
        this.content = content;
    }

    // getter 和 setter
    public int getStartRow() { return startRow; }
    public void setStartRow(int startRow) { this.startRow = startRow; }

    public int getStartCol() { return startCol; }
    public void setStartCol(int startCol) { this.startCol = startCol; }

    public int getRowSpan() { return rowSpan; }
    public void setRowSpan(int rowSpan) { this.rowSpan = rowSpan; }

    public int getColSpan() { return colSpan; }
    public void setColSpan(int colSpan) { this.colSpan = colSpan; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}