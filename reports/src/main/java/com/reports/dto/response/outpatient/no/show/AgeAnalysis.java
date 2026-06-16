package com.reports.dto.response.outpatient.no.show;

import lombok.Data;
import java.util.List;

/**
 * 爽约退号分析 - 年龄分析
 */
@Data
public class AgeAnalysis {

    private List<String> categories;
    private List<Integer> data;

}
