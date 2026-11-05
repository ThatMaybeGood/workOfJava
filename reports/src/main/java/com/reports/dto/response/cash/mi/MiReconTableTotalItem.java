package com.reports.dto.response.cash.mi;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 某日两表全量合计(账平差异弹窗顶部对照, 不限zp_flag)
 */
@Data
public class MiReconTableTotalItem {

    /** 表名: HIS业务表/医保支付表(SQL直出中文) */
    private String tableName;

    /** 总条数 */
    private Long qty;

    /** 总金额 */
    private BigDecimal fee;
}
