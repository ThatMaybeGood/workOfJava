package com.mergedata.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 门诊结账主表实体类
 */
@Data
@TableName("mpp_cash_statistics_master")
public class OutpCashMainEntity {
    /**
     * 主键：流水号
     */
    @TableId(type = IdType.INPUT)
    @TableField("serial_no")
    private String serialNo;
    /**
     * 报表日期
     */
    private LocalDate reportDate;
    /**
     * 报表年份
     */
    private Integer reportYear;
    /**
     * 是否有效 (0,1 默认1)
     */

    private String validFlag;
    /**
     * 创建人
     */
    private String creator;
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    //==================================添加字段============================
     /**
     * 是否汇总标志  前端调用穿=传入
     */
    private String totalFlag;
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
//     /**
//     * 门诊当日实存金额
//     */
//    private BigDecimal outpArrearsTotal;

    /**
     * 备注
     */
    private String remark;

    /**
     * 关联的子报表列表（一对多关系）
     */
     @TableField(exist = false)
    private List<OutpCashSubEntity> subs =new ArrayList<>() ;


}
