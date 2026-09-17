package com.reports.dto.response.outpatient.service.quality;

import lombok.Data;

/**
 * 门诊服务质量分析 - 投诉明细
 */
@Data
public class ComplaintItem {

    /** 来源：院领导信箱(源库) / 人工登记 */
    private String source;
    private String time;
    private String dept;
    private String person;
    private String position;
    private String category;
    private String result;
    /** 您投诉的内容（来自源库答卷原文） */
    private String content;
    /** 诉求（来自源库答卷原文） */
    private String appeal;
    private String remark;

}
