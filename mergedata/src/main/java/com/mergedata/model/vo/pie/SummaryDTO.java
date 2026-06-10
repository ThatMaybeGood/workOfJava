package com.mergedata.model.vo.pie;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 汇总统计实体
 */
@Data
public class SummaryDTO {
    private CoreMetricDTO totalRepay;      // 回款
    private CoreMetricDTO totalLoan;      // 借款
    private CoreMetricDTO totalNet;         // 总合计汇总
    private BigDecimal totalAuxiliary;      // 辅助项总金额
}