package com.reports.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 门诊财务报表-抽取：门诊挂号明细（ETL 按就诊日期抽取，源表 clinic_master）
 */
@Data
@TableName("ETL_CLINIC")
public class EtlClinicEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @TableId("id")
    private Long id;

    /** 患者ID（bt2 关联排班表） */
    @TableField("patient_id")
    private String patientId;

    /** 就诊日期（门诊量、抽取日期） */
    @TableField("visit_date")
    private Date visitDate;

    /** 退号日期（门诊量 T2/T3 退号筛选） */
    @TableField("returned_date")
    private Date returnedDate;

    /** 挂号费（bt8 分界前挂号费） */
    @TableField("regist_fee")
    private BigDecimal registFee;

    /** 诊查费（bt8 分界前挂号费） */
    @TableField("clinic_fee")
    private BigDecimal clinicFee;

    /** 诊室标签（bt2 拼接 schedule_id） */
    @TableField("clinic_label")
    private String clinicLabel;

    /** 就诊时段（bt2 拼接 schedule_id） */
    @TableField("visit_time_desc")
    private String visitTimeDesc;

    /** 操作员号（bt2 operator 归类） */
    @TableField("operator_no")
    private String operatorNo;

    /** 创建时间 */
    @TableField("create_time")
    private Date createTime;

    /** 更新时间 */
    @TableField("update_time")
    private Date updateTime;
}
