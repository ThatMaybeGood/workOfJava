package com.reports.dto.response.outpatient.window.stats;

import lombok.Data;
import java.util.List;

/**
 * 人工窗口统计 - 分时段工作量统计
 */
@Data
public class WorkloadTable {

    private List<String> headers;
    private List<WorkloadRow> rows;

}
