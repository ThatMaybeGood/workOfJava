package com.reports.dto.response.cash.cashier.settlement;

import com.reports.dto.common.PageResult;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 收费员结账统计 - 表格分页结果（附带维度下拉选项）
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CashierTableResult extends PageResult<TableItem> {

    private static final long serialVersionUID = 1L;

    /** 维度下拉选项（按收费员=工号+姓名 / 按来源=渠道） */
    private List<DimOption> dims;

    public CashierTableResult(List<TableItem> list, Long total, Integer page, Integer pageSize, List<DimOption> dims) {
        super(list, total, page, pageSize);
        this.dims = dims;
    }
}
