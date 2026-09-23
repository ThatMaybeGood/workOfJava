package com.reports.entity;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 住院预交金统计-渠道×支付方式聚合(本期/去年同期)
 */
@Data
public class InpatPrepayChtEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 渠道(自助机/窗口,按操作员区分:9111为自助机) */
    private String channel;

    /** 支付方式 */
    private String payWay;

    /** 本期笔数 */
    private Integer countCurrent;

    /** 去年同期笔数 */
    private Integer countLast;

    /** 本期金额 */
    private BigDecimal amountCurrent;

    /** 去年同期金额 */
    private BigDecimal amountLast;

}
