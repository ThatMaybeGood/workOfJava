package com.reports.service;

import com.reports.dto.request.TreatmentStatsRequest;

import java.util.Map;

/**
 * 治疗统计报表服务
 */
public interface TreatmentStatsService {

    /**
     * 一次性返回页面上四块数据：
     * overview（概览指标）、trend（治疗量趋势）、topProjects（TOP 治疗项目）、table（科室明细）。
     *
     * <p>页面只调一个接口、读的是这个嵌套结构（见 treatment-stats-app.js 的 loadData），
     * 所以不需要按子接口分发。
     */
    Map<String, Object> queryStats(TreatmentStatsRequest request, Integer page, Integer pageSize);
}
