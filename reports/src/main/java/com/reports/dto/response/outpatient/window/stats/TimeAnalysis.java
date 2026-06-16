package com.reports.dto.response.outpatient.window.stats;

import lombok.Data;
import java.util.List;

/**
 * 人工窗口统计 - 分时段业务量分析
 */
@Data
public class TimeAnalysis {

    private List<String> categories;
    private List<Integer> data;

}
