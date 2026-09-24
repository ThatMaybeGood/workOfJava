package com.mergedata.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.mergedata.constants.Constant;
import lombok.Data;
import nonapi.io.github.classgraph.json.Id;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 住院现金统计主表实体类
 */
@Data
@TableName("mpp_cash_inp_master")
public class InpCashMainEntity {
    /**
     * 主键：流水号
     */
    @TableId(value = "serial_no")
    private String serialNo;
    /**
     * 报表日期
     */
    @TableField(value = "report_date")
    private LocalDate reportDate;
    /**
     * 报表年份
     */
    @TableField(value = "report_year")
    private Integer reportYear;
    /**
     * 报表状态  0：未生效 1：已生效
     */
    @TableField(value = "valid_flag")
    private String validFlag = Constant.NO;
    /**
     * 创建人
     */
    @TableField(value = "creator")
    private String creator;
    /**
     * 创建时间
     */
    @TableField(value = "create_time")
    private LocalDateTime createTime;
    /**
     * 更新时间
     */
    @TableField(value = "update_time")
    private LocalDateTime updateTime;
    /**
     * 节假日汇总标志  0：否 1：是
     */
    @TableField(value = "holiday_total_flag")
    private String holidayTotalFlag = Constant.NO;
    /**
     * 报表级备注（合计行下方整表一条）
     */
    @TableField(value = "total_remark")
    private String totalRemark;
    /**
     * 审核状态 0：未审核 1：审核通过 2：审核不通过
     */
    @TableField(value = "audit_status")
    private String auditStatus = Constant.NO;
    /**
     * 审核人
     */
    @TableField(value = "audit_by")
    private String auditBy;
    /**
     * 审核时间
     */
    @TableField(value = "audit_time")
    private LocalDateTime auditTime;
    /**
     * 审核意见
     */
    @TableField(value = "audit_remark")
    private String auditRemark;
    /**
     * 子报表列表（一对多关系）
     */
    @TableField(exist = false)
    private List<InpCashSubEntity> subs;

    /**
     * 审核日志列表（查询时附带，不落本表）
     */
    @TableField(exist = false)
    private List<InpAuditLogEntity> auditLogs;

}
