package com.reports.service;

import com.reports.dto.common.PageResult;
import com.reports.dto.request.OutpatientAlertRequest;
import com.reports.dto.response.outpatient.alert.DeptTableItem;
import com.reports.dto.response.outpatient.alert.DetailItem;
import com.reports.dto.response.outpatient.alert.DoctorTableItem;
import com.reports.dto.response.outpatient.alert.OverviewData;

import java.util.List;

/**
 * 门诊预警统计服务
 */
public interface OutpatientAlertService {

    /**
     * 查询概览数据
     */
    OverviewData queryOverview(OutpatientAlertRequest request);

    /**
     * 查询科室预警表格（分页）
     */
    PageResult<DeptTableItem> queryDeptTable(OutpatientAlertRequest request, Integer page, Integer pageSize);

    /**
     * 查询医生预警表格（分页）
     */
    PageResult<DoctorTableItem> queryDoctorTable(OutpatientAlertRequest request, Integer page, Integer pageSize);

    /**
     * 查询早退明细（无分页，用于弹窗）
     */
    List<DetailItem> queryEarlyLeaveDetail(OutpatientAlertRequest request);

}
