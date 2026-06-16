package com.reports.service;

import com.reports.dto.common.PageResult;
import com.reports.dto.request.OutpatientQualityControlRequest;
import com.reports.dto.response.outpatient.quality.control.OverviewData;
import com.reports.dto.response.outpatient.quality.control.TableItem;

/**
 * 门诊管理质量控制服务
 */
public interface OutpatientQualityControlService {

    /**
     * 查询概览数据
     */
    OverviewData queryOverview(OutpatientQualityControlRequest request);

    /**
     * 查询表格数据（分页）
     */
    PageResult<TableItem> queryTable(OutpatientQualityControlRequest request, Integer page, Integer pageSize);

}
