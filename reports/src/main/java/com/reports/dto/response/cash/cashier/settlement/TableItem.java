package com.reports.dto.response.cash.cashier.settlement;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import lombok.Data;
import java.util.Map;

/**
 * 收费员结账统计 - 表格行数据（支持动态列）
 *
 * <p>页面是按「行 = 日期、列 = 动态维度名」渲染的，直接读 {@code row['收费员1']}、
 * {@code row['预约挂号量']}、{@code row['汇总']} 这种顶层键。
 * 所以 columns 用 {@link JsonAnyGetter} 把键值平铺到行对象的顶层，
 * 序列化出来就是 {@code {date, 收费员1: n, ..., 汇总: n}}，
 * 不会出现多余的 columns 层级。
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

    @JsonAnyGetter
    public Map<String, Object> getColumns() {
        return columns;
    }

}
