package com.reports.service;

import com.reports.dto.common.PageResult;
import com.reports.dto.request.OutpatientServiceQualityRequest;
import com.reports.dto.response.outpatient.service.quality.ComplaintItem;
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

}
