package com.reports.service.impl;

import com.reports.config.ReportDataConfig;
import com.reports.dto.common.PageResult;
import com.reports.dto.request.CashDischargeSettlementRequest;
import com.reports.dto.response.cash.discharge.settlement.*;
import com.reports.service.CashDischargeSettlementService;
import com.reports.util.SeqUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 出院结算报表服务实现
 */
@Slf4j
@Service
public class CashDischargeSettlementServiceImpl implements CashDischargeSettlementService {

    private final ReportDataConfig dataConfig;
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public CashDischargeSettlementServiceImpl(ReportDataConfig dataConfig, JdbcTemplate jdbcTemplate) {
        this.dataConfig = dataConfig;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public OverviewData queryOverview(CashDischargeSettlementRequest request) {
        log.info("查询出院结算概览数据，mode={}", dataConfig.getMode());
        if (dataConfig.isMock()) {
            return queryOverviewMock(request);
        } else if (dataConfig.isJdbc()) {
            return queryOverviewByJdbc(request);
        } else {
            return queryOverviewByMybatisPlus(request);
        }
    }

    @Override
    public ChartsData queryCharts(CashDischargeSettlementRequest request) {
        log.info("查询出院结算图表数据，mode={}", dataConfig.getMode());
        if (dataConfig.isMock()) {
            return queryChartsMock(request);
        } else if (dataConfig.isJdbc()) {
            return queryChartsByJdbc(request);
        } else {
            return queryChartsByMybatisPlus(request);
        }
    }

    @Override
    public PageResult<TableItem> queryTable(CashDischargeSettlementRequest request, Integer page, Integer pageSize) {
        log.info("查询出院结算表格数据，mode={}", dataConfig.getMode());
        if (dataConfig.isMock()) {
            return queryTableMock(request, page, pageSize);
        } else if (dataConfig.isJdbc()) {
            return queryTableByJdbc(request, page, pageSize);
        } else {
            return queryTableByMybatisPlus(request, page, pageSize);
        }
    }

    // ==================== Mock 模式 ====================

    private OverviewData queryOverviewMock(CashDischargeSettlementRequest request) {
        SeqUtil.next();
        OverviewData overview = new OverviewData();
        overview.setTotalDischargeCount(856);
        overview.setTotalDischargeCompare(72);
        overview.setDischargedCount(720);
        overview.setDischargedCompare(65);
        overview.setNotDischargedCount(136);
        overview.setNotDischargedCompare(7);
        overview.setSettlementAmount(1256800.50);
        overview.setSettlementAmountCompare(108);
        return overview;
    }

    private ChartsData queryChartsMock(CashDischargeSettlementRequest request) {
        SeqUtil.next();
        ChartsData charts = new ChartsData();

        List<ChartItem> channelAnalysis = new ArrayList<>();
        channelAnalysis.add(newChartItem("窗口", 28, 5));
        channelAnalysis.add(newChartItem("自助机", 72, 10));
        charts.setChannelAnalysis(channelAnalysis);

        List<ChartItem> patientTypeAnalysis = new ArrayList<>();
        patientTypeAnalysis.add(newChartItem("医保", 175, 15));
        patientTypeAnalysis.add(newChartItem("自费", 135, 12));
        charts.setPatientTypeAnalysis(patientTypeAnalysis);

        List<ChartItem> amountTypeAnalysis = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            amountTypeAnalysis.add(newChartItem("2024-01-0" + (i + 1), 80 + i, 5 + i));
        }
        charts.setAmountTypeAnalysis(amountTypeAnalysis);
        return charts;
    }

    private PageResult<TableItem> queryTableMock(CashDischargeSettlementRequest request, Integer page, Integer pageSize) {
        SeqUtil.next();
        List<TableItem> list = new ArrayList<>();
        for (int i = 0; i < pageSize; i++) {
            TableItem item = new TableItem();
            item.setDate("2024-01-" + String.format("%02d", i + 1));
            item.setTotalLast(30 + i);
            item.setTotalCurrent(35 + i);
            item.setTotalCompare(5);
            item.setDischargedLast(25 + i);
            item.setDischargedCurrent(30 + i);
            item.setDischargedCompare(5);
            item.setNotDischargedLast(5);
            item.setNotDischargedCurrent(5);
            item.setNotDischargedCompare(0);
            item.setAmountLast(50000.00 + i * 1000);
            item.setAmountCurrent(55000.00 + i * 1100);
            item.setAmountCompare(10);
            list.add(item);
        }
        return PageResult.of(list, 55L, page, pageSize);
    }

    // ==================== JdbcTemplate 模式 ====================

    private OverviewData queryOverviewByJdbc(CashDischargeSettlementRequest request) {
        log.info("使用 JdbcTemplate 查询概览数据");
        return queryOverviewMock(request);
    }

    private ChartsData queryChartsByJdbc(CashDischargeSettlementRequest request) {
        return queryChartsMock(request);
    }

    private PageResult<TableItem> queryTableByJdbc(CashDischargeSettlementRequest request, Integer page, Integer pageSize) {
        log.info("使用 JdbcTemplate 查询表格数据");
        return queryTableMock(request, page, pageSize);
    }

    // ==================== MyBatis-Plus 模式 ====================

    private OverviewData queryOverviewByMybatisPlus(CashDischargeSettlementRequest request) {
        return queryOverviewMock(request);
    }

    private ChartsData queryChartsByMybatisPlus(CashDischargeSettlementRequest request) {
        return queryChartsMock(request);
    }

    private PageResult<TableItem> queryTableByMybatisPlus(CashDischargeSettlementRequest request, Integer page, Integer pageSize) {
        return queryTableMock(request, page, pageSize);
    }

    // ==================== 工具方法 ====================

    private com.reports.dto.response.cash.discharge.settlement.ChartItem newChartItem(String name, int value, int compare) {
        com.reports.dto.response.cash.discharge.settlement.ChartItem item = new com.reports.dto.response.cash.discharge.settlement.ChartItem();
        item.setName(name);
        item.setValue(value);
        item.setCompare(compare);
        return item;
    }

}
