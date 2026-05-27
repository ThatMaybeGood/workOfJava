package com.mergedata.model.vo.pie;


import lombok.Data;

/**
 * 单个项目的完整数据（收入 + 退款 + 合计）
 */
@Data
public class ItemDetailDTO {
    private CoreMetricDTO income;   // 收入数据
    private CoreMetricDTO refund;   // 退款数据
    private CoreMetricDTO total;    // 合计数据（金额 = 收入金额 - 退款金额，笔数 = 收入笔数 + 退款笔数）
}