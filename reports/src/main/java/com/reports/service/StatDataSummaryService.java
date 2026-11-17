package com.reports.service;

import com.reports.dto.request.StatDataSummaryRequest;
import com.reports.dto.response.stat.StatDataSummaryResponse;

/**
 * 运营数据统计服务
 */
public interface StatDataSummaryService {

    /** 按页签查询统计数据 */
    StatDataSummaryResponse query(StatDataSummaryRequest request, int page, int pageSize);

}
