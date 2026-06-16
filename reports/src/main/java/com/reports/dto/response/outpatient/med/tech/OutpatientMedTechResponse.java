package com.reports.dto.response.outpatient.med.tech;

import com.reports.dto.common.PageResult;
import lombok.Data;

/**
 * 医技统计 - 响应体
 */
@Data
public class OutpatientMedTechResponse {

    private static final long serialVersionUID = 1L;

    private OverviewData overview;
    private PageResult<TableItem> table;

}
