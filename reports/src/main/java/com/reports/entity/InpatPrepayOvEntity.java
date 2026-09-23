package com.reports.entity;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 住院预交金统计-概览聚合(本期/去年同期)
 */
@Data
public class InpatPrepayOvEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 本期笔数 */
    private Integer countCurrent;

    /** 去年同期笔数 */
    private Integer countLast;

    /** 本期金额 */
    private BigDecimal amountCurrent;

    /** 去年同期金额 */
    private BigDecimal amountLast;

}
