package com.reports.dto.source;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 源库答卷解析后的投诉/表扬记录（源侧能给的字段）
 */
@Data
public class SourceFeedback implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 源答卷ID，人工补充字段靠它挂回来 */
    private String sourceId;

    /** 投诉/表扬时间 */
    private Date time;

    /** 分类：complaint 投诉 / praise 表扬 */
    private String kind;

    /** 科室（投诉分支是用户手填的自由文本；表扬分支源里没有） */
    private String deptName;

    /** 人员（同上，表扬分支源里没有） */
    private String personName;

    /** 正文：投诉内容 / 表扬反馈内容 */
    private String content;

    /** 诉求（只有投诉分支有） */
    private String appeal;

    /** 意见箱的处理结果 */
    private String result;
}
