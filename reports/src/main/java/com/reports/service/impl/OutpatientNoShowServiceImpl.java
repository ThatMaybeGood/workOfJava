package com.reports.service.impl;

import com.reports.config.ReportDataConfig;
import com.reports.dto.common.PageResult;
import com.reports.dto.request.OutpatientNoShowRequest;
import com.reports.dto.response.outpatient.no.show.*;
import com.reports.service.OutpatientNoShowService;
import com.reports.util.SeqUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 爽约退号分析服务实现
 */
@Slf4j
@Service
public class OutpatientNoShowServiceImpl implements OutpatientNoShowService {

    private final ReportDataConfig dataConfig;
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public OutpatientNoShowServiceImpl(ReportDataConfig dataConfig, JdbcTemplate jdbcTemplate) {
        this.dataConfig = dataConfig;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public OverviewData queryOverview(OutpatientNoShowRequest request) {
        log.info("查询爽约退号概览数据，mode={}", dataConfig.getMode());
        if (dataConfig.isMock()) {
            return queryOverviewMock(request);
        } else if (dataConfig.isJdbc()) {
            return queryOverviewByJdbc(request);
        } else {
            return queryOverviewByMybatisPlus(request);
        }
    }

    @Override
    public AgeAnalysis queryAgeAnalysis(OutpatientNoShowRequest request) {
        log.info("查询爽约退号年龄分析，mode={}", dataConfig.getMode());
        if (dataConfig.isMock()) {
            return queryAgeAnalysisMock(request);
        } else if (dataConfig.isJdbc()) {
            return queryAgeAnalysisByJdbc(request);
        } else {
            return queryAgeAnalysisByMybatisPlus(request);
        }
    }

    @Override
    public List<AnalysisItem> queryRefundOrigin(OutpatientNoShowRequest request) {
        log.info("查询爽约退号来源分析，mode={}", dataConfig.getMode());
        if (dataConfig.isMock()) {
            return queryRefundOriginMock(request);
        } else if (dataConfig.isJdbc()) {
            return queryRefundOriginByJdbc(request);
        } else {
            return queryRefundOriginByMybatisPlus(request);
        }
    }

    @Override
    public List<AnalysisItem> queryRefundChannel(OutpatientNoShowRequest request) {
        log.info("查询爽约退号渠道分析，mode={}", dataConfig.getMode());
        if (dataConfig.isMock()) {
            return queryRefundChannelMock(request);
        } else if (dataConfig.isJdbc()) {
            return queryRefundChannelByJdbc(request);
        } else {
            return queryRefundChannelByMybatisPlus(request);
        }
    }

    @Override
    public PageResult<TableItem> queryTable(OutpatientNoShowRequest request, Integer page, Integer pageSize) {
        log.info("查询爽约退号明细表格，mode={}", dataConfig.getMode());
        if (dataConfig.isMock()) {
            return queryTableMock(request, page, pageSize);
        } else if (dataConfig.isJdbc()) {
            return queryTableByJdbc(request, page, pageSize);
        } else {
            return queryTableByMybatisPlus(request, page, pageSize);
        }
    }

    // ==================== Mock 模式 ====================

    private OverviewData queryOverviewMock(OutpatientNoShowRequest request) {
        SeqUtil.next();
        OverviewData overview = new OverviewData();
        overview.setRefundCount(128);
        overview.setRefundRate("2.60%");
        overview.setNoShowCount(256);
        overview.setNoShowRate("5.20%");
        return overview;
    }

    private AgeAnalysis queryAgeAnalysisMock(OutpatientNoShowRequest request) {
        SeqUtil.next();
        AgeAnalysis analysis = new AgeAnalysis();
        List<String> categories = new ArrayList<>();
        categories.add("0-18岁");
        categories.add("19-35岁");
        categories.add("36-50岁");
        categories.add("51-65岁");
        categories.add("65岁以上");
        analysis.setCategories(categories);
        List<Integer> data = new ArrayList<>();
        data.add(20);
        data.add(80);
        data.add(70);
        data.add(50);
        data.add(36);
        analysis.setData(data);
        return analysis;
    }

    private List<AnalysisItem> queryRefundOriginMock(OutpatientNoShowRequest request) {
        SeqUtil.next();
        List<AnalysisItem> list = new ArrayList<>();
        list.add(newAnalysisItem("微信预约", 80));
        list.add(newAnalysisItem("电话预约", 30));
        list.add(newAnalysisItem("现场挂号", 18));
        return list;
    }

    private List<AnalysisItem> queryRefundChannelMock(OutpatientNoShowRequest request) {
        SeqUtil.next();
        List<AnalysisItem> list = new ArrayList<>();
        list.add(newAnalysisItem("窗口退费", 60));
        list.add(newAnalysisItem("自助机退费", 40));
        list.add(newAnalysisItem("线上退费", 28));
        return list;
    }

    private PageResult<TableItem> queryTableMock(OutpatientNoShowRequest request, Integer page, Integer pageSize) {
        SeqUtil.next();
        List<TableItem> list = new ArrayList<>();
        for (int i = 0; i < pageSize; i++) {
            TableItem item = new TableItem();
            item.setDeptName("心血管内科" + (i + 1));
            item.setRefundCount(5 + i);
            item.setRefundRate("2.00%");
            item.setNoShowCount(10 + i);
            item.setNoShowRate("5.00%");
            list.add(item);
        }
        return PageResult.of(list, 55L, page, pageSize);
    }

    // ==================== JdbcTemplate 模式 ====================

    private OverviewData queryOverviewByJdbc(OutpatientNoShowRequest request) {
        log.info("使用 JdbcTemplate 查询概览数据");
        return queryOverviewMock(request);
    }

    private AgeAnalysis queryAgeAnalysisByJdbc(OutpatientNoShowRequest request) {
        return queryAgeAnalysisMock(request);
    }

    private List<AnalysisItem> queryRefundOriginByJdbc(OutpatientNoShowRequest request) {
        return queryRefundOriginMock(request);
    }

    private List<AnalysisItem> queryRefundChannelByJdbc(OutpatientNoShowRequest request) {
        return queryRefundChannelMock(request);
    }

    private PageResult<TableItem> queryTableByJdbc(OutpatientNoShowRequest request, Integer page, Integer pageSize) {
        return queryTableMock(request, page, pageSize);
    }

    // ==================== MyBatis-Plus 模式 ====================

    private OverviewData queryOverviewByMybatisPlus(OutpatientNoShowRequest request) {
        return queryOverviewMock(request);
    }

    private AgeAnalysis queryAgeAnalysisByMybatisPlus(OutpatientNoShowRequest request) {
        return queryAgeAnalysisMock(request);
    }

    private List<AnalysisItem> queryRefundOriginByMybatisPlus(OutpatientNoShowRequest request) {
        return queryRefundOriginMock(request);
    }

    private List<AnalysisItem> queryRefundChannelByMybatisPlus(OutpatientNoShowRequest request) {
        return queryRefundChannelMock(request);
    }

    private PageResult<TableItem> queryTableByMybatisPlus(OutpatientNoShowRequest request, Integer page, Integer pageSize) {
        return queryTableMock(request, page, pageSize);
    }

    // ==================== 工具方法 ====================

    private AnalysisItem newAnalysisItem(String name, int value) {
        AnalysisItem item = new AnalysisItem();
        item.setName(name);
        item.setValue(value);
        return item;
    }

}
