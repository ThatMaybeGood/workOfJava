package com.reports.service;

import com.reports.dto.common.PageResult;
import com.reports.dto.request.OutpatientOperationRequest;
import com.reports.dto.response.OverviewData;
import com.reports.dto.response.TableItem;

/**
 * 门诊运行数据统计服务
 */
public interface OutpatientOperationService {

    /**
     * 查询概览数据
     */
    OverviewData queryOverview(OutpatientOperationRequest request);

    /**
     * 查询表格数据（分页）
     */
    PageResult<TableItem> queryTable(OutpatientOperationRequest request, Integer page, Integer pageSize);

}
