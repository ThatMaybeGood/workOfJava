package com.reports.service;

import com.reports.dto.common.PageResult;
import com.reports.dto.request.OutpatientSpecialtyTreatmentRequest;
import com.reports.dto.response.outpatient.specialty.treatment.OverviewData;
import com.reports.dto.response.outpatient.specialty.treatment.TableItem;

/**
 * 专科治疗量统计服务
 */
public interface OutpatientSpecialtyTreatmentService {

    /**
     * 查询概览数据
     */
    OverviewData queryOverview(OutpatientSpecialtyTreatmentRequest request);

    /**
     * 查询表格数据（分页）
     */
    PageResult<TableItem> queryTable(OutpatientSpecialtyTreatmentRequest request, Integer page, Integer pageSize);

}
