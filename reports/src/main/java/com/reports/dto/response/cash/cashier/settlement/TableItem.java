package com.reports.dto.response.cash.cashier.settlement;

import lombok.Data;
import java.util.Map;

/**
 * 收费员结账统计 - 表格行数据（支持动态列）
 */
@Data
public class TableItem {

    /**
     * 日期
     */
    private String date;

    /**
     * 动态列数据（收费员列、汇总列等）
     */
    private Map<String, Object> columns;

}
