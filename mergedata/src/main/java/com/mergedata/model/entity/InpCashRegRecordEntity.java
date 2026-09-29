package com.mergedata.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 住院现金登记表实体类(手机端登记库存金额/差额)
 */
@Data
@TableName("MPP_INP_CASH_REG_RECORD")
public class InpCashRegRecordEntity {

    /** 登记日期 */
    private LocalDate saveDate;
    /** 操作员编号 */
    private String operatorNo;
    /** 库存金额 */
    private BigDecimal pettyAmount;
    /** 节假日交款(列名difference_amount, 手机端实际登记的是节假日交款) */
    @TableField("difference_amount")
    private BigDecimal holidayPayment;
    /** 登录编号 */
    private String dbUser;

}
