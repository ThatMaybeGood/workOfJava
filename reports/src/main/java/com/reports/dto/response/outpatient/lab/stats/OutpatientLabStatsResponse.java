package com.reports.dto.response.outpatient.lab.stats;

import lombok.Data;
import java.util.List;

/**
 * 检验统计 - 响应体
 */
@Data
public class OutpatientLabStatsResponse {

    private static final long serialVersionUID = 1L;

    private OverviewData overview;
    private TimeAnalysis timeAnalysis;
    private ReportRank reportRank;

}
