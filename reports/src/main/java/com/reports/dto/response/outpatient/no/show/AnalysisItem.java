package com.reports.dto.response.outpatient.no.show;

import lombok.Data;

/**
 * 爽约退号分析 - 分析单项（用于归属地、渠道等）
 */
@Data
public class AnalysisItem {

    private String name;
    private Integer value;

}
