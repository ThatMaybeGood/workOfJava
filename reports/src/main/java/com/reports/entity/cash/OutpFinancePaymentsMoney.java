package com.reports.entity.cash;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("TR_OUTP_FIN_PAYMENTS_MONEY")
public class OutpFinancePaymentsMoney implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableField("rcpt_no")
    private String rcptNo;

    @TableField("money_type")
    private String moneyType;

    @TableField("payment_amount")
    private BigDecimal paymentAmount;

    @TableField("refunded_amount")
    private BigDecimal refundedAmount;

    @TableField("create_time")
    private Date createTime;

    @TableField("update_time")
    private Date updateTime;
}
