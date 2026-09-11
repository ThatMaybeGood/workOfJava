package com.reports.service;

import com.reports.dto.common.PageResult;
import com.reports.dto.request.OutpatientServiceQualityRequest;
import com.reports.dto.request.ServiceQualityMaintainRequest;
import com.reports.dto.response.outpatient.service.quality.ComplaintItem;
import com.reports.dto.response.outpatient.service.quality.MaintainItem;
import com.reports.dto.response.outpatient.service.quality.OverviewData;
import com.reports.dto.response.outpatient.service.quality.PraiseItem;

/**
 * 门诊服务质量分析服务
 */
public interface OutpatientServiceQualityService {

    /**
     * 查询概览数据
     */
    OverviewData queryOverview(OutpatientServiceQualityRequest request);

    /**
     * 查询投诉明细（分页）
     */
    PageResult<ComplaintItem> queryComplaintList(OutpatientServiceQualityRequest request, Integer page, Integer pageSize);

    /**
     * 查询表扬明细（分页）
     */
    PageResult<PraiseItem> queryPraiseList(OutpatientServiceQualityRequest request, Integer page, Integer pageSize);

    /**
     * 数据维护：查询明细列表（分页）
     */
    PageResult<MaintainItem> queryMaintainList(ServiceQualityMaintainRequest request, Integer page, Integer pageSize);

    /**
     * 数据维护：批量保存（id 为空新增，否则更新）
     */
    int saveMaintain(ServiceQualityMaintainRequest request);

    /**
     * 数据维护：删除明细
     */
    int deleteMaintain(ServiceQualityMaintainRequest request);

}
