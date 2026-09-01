package com.reports.entity.cash;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("TR_OUTP_FIN_CLINIC_MASTER")
public class OutpFinanceClinicMaster implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableField("patient_id")
    private String patientId;

    @TableField("visit_date")
    private Date visitDate;

    @TableField("returned_date")
    private Date returnedDate;

    @TableField("regist_fee")
    private BigDecimal registFee;

    @TableField("clinic_fee")
    private BigDecimal clinicFee;

    @TableField("clinic_label")
    private String clinicLabel;

    @TableField("visit_time_desc")
    private String visitTimeDesc;

    @TableField("operator_no")
    private String operatorNo;

    @TableField("create_time")
    private Date createTime;

    @TableField("update_time")
    private Date updateTime;
}
