package com.reports.service;

import com.reports.dto.request.OutpatientWindowStatsRequest;
import com.reports.dto.response.outpatient.window.stats.*;

import java.util.List;

/**
 * 人工窗口统计服务
 */
public interface OutpatientWindowStatsService {

    /**
     * 查询概览数据
     */
    OverviewData queryOverview(OutpatientWindowStatsRequest request);

    /**
     * 查询年龄分析
     */
    AgeAnalysis queryAgeAnalysis(OutpatientWindowStatsRequest request);

    /**
     * 查询时段分析
     */
    TimeAnalysis queryTimeAnalysis(OutpatientWindowStatsRequest request);

    /**
     * 查询来源分析
     */
    List<AnalysisItem> querySourceAnalysis(OutpatientWindowStatsRequest request);

    /**
     * 查询工作量表格
     */
    WorkloadTable queryWorkloadTable(OutpatientWindowStatsRequest request);

}
