package com.reports.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 院前操作员维护表
 */
@Data
@TableName("tr_prehosp_oper")
public class PreHospOperEntity {

    @TableId("id")
    private Long id;

    /** 操作员=HIS users.USER_ID(统计查询按此过滤) */
    @TableField("oper_no")
    private String operNo;

    /** 登录ID=HIS users.DB_USER(维护页添加/显示) */
    @TableField("dbuser")
    private String dbuser;

    /** 是否有效: 1有效 0无效 */
    @TableField("valid_flag")
    private Integer validFlag;

    @TableField("create_time")
    private Date createTime;

    @TableField("update_time")
    private Date updateTime;

    /** 操作员姓名(HIS users.USER_NAME回显, 非本表列) */
    @TableField(exist = false)
    private String operName;
}
