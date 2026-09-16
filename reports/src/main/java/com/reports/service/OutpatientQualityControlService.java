package com.reports.service;

import com.reports.dto.common.PageResult;
import com.reports.dto.request.OutpatientQualityControlRequest;
import com.reports.dto.request.QualityControlMaintainRequest;
import com.reports.dto.response.outpatient.quality.control.OverviewData;
import com.reports.dto.response.outpatient.quality.control.QcMaintainItem;
import com.reports.dto.response.outpatient.quality.control.TableItem;

import java.util.List;

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

    /**
     * 查询维护明细（按月份）
     */
    List<QcMaintainItem> queryMaintainList(QualityControlMaintainRequest request);

    /**
     * 保存维护明细（按 月份+指标编码 覆盖，source_type=人工登记）
     */
    int saveMaintain(QualityControlMaintainRequest request);

}
