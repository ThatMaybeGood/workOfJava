package com.reports.dto.response.outpatient.forecast;

import lombok.Data;
import java.util.List;

/**
 * 预测门诊量报表 - 未来30天预测数据
 */
@Data
public class MonthForecast {

    private List<String> dates;
    private List<Integer> data;

}
