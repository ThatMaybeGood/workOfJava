package com.reports.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 爽约退号分析-科室明细
 */
@Data
@TableName("TR_NOSHOW_DTL")
public class NoShowDtlEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @TableId("id")
    private Long id;

    /** 统计日期 */
    @TableField("stat_date")
    private Date statDate;

    /** 科室名称 */
    @TableField("dept_name")
    private String deptName;

    /** 退号人数 */
    @TableField("refund_count")
    private Integer refundCount;

    /** 退号率 */
    @TableField("refund_rate")
    private String refundRate;

    /** 爽约人数 */
    @TableField("no_show_count")
    private Integer noShowCount;

    /** 爽约率 */
    @TableField("no_show_rate")
    private String noShowRate;

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
