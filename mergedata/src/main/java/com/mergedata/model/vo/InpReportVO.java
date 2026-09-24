package com.mergedata.model.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.mergedata.constants.Constant;
import com.mergedata.model.entity.InpAuditLogEntity;
import com.mergedata.model.entity.InpCashSubEntity;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 住院现金统计主表VO
 */
@Data
public class InpReportVO {

    @TableId(value = "serial_no")
    private String serialNo;              // 流水号

    @TableField(value = "report_date")
    private LocalDate reportDate;         // 报表日期
    @TableField(value = "report_year")
    private Integer reportYear;           // 报表年份

    @TableField(value = "report_type")
    private String reportType = Constant.TYPE_INP;            // 报表类型 0：门诊 1：住院

    @TableField(value = "total_flag")
    private String totalFlag = Constant.NO; //汇总标志

    private String totalRemark;           // 报表级备注（合计行下方）

    private String auditStatus;           // 审核状态 0-未审核 1-通过 2-不通过
    private String auditBy;               // 审核人
    private LocalDateTime auditTime;      // 审核时间
    private String auditRemark;           // 审核意见

    private List<InpAuditLogEntity> auditLogs;  // 审核日志记录

    // 关联的子报表列表（一对多关系）
    @TableField(exist = false)
    private List<InpCashSubEntity> subs;

}
