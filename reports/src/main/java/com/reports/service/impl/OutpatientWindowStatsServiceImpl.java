package com.reports.service.impl;

import com.reports.config.ReportDataConfig;
import com.reports.dto.request.OutpatientWindowStatsRequest;
import com.reports.dto.response.outpatient.window.stats.*;
import com.reports.service.OutpatientWindowStatsService;
import com.reports.util.SeqUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 人工窗口统计服务实现
 */
@Slf4j
@Service
public class OutpatientWindowStatsServiceImpl implements OutpatientWindowStatsService {

    private final ReportDataConfig dataConfig;
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public OutpatientWindowStatsServiceImpl(ReportDataConfig dataConfig, JdbcTemplate jdbcTemplate) {
        this.dataConfig = dataConfig;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public OverviewData queryOverview(OutpatientWindowStatsRequest request) {
        log.info("查询人工窗口概览数据，mode={}", dataConfig.getMode());
        if (dataConfig.isMock()) {
            return queryOverviewMock(request);
        } else if (dataConfig.isJdbc()) {
            return queryOverviewByJdbc(request);
        } else {
            return queryOverviewByMybatisPlus(request);
        }
    }

    @Override
    public AgeAnalysis queryAgeAnalysis(OutpatientWindowStatsRequest request) {
        log.info("查询人工窗口年龄分析，mode={}", dataConfig.getMode());
        if (dataConfig.isMock()) {
            return queryAgeAnalysisMock(request);
        } else if (dataConfig.isJdbc()) {
            return queryAgeAnalysisByJdbc(request);
        } else {
            return queryAgeAnalysisByMybatisPlus(request);
        }
    }

    @Override
    public TimeAnalysis queryTimeAnalysis(OutpatientWindowStatsRequest request) {
        log.info("查询人工窗口时段分析，mode={}", dataConfig.getMode());
        if (dataConfig.isMock()) {
            return queryTimeAnalysisMock(request);
        } else if (dataConfig.isJdbc()) {
            return queryTimeAnalysisByJdbc(request);
        } else {
            return queryTimeAnalysisByMybatisPlus(request);
        }
    }

    @Override
    public List<AnalysisItem> querySourceAnalysis(OutpatientWindowStatsRequest request) {
        log.info("查询人工窗口来源分析，mode={}", dataConfig.getMode());
        if (dataConfig.isMock()) {
            return querySourceAnalysisMock(request);
        } else if (dataConfig.isJdbc()) {
            return querySourceAnalysisByJdbc(request);
        } else {
            return querySourceAnalysisByMybatisPlus(request);
        }
    }

    @Override
    public WorkloadTable queryWorkloadTable(OutpatientWindowStatsRequest request) {
        log.info("查询人工窗口工作量表格，mode={}", dataConfig.getMode());
        if (dataConfig.isMock()) {
            return queryWorkloadTableMock(request);
        } else if (dataConfig.isJdbc()) {
            return queryWorkloadTableByJdbc(request);
        } else {
            return queryWorkloadTableByMybatisPlus(request);
        }
    }

    // ==================== Mock 模式 ====================

    private OverviewData queryOverviewMock(OutpatientWindowStatsRequest request) {
        SeqUtil.next();
        OverviewData overview = new OverviewData();
        overview.setRegisterCount(3256);
        overview.setPaymentCount(2560);
        overview.setRefundCount(128);
        return overview;
    }

    private AgeAnalysis queryAgeAnalysisMock(OutpatientWindowStatsRequest request) {
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
        data.add(200);
        data.add(800);
        data.add(700);
        data.add(500);
        data.add(360);
        analysis.setData(data);
        return analysis;
    }

    private TimeAnalysis queryTimeAnalysisMock(OutpatientWindowStatsRequest request) {
        SeqUtil.next();
        TimeAnalysis analysis = new TimeAnalysis();
        List<String> categories = new ArrayList<>();
        categories.add("08:00-09:00");
        categories.add("09:00-10:00");
        categories.add("10:00-11:00");
        categories.add("11:00-12:00");
        categories.add("14:00-15:00");
        categories.add("15:00-16:00");
        categories.add("16:00-17:00");
        analysis.setCategories(categories);
        List<Integer> data = new ArrayList<>();
        data.add(500);
        data.add(800);
        data.add(700);
        data.add(600);
        data.add(400);
        data.add(500);
        data.add(300);
        analysis.setData(data);
        return analysis;
    }

    private List<AnalysisItem> querySourceAnalysisMock(OutpatientWindowStatsRequest request) {
        SeqUtil.next();
        List<AnalysisItem> list = new ArrayList<>();
        list.add(newAnalysisItem("窗口挂号", 2000));
        list.add(newAnalysisItem("自助机", 800));
        list.add(newAnalysisItem("手机APP", 456));
        return list;
    }

    private WorkloadTable queryWorkloadTableMock(OutpatientWindowStatsRequest request) {
        SeqUtil.next();
        WorkloadTable table = new WorkloadTable();
        List<String> headers = new ArrayList<>();
        headers.add("窗口");
        headers.add("挂号");
        headers.add("收费");
        headers.add("退费");
        table.setHeaders(headers);
        List<WorkloadRow> rows = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            WorkloadRow row = new WorkloadRow();
            row.setBusiness("A0" + (i + 1));
            List<Integer> data = new ArrayList<>();
            data.add(100 + i * 10);
            data.add(80 + i * 8);
            data.add(5 + i);
            row.setData(data);
            rows.add(row);
        }
        table.setRows(rows);
        return table;
    }

    // ==================== JdbcTemplate 模式 ====================

    private OverviewData queryOverviewByJdbc(OutpatientWindowStatsRequest request) {
        log.info("使用 JdbcTemplate 查询概览数据");
        return queryOverviewMock(request);
    }

    private AgeAnalysis queryAgeAnalysisByJdbc(OutpatientWindowStatsRequest request) {
        return queryAgeAnalysisMock(request);
    }

    private TimeAnalysis queryTimeAnalysisByJdbc(OutpatientWindowStatsRequest request) {
        return queryTimeAnalysisMock(request);
    }

    private List<AnalysisItem> querySourceAnalysisByJdbc(OutpatientWindowStatsRequest request) {
        return querySourceAnalysisMock(request);
    }

    private WorkloadTable queryWorkloadTableByJdbc(OutpatientWindowStatsRequest request) {
        return queryWorkloadTableMock(request);
    }

    // ==================== MyBatis-Plus 模式 ====================

    private OverviewData queryOverviewByMybatisPlus(OutpatientWindowStatsRequest request) {
        return queryOverviewMock(request);
    }

    private AgeAnalysis queryAgeAnalysisByMybatisPlus(OutpatientWindowStatsRequest request) {
        return queryAgeAnalysisMock(request);
    }

    private TimeAnalysis queryTimeAnalysisByMybatisPlus(OutpatientWindowStatsRequest request) {
        return queryTimeAnalysisMock(request);
    }

    private List<AnalysisItem> querySourceAnalysisByMybatisPlus(OutpatientWindowStatsRequest request) {
        return querySourceAnalysisMock(request);
    }

    private WorkloadTable queryWorkloadTableByMybatisPlus(OutpatientWindowStatsRequest request) {
        return queryWorkloadTableMock(request);
    }

    // ==================== 工具方法 ====================

    private AnalysisItem newAnalysisItem(String name, int value) {
        AnalysisItem item = new AnalysisItem();
        item.setName(name);
        item.setValue(value);
        return item;
    }

}
