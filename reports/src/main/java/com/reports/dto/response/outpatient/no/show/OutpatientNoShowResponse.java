package com.reports.dto.response.outpatient.no.show;

import com.reports.dto.common.PageResult;
import lombok.Data;
import java.util.List;
import java.util.Map;

/**
 * 爽约退号分析 - 响应体
 */
@Data
public class OutpatientNoShowResponse {

    private static final long serialVersionUID = 1L;

    private OverviewData overview;
    private List<AnalysisItem> refundOrigin;
    private List<AnalysisItem> refundChannel;
    private AgeAnalysis ageAnalysis;
    private PageResult<TableItem> table;

}
