package com.reports.dto.response.outpatient.alert;

import com.reports.dto.common.PageResult;
import lombok.Data;

import java.util.List;

/**
 * 门诊预警统计 - 响应体
 */
@Data
public class OutpatientAlertResponse {

    private static final long serialVersionUID = 1L;

    /**
     * 概览数据
     */
    private OverviewData overview;

    /**
     * 科室预警统计表格
     */
    private PageResult<DeptTableItem> deptTable;

    /**
     * 医生预警统计表格
     */
    private PageResult<DoctorTableItem> doctorTable;

    /**
     * 早退明细（detail=true 时返回）
     */
    private List<DetailItem> detailList;

}
