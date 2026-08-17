package com.reports.etl.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("etl_log_config")
public class EtlLogConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.INPUT)
    private Long id;

    private Integer saveDays;

    private Integer autoClean;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;
}