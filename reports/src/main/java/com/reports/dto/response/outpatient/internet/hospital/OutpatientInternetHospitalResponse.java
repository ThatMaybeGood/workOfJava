package com.reports.dto.response.outpatient.internet.hospital;

import com.reports.dto.common.PageResult;
import lombok.Data;

/**
 * 互医质控运营月报 - 响应体
 */
@Data
public class OutpatientInternetHospitalResponse {

    private static final long serialVersionUID = 1L;

    private OverviewData overview;
    private PageResult<OperationTableItem> operationTable;
    private BusinessChart businessChart;
    private PageResult<DeptRankingItem> deptRanking;
    private PageResult<DoctorRankingItem> doctorRanking;
    private GrowthChart growthChart;

}
