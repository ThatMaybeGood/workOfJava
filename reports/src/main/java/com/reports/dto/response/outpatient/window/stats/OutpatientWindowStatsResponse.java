package com.reports.dto.response.outpatient.window.stats;

import lombok.Data;
import java.util.List;

/**
 * 人工窗口统计 - 响应体
 */
@Data
public class OutpatientWindowStatsResponse {

    private static final long serialVersionUID = 1L;

    private OverviewData overview;
    private List<AnalysisItem> originAnalysis;
    private AgeAnalysis ageAnalysis;
    private TimeAnalysis timeAnalysis;
    private WorkloadTable workloadTable;

}
