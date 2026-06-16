package com.reports.dto.response.outpatient.no.show;

import lombok.Data;
import java.util.Map;

/**
 * 爽约退号分析 - 表格行数据
 */
@Data
public class TableItem {

    private String deptName;
    private Integer refundCount;
    private String refundRate;
    private Map<String, String> refundOrigin;
    private Map<String, Integer> refundChannel;
    private Integer noShowCount;
    private String noShowRate;
    private Map<String, String> noShowOrigin;

}
