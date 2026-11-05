package com.reports.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 医保对账上传记录表
 */
@Data
@TableName("tr_mi_recon_file")
public class MiReconFileEntity {

    @TableId("id")
    private Long id;

    /** 原始文件名 */
    @TableField("file_name")
    private String fileName;

    /** 医保类型: 310职工 390居民 3101异地职工 3901异地居民 */
    @TableField("insur_type")
    private String insurType;

    /** 医保类型中文名 */
    @TableField("insur_type_name")
    private String insurTypeName;

    /** 数据日期(上传时选择, 默认当天) */
    @TableField("data_date")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date dataDate;

    /** 文件明细行数(按结算日期聚合后) */
    @TableField("row_count")
    private Integer rowCount;

    /** 启用状态: 1启用 0已作废(同类型同数据日期重复上传时旧记录自动作废) */
    @TableField("status")
    private String status;

    @TableField("create_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @TableField("update_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;
}
