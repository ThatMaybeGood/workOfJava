package com.reports.dto.response.cash.discharge.settlement;

import lombok.Data;

import java.io.Serializable;

/**
 * 出院结算人次统计明细行
 */
@Data
public class PersonCountItem implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 统计日期（按天 yyyy-MM-dd / 按月 yyyy-MM）
     */
    private String itemDate;

    /**
     * 费别（军队医改/普通患者）
     */
    private String feeType;

    /**
     * 结算类别（自助结算/窗口结算）
     */
    private String settleChannel;

    /**
     * 操作员工号（按操作员维度）
     */
    private String operatorNo;

    /**
     * 操作员姓名（关联 users 表）
     */
    private String operatorName;

    /**
     * 支付类别（按支付类别维度）
     */
    private String payType;

    /**
     * 人次
     */
    private Integer cnt;
}
