package com.reports.dto.response.outpatient.window.stats;

import lombok.Data;
import java.util.List;

/**
 * 人工窗口统计 - 工作量行数据
 */
@Data
public class WorkloadRow {

    private String business;
    private List<Integer> data;

}
