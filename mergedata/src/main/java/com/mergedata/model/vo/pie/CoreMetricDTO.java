package com.mergedata.model.vo.pie;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 核心指标通用实体
 * 收入、退款、合计都使用同一个实体
 */
@Data
public class CoreMetricDTO {
    private String itemName;        // 项目名称
    private BigDecimal amount;      // 金额
    private Integer count;          // 笔数
}