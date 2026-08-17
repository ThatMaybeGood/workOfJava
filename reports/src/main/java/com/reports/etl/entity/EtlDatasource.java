package com.reports.etl.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("etl_datasource")
public class EtlDatasource implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String dbType;

    private String driverClass;

    private String url;

    private String username;

    private String password;

    private String role;

    private Integer poolInitialSize;

    private Integer poolMaxActive;

    private Integer enabled;

    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;
}