package com.reports.dto.response.outpatient.lab.stats;

import lombok.Data;
import java.util.List;

/**
 * 检验统计 - 分时段采血分析
 */
@Data
public class TimeAnalysis {

    private List<String> categories;
    private List<Integer> data;

}
