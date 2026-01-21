package com.mergedata.model.entity;


import lombok.Data;

import java.math.BigDecimal;

// 定义一个类，用于存储医院现金报表记录
@Data
public class YQCashRegRecordEntity {
    private String  operator;
    //操作员编号
    private String operatorNo;

    //留存现金数
    private BigDecimal retainedCash; //his

    private String  applyDate;

    private String createTime;

    public String opeType;

    public String windowNo;

    public String scheduling;

    //报表日期
    public String saveDate;



}
