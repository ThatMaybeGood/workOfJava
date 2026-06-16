package com.reports.dto.response.outpatient.revenue;

import com.reports.dto.common.PageResult;
import lombok.Data;

/**
 * 门诊收入分析 - 响应体
 */
@Data
public class OutpatientRevenueResponse {

    private static final long serialVersionUID = 1L;

    private OverviewData overview;
    private PageResult<DeptTableItem> deptTable;
    private PageResult<DoctorTableItem> doctorTable;

}
