package com.reports.dto.response.outpatient.specialty.treatment;

import com.reports.dto.common.PageResult;
import lombok.Data;

/**
 * 专科治疗量统计 - 响应体
 */
@Data
public class OutpatientSpecialtyTreatmentResponse {

    private static final long serialVersionUID = 1L;

    private OverviewData overview;
    private PageResult<TableItem> table;

}
