package com.reports.service;

import com.reports.dto.common.PageResult;
import com.reports.dto.request.OutpatientMedTechRequest;
import com.reports.dto.response.outpatient.med.tech.OverviewData;
import com.reports.dto.response.outpatient.med.tech.TableItem;

/**
 * 医技统计服务
 */
public interface OutpatientMedTechService {

    /**
     * 查询概览数据
     */
    OverviewData queryOverview(OutpatientMedTechRequest request);

    /**
     * 查询表格数据（分页）
     */
    PageResult<TableItem> queryTable(OutpatientMedTechRequest request, Integer page, Integer pageSize);

}
