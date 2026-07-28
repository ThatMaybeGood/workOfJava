package com.reports.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 出院结算报表-图表
 */
@Data
@TableName("TR_DISCH_SETTLE_CHT")
public class DischSettleChtEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @TableId("id")
    private Long id;

    /** 统计日期 */
    @TableField("stat_date")
    private Date statDate;

    /** 图表类型(CHANNEL/PATIENT_TYPE/AMOUNT_TYPE) */
    @TableField("chart_type")
    private String chartType;

    /** 项目名称 */
    @TableField("item_name")
    private String itemName;

    /** 数值 */
    @TableField("item_value")
    private Integer itemValue;

    /** 对比值 */
    @TableField("item_compare")
    private Integer itemCompare;

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
