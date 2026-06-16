package com.reports.service;

import com.reports.dto.common.PageResult;
import com.reports.dto.request.OutpatientRoomUsageRequest;
import com.reports.dto.response.outpatient.room.usage.OverviewData;
import com.reports.dto.response.outpatient.room.usage.TableItem;

/**
 * 诊室使用率分析服务
 */
public interface OutpatientRoomUsageService {

    /**
     * 查询概览数据
     */
    OverviewData queryOverview(OutpatientRoomUsageRequest request);

    /**
     * 查询表格数据（分页）
     */
    PageResult<TableItem> queryTable(OutpatientRoomUsageRequest request, Integer page, Integer pageSize);

}
