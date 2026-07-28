package com.reports.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 预测门诊量报表-30天明细
 */
@Data
@TableName("TR_OUTP_FC_MONTH")
public class OutpatientForecastMonthEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId("id")
    /**
     * 主键ID
     */
    private Long id;

    @TableField("stat_date")
    /**
     * 统计日期
     */
    private Date statDate;

    @TableField("forecast_date")
    /**
     * 预测日期
     */
    private Date forecastDate;

    @TableField("forecast_value")
    /**
     * 预测值
     */
    private Integer forecastValue;

    @TableField("create_time")
    /**
     * 创建时间
     */
    private Date createTime;

    @TableField("update_time")
    /**
     * 更新时间
     */
    private Date updateTime;

    @TableField("ext1")
    /**
     * 扩展字段1
     */
    private String ext1;

    @TableField("ext2")
    /**
     * 扩展字段2
     */
    private String ext2;

    @TableField("ext3")
    /**
     * 扩展字段3
     */
    private String ext3;
}
