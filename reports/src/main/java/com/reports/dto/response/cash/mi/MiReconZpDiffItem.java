package com.reports.dto.response.cash.mi;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 账平差异明细(HIS业务表ips_dz_biz_bills vs 医保支付表ips_dz_pay_bills, 双边zp_flag=1按setl_id比对)
 */
@Data
public class MiReconZpDiffItem {

    /** 结算ID */
    private String setlId;

    /** 结算日期 */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date settleDate;

    /** 类型代码: 310/390/3101/3901 */
    private String insurType;

    /** 类型名称 */
    private String insurTypeName;

    /** 医疗总金额 */
    private BigDecimal medfeeSumamt;

    /** 来源类型: 医保支付表/HIS业务表(SQL直出中文, 账平仅存在的一方) */
    private String diffSide;
}
