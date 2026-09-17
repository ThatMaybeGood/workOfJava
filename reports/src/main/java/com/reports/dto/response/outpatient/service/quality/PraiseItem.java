package com.reports.dto.response.outpatient.service.quality;

import lombok.Data;

/**
 * 门诊服务质量分析 - 表扬明细
 */
@Data
public class PraiseItem {

    /** 来源：院领导信箱(源库) / 人工登记 */
    private String source;
    private String time;
    private String dept;
    private String person;
    private String position;
    private String method;
    /** 是否反馈科室（人工维护，走字典 feedback） */
    private String feedback;
    /** 反馈内容（来自源库答卷原文） */
    private String content;
    private String remark;

}
