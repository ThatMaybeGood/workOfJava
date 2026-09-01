package com.reports.entity.cash;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("TR_OUTP_FIN_RCPT_ACCT")
public class OutpFinanceRcptAcct implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableField("rcpt_no")
    private String rcptNo;

    @TableField("patient_id")
    private String patientId;

    @TableField("visit_date")
    private Date visitDate;

    @TableField("total_charges")
    private BigDecimal totalCharges;

    @TableField("refunded_rcpt_no")
    private String refundedRcptNo;

    @TableField("operator_no")
    private String operatorNo;

    @TableField("bill_class")
    private String billClass;

    @TableField("create_time")
    private Date createTime;

    @TableField("update_time")
    private Date updateTime;
}
