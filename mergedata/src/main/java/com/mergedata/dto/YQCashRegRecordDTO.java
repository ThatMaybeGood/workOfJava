package com.mergedata.dto;


import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

// 定义一个类，用于存储医院现金报表记录
@Data
public class YQCashRegRecordDTO {
    private String  operatorNo;
    //留存现金数
    private BigDecimal retainedCash; //his

    private LocalDate  applyDate;

    private LocalDate createTime;

    private String operatorName;

    public String operatType;

    public String windowNo;

    public String sechduling;

    //报表日期
    public String saveDate;


}
