package com.mergedata.model.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.mergedata.util.AddGroup;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 门诊现金统计主表VO
 */
@Data
public class OutpReportMainVONew {
    @JsonProperty("serial_no")
    private String serialNo;

    /*
    报表日期
     */
    @NotBlank(message = "报表日期不能为空", groups = {AddGroup.class})
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @JsonProperty("report_date")
    private LocalDate reportDate;

    /*
    报表年份
     */
    @JsonProperty("report_year")
    private String reportYear;

    /*
    汇总标志
     */
    @JsonProperty("total_flag")
    private String totalFlag; //汇总标志

    /*
    备注信息
     */
    @JsonProperty("remark")
    private String remark;

    /**
     * 当日暂收款合计
     */
    private BigDecimal tempReceiptTotal;
    /**
     * 日报表数合计
     */
    private BigDecimal dailyReportTotal;
    /**
     * 合计存款金额
     */
    private BigDecimal depositTotal;
    /**
     * 住院部当日借款
     */
    private BigDecimal borrowTotal;
    /**
     * 住院部当日回款
     */
    private BigDecimal repayTotal;
    /**
     * 门诊当日借款
     */
    private BigDecimal outpBorrowTotal;
    /**
     * 门诊当日回款
     */
    private BigDecimal outpRepayTotal;


    /**
     * 关联的子报表列表（一对多关系）
     */
    @TableField(exist = false)
    private List<OutpReportSubVONew> subs;

}
