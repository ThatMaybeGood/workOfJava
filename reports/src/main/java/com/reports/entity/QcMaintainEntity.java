package com.reports.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 门诊质控指标人工维护表 TR_QC_MAINTAIN
 */
@Data
@TableName("TR_QC_MAINTAIN")
public class QcMaintainEntity {

    @TableId(value = "ID", type = IdType.INPUT)
    private Long id;

    @TableField("STAT_MONTH")
    private String statMonth;

    @TableField("INDICATOR_CODE")
    private String indicatorCode;

    @TableField("INDICATOR_NAME")
    private String indicatorName;

    @TableField("NUMERATOR")
    private BigDecimal numerator;

    @TableField("DENOMINATOR")
    private BigDecimal denominator;

    @TableField("RATE")
    private BigDecimal rate;

    @TableField("SOURCE_TYPE")
    private String sourceType;

    @TableField("CREATE_TIME")
    private Date createTime;

    @TableField("UPDATE_TIME")
    private Date updateTime;

    @TableField("EXT1")
    private String ext1;

    @TableField("EXT2")
    private String ext2;

    @TableField("EXT3")
    private String ext3;
}
