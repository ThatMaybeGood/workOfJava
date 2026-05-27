package com.mergedata.model.vo.pie;


import lombok.Data;
import java.math.BigDecimal;

/**
 * 辅助项目实体（借款、回款、实存）
 * 这类只有金额，没有笔数概念
 */
@Data
public class AuxiliaryMetricDTO {
    private String itemName;    // 项目名称
    private BigDecimal amount;  // 金额
}