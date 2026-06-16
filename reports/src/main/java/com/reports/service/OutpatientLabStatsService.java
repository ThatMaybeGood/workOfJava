package com.reports.service;

import com.reports.dto.request.OutpatientLabStatsRequest;
import com.reports.dto.response.outpatient.lab.stats.OverviewData;

/**
 * 检验统计服务
 */
public interface OutpatientLabStatsService {

    /**
     * 查询概览数据
     */
    OverviewData queryOverview(OutpatientLabStatsRequest request);

}
