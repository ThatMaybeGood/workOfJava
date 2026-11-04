package com.reports.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 医保对账明细表(上传文件按结算日期聚合落库)
 */
@Data
@TableName("tr_mi_recon_detail")
public class MiReconDetailEntity {

    @TableId("id")
    private Long id;

    /** 来源上传记录tr_mi_recon_file.id */
    @TableField("file_id")
    private Long fileId;

    /** 医保类型: 310职工 390居民 3101异地职工 3901异地居民 */
    @TableField("insur_type")
    private String insurType;

    /** 医保类型中文名 */
    @TableField("insur_type_name")
    private String insurTypeName;

    /** 对账数据量 */
    @TableField("qty")
    private Long qty;

    /** 医疗费总额 */
    @TableField("total_fee")
    private BigDecimal totalFee;

    /** 结算日期 */
    @TableField("settle_date")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date settleDate;

    @TableField("create_time")
    private Date createTime;
}
