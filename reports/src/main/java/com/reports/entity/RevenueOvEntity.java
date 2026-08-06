package com.reports.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 门诊收入分析-概览
 */
@Data
@TableName("TR_REV_OV")
public class RevenueOvEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @TableId("id")
    private Long id;

    /** 统计日期 */
    @TableField("stat_date")
    private Date statDate;

    /** 门诊收入 */
    @TableField("outpatient_revenue")
    private BigDecimal outpatientRevenue;

    /** 药品收入 */
    @TableField("medical_revenue")
    private BigDecimal medicalRevenue;

    /** 挂号收入 */
    @TableField("register_revenue")
    private BigDecimal registerRevenue;

    /** 服务性收入 */
    @TableField("service_revenue")
    private BigDecimal serviceRevenue;

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
