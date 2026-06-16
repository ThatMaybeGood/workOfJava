package com.reports.dto.response.outpatient.room.usage;

import lombok.Data;

/**
 * 诊室使用率分析 - 表格行数据
 */
@Data
public class TableItem {

    private String deptName;
    private String avgUsage;
    private String amUsage;
    private String pmUsage;
    private String holidayUsage;

}
