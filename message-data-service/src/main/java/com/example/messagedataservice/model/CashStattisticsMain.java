package com.example.messagedataservice.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
public class CashStattisticsMain {
    //流水号
    private Long serialNo;
    //报表日期
    private Date reportDate;
    //报表年份
    private Integer reportYear;
    //是否有效 (0,1 默认1)
    private Boolean isvalid;
    //创建人
    private String creator;
    //创建时间
    private Date createTime;
    //更新时间
    private Date updateTime;
    //关联的子报表列表（一对多关系）
    private List<CashStatisticsSub> subReports = new ArrayList<>();

    // 添加子报表的方法
    public void addSubReport(CashStatisticsSub subReport) {
        if (subReport != null) {
            subReport.setSerialNo(this.serialNo); // 设置关联的流水号
            this.subReports.add(subReport);
        }
    }



}
