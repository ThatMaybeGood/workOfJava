package com.reports.dto.source;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 源库答卷原始行：SFP_QUESTION_ANSWER 左连 POWERSFP_ADVICE_INFO（意见箱）
 */
@Data
public class QuestionAnswerRow implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 答卷记录ID */
    private String recordId;

    /** 答卷创建时间 */
    private Date createTime;

    /** 答卷JSON，[{topic_id, answer}, ...] */
    private String answerInfo;

    /** 问卷ID */
    private String questionId;

    /** 意见箱-处理结果 */
    private String disposeResult;

    /** 意见箱-科室名称 */
    private String divisionName;

    /** 意见箱-处理状态(0未回复 1已回复 2无需回复) */
    private String adviceStatus;
}
