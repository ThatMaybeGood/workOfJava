package com.mergedata.model.vo.pie;


import lombok.Data;

import java.util.Map;

/**
 * 财务报表响应实体
 */
@Data
public class OutReportPieDTO {
    private DateRangeDTO queryDateRange;                              // 查询日期范围
    private Map<String, ItemDetailDTO> coreItems;                    // 核心5个项目（预交金、门诊收入、暂收款、实交报表数、疫苗收入）
    private Map<String, AuxiliaryMetricDTO> auxiliaryItems;          // 辅助5个项目（门诊借款、住院借款、门诊回款、住院回款、门诊实存）
    private SummaryDTO summary;                                       // 汇总统计
}