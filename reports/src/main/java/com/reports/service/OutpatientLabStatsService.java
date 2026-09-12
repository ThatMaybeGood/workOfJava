package com.reports.service;

import com.reports.dto.request.OutpatientLabStatsRequest;
import com.reports.dto.response.outpatient.lab.stats.OverviewData;
import com.reports.dto.response.outpatient.lab.stats.ReportRank;
import com.reports.dto.response.outpatient.lab.stats.TimeAnalysis;

/**
 * 检验统计服务
 */
public interface OutpatientLabStatsService {

    /**
     * 查询概览数据
     */
    OverviewData queryOverview(OutpatientLabStatsRequest request);

    /**
     * 查询分时段采血分析
     */
    TimeAnalysis queryTimeAnalysis(OutpatientLabStatsRequest request);

    /**
     * 查询检验项目排行
     */
    ReportRank queryReportRank(OutpatientLabStatsRequest request);

}
