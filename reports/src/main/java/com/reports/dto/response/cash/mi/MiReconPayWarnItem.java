package com.reports.dto.response.cash.mi;

import lombok.Data;

/**
 * 支付构成主表退费匹配警示行(dim=warn): 每险种当月退费匹配概况
 */
@Data
public class MiReconPayWarnItem {

    /** 类型代码: 310/390/3101/3901/515253 */
    private String insurType;

    /** 类型名称 */
    private String insurTypeName;

    /** 当月退费总条数 */
    private Long refundTotal;

    /** 跨月匹配条数(本月未匹配、上月月初~退费当天在HIS找到正交易) */
    private Long crossCnt;

    /** 找不到条数(查找范围内均无对应正交易) */
    private Long noneCnt;
}
