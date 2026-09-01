package com.reports.entity.cash;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("TR_OUTP_FIN_ACCT_MASTER")
public class OutpFinanceAcctMaster implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableField("acct_date")
    private Date acctDate;

    @TableField("total_costs")
    private BigDecimal totalCosts;

    @TableField("refund_amount")
    private BigDecimal refundAmount;

    @TableField("rcpts_num")
    private BigDecimal rcptsNum;

    @TableField("refund_num")
    private BigDecimal refundNum;

    @TableField("create_time")
    private Date createTime;

    @TableField("update_time")
    private Date updateTime;
}
