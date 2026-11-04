package com.reports.dto.response.cash.mi;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 医疗机构对账某日按来源类型汇总(明细弹窗用)
 */
@Data
public class MiReconOrgCatItem {

    /** 来源类型: 账平/医保负金额/医保正金额(或1/2/3代码) */
    private String sourceType;

    /** 来源类型中文名 */
    private String sourceTypeName;

    /** 数量 */
    private Long qty;

    /** 金额 */
    private BigDecimal fee;
}
