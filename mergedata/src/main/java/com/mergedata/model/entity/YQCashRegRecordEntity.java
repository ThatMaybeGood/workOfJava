package com.mergedata.model.entity;


import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.Value;

import java.math.BigDecimal;

/**
 * 医院现金登记记录实体类
 */
@Data
@TableName("mpp_cash_reg_record") // 真实的表名
public class YQCashRegRecordEntity {
    /*
     * 操作员
     */
    private String  operator;
    /**
     * 操作员编号
     */
    private String operatorNo;
    /**
     * 留存现金数
     */
    @TableField(value = "amount")
    private BigDecimal retainedCash;
    /**
     * 申请日期
     */
    private String  applyDate;
    /**
     * 创建时间
     */
    private String createTime;
    /**
     * 操作类型
     */
    public String opeType;

    /**
     * 窗口号
     */
    public String windowNo;

    /**
     * 排班号
     */
    public String scheduling;

     /**
      * 报表日期
      */
    public String saveDate;

    /**
     * 现金登记记录编号
     */
     private String dbUser;



}
