package com.reports.dto.response.cash.mi;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 两表差异明细(HIS业务表ips_dz_biz_bills vs 医保支付表ips_dz_pay_bills, 不限账平标志)
 * 三种差异: 仅医保支付表/仅HIS业务表(setl_id单边存在) + 账平标志不一致(同setl_id两边zp_flag不同)
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

    /** 差异类型: 仅医保支付表/仅HIS业务表/账平标志不一致(SQL直出中文) */
    private String diffSide;

    /** 医保支付表账平标志代码(1账平/-1单边/-2金额不等, 该侧无数据为null) */
    private String payZp;

    /** 医保支付表账平标志中文 */
    private String payZpName;

    /** HIS业务表账平标志代码(该侧无数据为null) */
    private String bizZp;

    /** HIS业务表账平标志中文 */
    private String bizZpName;

    /** 医保支付表医疗总金额(该侧无数据为null) */
    private BigDecimal payFee;

    /** HIS业务表医疗总金额(该侧无数据为null) */
    private BigDecimal bizFee;

    /** 自对冲标记: 与医保表内一条同就诊同人员金额相反的数据自相抵消('自对冲', 不是为null) */
    private String offsetPair;

    /** 配对键(就诊号mdtrt_id, 前端据此把自对冲的两条排在一起, 其他类型为null) */
    private String pairKey;
}
