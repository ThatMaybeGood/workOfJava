package com.reports.dto.response.outpatient.window.stats;

import lombok.Data;
import java.util.List;

/**
 * 人工窗口统计 - 年龄分析
 */
@Data
public class AgeAnalysis {

    private List<String> categories;
    private List<Integer> data;

}
