package com.reports.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 治疗统计报表请求
 */
@Data
public class TreatmentStatsRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 开始日期，格式 yyyy-MM-dd（必须带 @JsonFormat，否则会被当成 UTC 解析出 8 小时偏移） */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date startDate;

    /** 结束日期，格式 yyyy-MM-dd */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date endDate;

    /** 就诊类型（当前仅作为筛选条件保留，库里没有对应维度） */
    private String visitType;

    /** 患者来源（同上） */
    private String patientSource;

    /** 年龄段（同上） */
    private String ageRange;

    /** 页码 */
    private Integer page;

    /** 每页条数 */
    private Integer pageSize;
}
