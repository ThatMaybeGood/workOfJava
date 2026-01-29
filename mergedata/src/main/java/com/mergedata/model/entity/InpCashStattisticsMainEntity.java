package com.mergedata.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/*
 *住院报表
 */
@Data
@TableName("mpp_cash_statistics_inp_master") // 只要在这里指定数据库真实的表名
public class InpCashStattisticsMainEntity {
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
    private LocalDateTime createTime;
    //更新时间
    private LocalDateTime updateTime;
    //关联的子报表列表（一对多关系）
    private List<OutpCashStatisticsSubEntity> subs ;


}
