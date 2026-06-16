package com.reports.service;

import com.reports.dto.common.PageResult;
import com.reports.dto.request.OutpatientNoShowRequest;
import com.reports.dto.response.outpatient.no.show.AgeAnalysis;
import com.reports.dto.response.outpatient.no.show.AnalysisItem;
import com.reports.dto.response.outpatient.no.show.OverviewData;
import com.reports.dto.response.outpatient.no.show.TableItem;

import java.util.List;

/**
 * 爽约退号分析服务
 */
public interface OutpatientNoShowService {

    /**
     * 查询概览数据
     */
    OverviewData queryOverview(OutpatientNoShowRequest request);

    /**
     * 查询退号来源分析
     */
    List<AnalysisItem> queryRefundOrigin(OutpatientNoShowRequest request);

    /**
     * 查询退号渠道分析
     */
    List<AnalysisItem> queryRefundChannel(OutpatientNoShowRequest request);

    /**
     * 查询年龄分析数据
     */
    AgeAnalysis queryAgeAnalysis(OutpatientNoShowRequest request);

    /**
     * 查询明细表格（分页）
     */
    PageResult<TableItem> queryTable(OutpatientNoShowRequest request, Integer page, Integer pageSize);

}
