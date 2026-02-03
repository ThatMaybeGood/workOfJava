package com.mergedata.model.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.mergedata.constants.Constant;
import com.mergedata.model.entity.InpCashSubEntity;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * 住院现金统计主表实体类
 */
@Data
public class InpReportVO {

    @TableId(value = "serial_no")
    private String serialNo;              // 流水号

    @TableField(value = "report_date")
    private LocalDate reportDate;         // 报表日期
    @TableField(value = "report_year")
    private Integer reportYear;           // 报表年份

    @TableField(value = "report_type")
    private String reportType = Constant.TYPE_INP;            // 报表类型 0：门诊 1：住院

    @TableField(value = "holiday_tatol_flag")
    private String holidayTotalFlag = Constant.FLAG_NO; //节假日汇总标志

    // 关联的子报表列表（一对多关系）
    @TableField(exist = false)
    private List<InpCashSubEntity> subs;

}
