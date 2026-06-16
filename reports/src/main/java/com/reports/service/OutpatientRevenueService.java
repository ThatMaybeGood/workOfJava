package com.reports.service;

import com.reports.dto.common.PageResult;
import com.reports.dto.request.OutpatientRevenueRequest;
import com.reports.dto.response.outpatient.revenue.DeptTableItem;
import com.reports.dto.response.outpatient.revenue.DoctorTableItem;
import com.reports.dto.response.outpatient.revenue.OverviewData;

/**
 * 门诊收入分析服务
 */
public interface OutpatientRevenueService {

    /**
     * 查询概览数据
     */
    OverviewData queryOverview(OutpatientRevenueRequest request);

    /**
     * 查询科室收入表格（分页）
     */
    PageResult<DeptTableItem> queryDeptTable(OutpatientRevenueRequest request, Integer page, Integer pageSize);

    /**
     * 查询医生收入表格（分页）
     */
    PageResult<DoctorTableItem> queryDoctorTable(OutpatientRevenueRequest request, Integer page, Integer pageSize);

}
