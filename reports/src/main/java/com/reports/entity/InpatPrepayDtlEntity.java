package com.reports.entity;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 住院预交金统计-按天聚合(本期/去年同期)
 */
@Data
public class InpatPrepayDtlEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 日期 */
    private Date itemDate;

    /** 本期笔数 */
    private Integer countCurrent;

    /** 去年同期笔数 */
    private Integer countLast;

    /** 本期金额 */
    private BigDecimal amountCurrent;

    /** 去年同期金额 */
    private BigDecimal amountLast;

}
