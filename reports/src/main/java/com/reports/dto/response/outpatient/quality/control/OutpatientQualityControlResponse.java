package com.reports.dto.response.outpatient.quality.control;

import com.reports.dto.common.PageResult;
import lombok.Data;

/**
 * 门诊管理质量控制 - 响应体
 */
@Data
public class OutpatientQualityControlResponse {

    private static final long serialVersionUID = 1L;

    private OverviewData overview;
    private PageResult<TableItem> table;

}
