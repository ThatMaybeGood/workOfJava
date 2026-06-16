package com.reports.dto.response.cash.cashier.settlement;

import lombok.Data;
import java.util.List;

/**
 * 收费员结账统计 - 图表数据
 */
@Data
public class ChartData {

    private String title;
    private String subTitle;
    private String dateRange;
    private List<String> categories;
    private List<Integer> data;

}
