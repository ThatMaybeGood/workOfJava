package com.reports.etl.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("etl_mapping")
public class EtlMapping implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long taskId;

    private String srcField;

    private String tgtField;

    private String defaultValue;

    private Integer isUpdateCol;

    private String srcType;

    private String tgtType;

    private Integer sortOrder;

    @TableField(fill = FieldFill.INSERT)
    private Date createTime;
}