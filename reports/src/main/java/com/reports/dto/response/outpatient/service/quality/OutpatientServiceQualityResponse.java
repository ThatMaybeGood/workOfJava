package com.reports.dto.response.outpatient.service.quality;

import com.reports.dto.common.PageResult;
import lombok.Data;

/**
 * 门诊服务质量分析 - 响应体
 */
@Data
public class OutpatientServiceQualityResponse {

    private static final long serialVersionUID = 1L;

    private OverviewData overview;
    private PageResult<ComplaintItem> complaint;
    private PageResult<PraiseItem> praise;

}
