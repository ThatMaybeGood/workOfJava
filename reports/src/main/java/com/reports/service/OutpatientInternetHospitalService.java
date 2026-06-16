package com.reports.service;

import com.reports.dto.common.PageResult;
import com.reports.dto.request.OutpatientInternetHospitalRequest;
import com.reports.dto.response.outpatient.internet.hospital.*;

/**
 * 互医质控运营月报服务
 */
public interface OutpatientInternetHospitalService {

    /**
     * 查询概览数据
     */
    OverviewData queryOverview(OutpatientInternetHospitalRequest request);

    /**
     * 查询运行情况表（分页）
     */
    PageResult<OperationTableItem> queryOperationTable(OutpatientInternetHospitalRequest request, Integer page, Integer pageSize);

    /**
     * 查询业务分析图表
     */
    BusinessChart queryBusinessChart(OutpatientInternetHospitalRequest request);

    /**
     * 查询科室排行（分页）
     */
    PageResult<DeptRankingItem> queryDeptRanking(OutpatientInternetHospitalRequest request, Integer page, Integer pageSize);

    /**
     * 查询医生排行（分页）
     */
    PageResult<DoctorRankingItem> queryDoctorRanking(OutpatientInternetHospitalRequest request, Integer page, Integer pageSize);

    /**
     * 查询增长趋势图表
     */
    GrowthChart queryGrowthChart(OutpatientInternetHospitalRequest request);

}
