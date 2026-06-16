package com.reports.dto.response.outpatient.service.quality;

import lombok.Data;

/**
 * 门诊服务质量分析 - 概览数据
 */
@Data
public class OverviewData {

    private Integer complaintCount;
    private Integer praiseCount;

}
