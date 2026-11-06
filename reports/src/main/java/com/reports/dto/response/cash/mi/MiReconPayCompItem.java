package com.reports.dto.response.cash.mi;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 支付构成行(dim=sum按险种 / dim=cat按险种+类别 / dim=day按日期 / dim=dayCat按日期+类别)
 */
@Data
public class MiReconPayCompItem {

    /** 类型代码: 310/390/3101/3901/515253 */
    private String insurType;

    /** 类型名称(IPS_DZ_INSUTYPE_DICT字典) */
    private String insurTypeName;

    /** 类别: 账平/医保负金额-退费/医保正金额-收费(cat/dayCat维度有值) */
    private String category;

    /** 结算日期(day/dayCat维度有值) */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date settleDate;

    /** 条数 */
    private Long qty;

    /** 医疗费总额 */
    private BigDecimal medfeeSumamt;

    /** 个人账户支出 */
    private BigDecimal acctPay;

    /** 统筹基金支出 */
    private BigDecimal hlfpPay;

    /** 现金支出 */
    private BigDecimal cashPay;

    /** 公务员补助金支出 */
    private BigDecimal cvlservPay;

    /** 企业补充医疗保险基金支出 */
    private BigDecimal hlfesPay;

    /** 居民大病支出 */
    private BigDecimal hlfmiPay;

    /** 职工大病支出 */
    private BigDecimal hlfobPay;

    /** 医疗救助支出 */
    private BigDecimal mafPay;

    /** 其他支出 */
    private BigDecimal othPay;
}
