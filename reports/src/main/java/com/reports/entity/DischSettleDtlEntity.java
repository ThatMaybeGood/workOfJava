package com.reports.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 出院结算报表-日明细
 */
@Data
@TableName("TR_DISCH_SETTLE_DTL")
public class DischSettleDtlEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @TableId("id")
    private Long id;

    /** 统计日期 */
    @TableField("stat_date")
    private Date statDate;

    /** 日期 */
    @TableField("item_date")
    private Date itemDate;

    /** 总出院上期 */
    @TableField("total_last")
    private Integer totalLast;

    /** 总出院本期 */
    @TableField("total_current")
    private Integer totalCurrent;

    /** 总出院对比 */
    @TableField("total_compare")
    private Integer totalCompare;

    /** 已出院上期 */
    @TableField("discharged_last")
    private Integer dischargedLast;

    /** 已出院本期 */
    @TableField("discharged_current")
    private Integer dischargedCurrent;

    /** 已出院对比 */
    @TableField("discharged_compare")
    private Integer dischargedCompare;

    /** 未出院上期 */
    @TableField("not_discharged_last")
    private Integer notDischargedLast;

    /** 未出院本期 */
    @TableField("not_discharged_current")
    private Integer notDischargedCurrent;

    /** 未出院对比 */
    @TableField("not_discharged_compare")
    private Integer notDischargedCompare;

    /** 金额上期 */
    @TableField("amount_last")
    private BigDecimal amountLast;

    /** 金额本期 */
    @TableField("amount_current")
    private BigDecimal amountCurrent;

    /** 金额对比 */
    @TableField("amount_compare")
    private Integer amountCompare;

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
