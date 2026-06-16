package com.reports.dto.response.outpatient.lab.stats;

import lombok.Data;
import java.util.List;

/**
 * 检验统计 - 检验项目排名
 */
@Data
public class ReportRank {

    private List<String> categories;
    private List<Integer> data;

}
