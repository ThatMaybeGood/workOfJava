package com.etl.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("etl_column_mapping")
public class EtlColumnMapping {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String taskCode;

    private String sourceColumn;

    private String targetColumn;

    private String dataType;

    private String defaultValue;

    private String transformExpr;

    private Integer mappingOrder;

    private String isPrimaryKey;

    private String enabled;

    private String description;
}
