package com.reports.dto.response.cash.mi;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 未匹配负交易(pay_bills退费在biz表和pay表都找不到对应正交易, 即机构侧内连接丢失的部分)
 */
@Data
public class MiReconUnmatchedNegItem {

    /** 结算ID */
    private String setlId;

    /** 结算日期 */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date settleDate;

    /** 类型代码: 310/390/3101/3901 */
    private String insurType;

    /** 类型名称 */
    private String insurTypeName;

    /** 医疗总金额(退费, 负数) */
    private BigDecimal medfeeSumamt;
}
