package com.reports.service;

import com.reports.dto.response.stat.StatDataSummaryResponse;

/**
 * 运营数据统计-HIS出诊单元服务
 */
public interface StatDataSummaryHisService {

    /** bt6 分析tab： 医生出诊单元 */
    StatDataSummaryResponse queryClinicUnit(String startDate, String endDate);

}
