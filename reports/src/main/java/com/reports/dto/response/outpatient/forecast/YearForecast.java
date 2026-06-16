package com.reports.dto.response.outpatient.forecast;

import lombok.Data;
import java.util.List;

/**
 * 预测门诊量报表 - 未来12个月预测数据
 */
@Data
public class YearForecast {

    private List<String> months;
    private List<Integer> data;

}
