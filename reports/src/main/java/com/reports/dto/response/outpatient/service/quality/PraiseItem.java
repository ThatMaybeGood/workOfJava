package com.reports.dto.response.outpatient.service.quality;

import lombok.Data;

/**
 * 门诊服务质量分析 - 表扬明细
 */
@Data
public class PraiseItem {

    private String time;
    private String dept;
    private String person;
    private String position;
    private String method;
    private String feedback;
    private String remark;

}
