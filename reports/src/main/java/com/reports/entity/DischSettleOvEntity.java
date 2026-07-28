package com.reports.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 出院结算报表-概览
 */
@Data
@TableName("TR_DISCH_SETTLE_OV")
public class DischSettleOvEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @TableId("id")
    private Long id;

    /** 统计日期 */
    @TableField("stat_date")
    private Date statDate;

    /** 总出院人次 */
    @TableField("total_discharge_count")
    private Integer totalDischargeCount;

    /** 总出院对比 */
    @TableField("total_discharge_compare")
    private Integer totalDischargeCompare;

    /** 已出院人次 */
    @TableField("discharged_count")
    private Integer dischargedCount;

    /** 已出院对比 */
    @TableField("discharged_compare")
    private Integer dischargedCompare;

    /** 未出院人次 */
    @TableField("not_discharged_count")
    private Integer notDischargedCount;

    /** 未出院对比 */
    @TableField("not_discharged_compare")
    private Integer notDischargedCompare;

    /** 结算金额 */
    @TableField("settlement_amount")
    private BigDecimal settlementAmount;

    /** 结算金额对比 */
    @TableField("settlement_amount_compare")
    private Integer settlementAmountCompare;

    /** 创建时间 */
    @TableField("create_time")
    private Date createTime;

    /** 更新时间 */
    @TableField("update_time")
    private Date updateTime;

    /** 扩展字段1 */
    @TableField("ext1")
    private String ext1;

    /** 扩展字段2 */
    @TableField("ext2")
    private String ext2;

    /** 扩展字段3 */
    @TableField("ext3")
    private String ext3;
}
