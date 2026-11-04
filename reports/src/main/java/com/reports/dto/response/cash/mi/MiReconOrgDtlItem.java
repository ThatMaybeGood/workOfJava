package com.reports.dto.response.cash.mi;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 医疗机构对账原始行(HIS侧, 聚合前, 6字段)
 */
@Data
public class MiReconOrgDtlItem {

    /** 日期(结算日期) */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date settleDate;

    /** 数量 */
    private Long qty;

    /** 医疗总金额 */
    private BigDecimal fee;

    /** 类型代码: 310/390/3101/3901 */
    private String insurType;

    /** 类型名称: 职工/居民/异地职工/异地居民 */
    private String insurTypeName;

    /** 来源类型: 账平/医保负金额(his正交易)/医保正金额(SQL直出中文, 兼容1/2/3代码) */
    private String sourceType;
}
