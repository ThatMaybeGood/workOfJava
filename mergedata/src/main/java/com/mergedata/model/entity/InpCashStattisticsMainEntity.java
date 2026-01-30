package com.mergedata.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 住院现金统计主表实体类
 */
@Data
@TableName("mpp_cash_statistics_inp_master")
public class InpCashStattisticsMainEntity {
    @TableId
    private String serialNo;              // 流水号

    private LocalDate reportDate;         // 报表日期
    private Integer reportYear;           // 报表年份

    private Integer reportStatus;            // 报表状态  0：未生效 1：已生效

    private String creator;               // 创建人
    private LocalDateTime createTime;     // 创建时间
    private LocalDateTime updateTime;     // 更新时间
    // 关联的子报表列表（一对多关系）
    @TableField(exist = false)
    private List<InpCashStatisticsSubEntity> subs;

//    // 可以添加其他业务字段
//    private String reportType;           // 报表类型
//    private String reportStatus;         // 报表状态
//    private BigDecimal totalAmount;      // 总金额
}
