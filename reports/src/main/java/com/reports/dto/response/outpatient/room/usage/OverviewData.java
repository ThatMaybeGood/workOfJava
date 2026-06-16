package com.reports.dto.response.outpatient.room.usage;

import lombok.Data;

/**
 * 诊室使用率分析 - 概览数据
 */
@Data
public class OverviewData {

    private String avgUsage;
    private String amUsage;
    private String pmUsage;
    private String holidayUsage;

}
