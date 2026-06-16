package com.reports.dto.response.outpatient.service.quality;

import lombok.Data;

/**
 * 门诊服务质量分析 - 投诉明细
 */
@Data
public class ComplaintItem {

    private String time;
    private String dept;
    private String person;
    private String position;
    private String category;
    private String result;
    private String remark;

}
