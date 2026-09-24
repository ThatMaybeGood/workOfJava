package com.mergedata.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 住院现金统计审核日志
 */
@Data
@TableName("mpp_cash_inp_audit_log")
public class InpAuditLogEntity {

    @TableId(value = "serial_no")
    private String serialNo;

    @TableField(value = "report_date")
    private LocalDate reportDate;

    @TableField(value = "holiday_total_flag")
    private String holidayTotalFlag;

    /** 审核动作 1：通过 2：不通过 0：取消审核 */
    @TableField(value = "audit_status")
    private String auditStatus;

    @TableField(value = "audit_by")
    private String auditBy;

    @TableField(value = "audit_time")
    private LocalDateTime auditTime;

    @TableField(value = "audit_remark")
    private String auditRemark;

}
