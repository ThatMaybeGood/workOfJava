package com.mergedata.model;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class CashStattisticsMain {
    //流水号
    private String serialNo;
    //报表日期
    private LocalDate reportDate;
    //报表年份
    private Integer reportYear;
    //是否有效 (0,1 默认1)
    private Boolean isvalid;
    //创建人
    private String creator;
    //创建时间
    private LocalDate createTime;
    //更新时间
    private LocalDate updateTime;
    //关联的子报表列表（一对多关系）
    private List<CashStatisticsSub> subs ;


}
