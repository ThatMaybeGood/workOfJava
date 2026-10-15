package com.reports.dto.response.cash.cashier.settlement;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 收费员结账统计 - 下拉选项（表格按操作员/来源维度取数时的可选值）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DimOption {

    /** 选项值：操作员工号 / 渠道名 */
    private String value;

    /** 选项显示：操作员姓名(staff_dict查不到回退工号) / 渠道名 */
    private String label;
}
