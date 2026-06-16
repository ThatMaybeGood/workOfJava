package com.reports.dto.response.outpatient.room.usage;

import com.reports.dto.common.PageResult;
import lombok.Data;

/**
 * 诊室使用率分析 - 响应体
 */
@Data
public class OutpatientRoomUsageResponse {

    private static final long serialVersionUID = 1L;

    private OverviewData overview;
    private PageResult<TableItem> table;

}
