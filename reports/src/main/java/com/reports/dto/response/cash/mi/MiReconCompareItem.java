package com.reports.dto.response.cash.mi;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 医保对账每日对比行
 */
@Data
public class MiReconCompareItem {

    /** 结算日期 */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date settleDate;

    /** 医保类型 */
    private String insurType;

    /** 医保-对账数据量(无数据为null) */
    private Long miQty;

    /** 医保-医疗费总额(无数据为null) */
    private BigDecimal miFee;

    /** 医疗机构-数量(无数据为null) */
    private Long orgQty;

    /** 医疗机构-医疗总费用(无数据为null) */
    private BigDecimal orgFee;

    /** 差额 = 医保 - 医疗机构 */
    private BigDecimal diffFee;

    /** 是否对平(双侧金额相等) */
    private Boolean matched;

    /** 医疗机构侧按来源类型汇总明细(账平/医保负金额/医保正金额), 无机构数据为null */
    private java.util.List<MiReconOrgCatItem> orgDtl;
}
