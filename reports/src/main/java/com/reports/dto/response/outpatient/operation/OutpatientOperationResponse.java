package com.reports.dto.response.outpatient.operation;

import com.reports.dto.common.PageResult;
import lombok.Data;

/**
 * 门诊运行数据统计 - 响应体
 */
@Data
public class OutpatientOperationResponse {

    private static final long serialVersionUID = 1L;

    /**
     * 概览数据
     */
    private OverviewData overview;

    /**
     * 表格分页数据
     */
    private PageResult<TableItem> table;

}
