package com.reports.service.impl;

import com.reports.config.ReportDataConfig;
import com.reports.dto.common.PageResult;
import com.reports.dto.request.CashDischargeSettlementRequest;
import com.reports.dto.response.cash.discharge.settlement.*;
import com.reports.service.CashDischargeSettlementService;
import com.reports.mapper.DischSettleMapper;
import com.reports.util.SeqUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * 出院结算报表服务实现
 * 数据源: tr_disch_settle_visit(出院记录)/tr_settle_master(结算主表)/tr_settle_payments(支付方式)
 * 本期/同期(去年同范围)一次扫描聚合, 同比查询时算
 */
@Slf4j
@Service
public class CashDischargeSettlementServiceImpl implements CashDischargeSettlementService {

    private final ReportDataConfig dataConfig;
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    private DischSettleMapper dischSettleMapper;

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
    public List<ChartItem> queryChartDetail(CashDischargeSettlementRequest request) {
        log.info("查询出院结算图表钻取明细，drillType={}，itemName={}", request.getDrillType(), request.getItemName());
        if (dataConfig.isMock()) {
            return queryChartDetailMock(request);
        }
        try {
            Date start = request.getStartDate();
            Date end = request.getEndDate();
            // 钻取只看本期所选范围
            if ("CHANNEL".equals(request.getDrillType())) {
                return dischSettleMapper.queryChannelDetail(start, end, request.getItemName());
            }
            return dischSettleMapper.queryPayDetail(start, end, request.getItemName());
        } catch (Exception e) {
            log.warn("查询出院结算钻取明细失败", e);
            return new ArrayList<>();
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
        channelAnalysis.add(newChartItem("窗口", 128, 5));
        channelAnalysis.add(newChartItem("自助机", 96, 10));
        charts.setChannelAnalysis(channelAnalysis);

        List<ChartItem> patientTypeAnalysis = new ArrayList<>();
        patientTypeAnalysis.add(newChartItem("职工医保", 120, 6));
        patientTypeAnalysis.add(newChartItem("异地职工医保", 45, 3));
        patientTypeAnalysis.add(newChartItem("居民医保", 60, -2));
        patientTypeAnalysis.add(newChartItem("自费", 65, -5));
        charts.setPatientTypeAnalysis(patientTypeAnalysis);

        List<ChartItem> amountTypeAnalysis = new ArrayList<>();
        amountTypeAnalysis.add(newChartItem("微信", 420, 5));
        amountTypeAnalysis.add(newChartItem("支付宝", 260, 8));
        amountTypeAnalysis.add(newChartItem("银行卡", 310, -3));
        amountTypeAnalysis.add(newChartItem("现金", 180, -6));
        charts.setAmountTypeAnalysis(amountTypeAnalysis);
        return charts;
    }

    /** 钻取明细mock：渠道→费别人次，支付方式→收/退金额 */
    private List<ChartItem> queryChartDetailMock(CashDischargeSettlementRequest request) {
        SeqUtil.next();
        List<ChartItem> list = new ArrayList<>();
        String[] feeTypes = {"职工医保", "异地职工医保", "居民医保", "自费"};
        if ("CHANNEL".equals(request.getDrillType())) {
            for (String feeType : feeTypes) {
                list.add(newChartItem(feeType, 10 + feeType.length() * 7, 0));
            }
        } else {
            list.add(newChartItem("收", 80, 0));
            list.add(newChartItem("退", 20, 0));
        }
        return list;
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
        try {
            Date[] range = currentAndLastRange(request);
            return dischSettleMapper.queryOverview(range[0], range[1], range[2], range[3]);
        } catch (Exception e) {
            log.warn("查询出院结算概览失败", e);
            return new OverviewData();
        }
    }

    private ChartsData queryChartsByMybatisPlus(CashDischargeSettlementRequest request) {
        try {
            Date[] range = currentAndLastRange(request);
            ChartsData charts = new ChartsData();
            charts.setChannelAnalysis(dischSettleMapper.queryChannelChart(range[0], range[1], range[2], range[3]));
            charts.setPatientTypeAnalysis(dischSettleMapper.queryFeeTypeChart(range[0], range[1], range[2], range[3]));
            charts.setAmountTypeAnalysis(dischSettleMapper.queryPayTypeChart(range[0], range[1], range[2], range[3]));
            return charts;
        } catch (Exception e) {
            log.warn("查询出院结算图表失败", e);
            return new ChartsData();
        }
    }

    private PageResult<TableItem> queryTableByMybatisPlus(CashDischargeSettlementRequest request, Integer page, Integer pageSize) {
        try {
            Date[] range = currentAndLastRange(request);
            List<TableItem> allItems = dischSettleMapper.queryDetail(range[0], range[1], range[2], range[3],
                    isMonthDimension(request));
            int total = allItems.size();
            int start = (page - 1) * pageSize;
            int end = Math.min(start + pageSize, total);
            List<TableItem> pageList = start < total ? allItems.subList(start, end) : new ArrayList<>();
            return PageResult.of(pageList, (long) total, page, pageSize);
        } catch (Exception e) {
            log.warn("查询出院结算表格失败", e);
            return PageResult.of(new ArrayList<>(), 0L, page, pageSize);
        }
    }

    /** 按月统计：dimension=month 时表格/概览都按月聚合 */
    private static boolean isMonthDimension(CashDischargeSettlementRequest request) {
        return "month".equals(request.getDimension());
    }

    /** 本期区间 + 去年同期区间(月份减12, 与明细轴平移口径一致) */
    private static Date[] currentAndLastRange(CashDischargeSettlementRequest request) {
        Date start = request.getStartDate();
        Date end = request.getEndDate();
        return new Date[]{start, end, addMonths(start, -12), addMonths(end, -12)};
    }

    private static Date addMonths(Date date, int months) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.MONTH, months);
        return calendar.getTime();
    }

    // ==================== 工具方法 ====================

    private ChartItem newChartItem(String name, int value, int compare) {
        ChartItem item = new ChartItem();
        item.setName(name);
        item.setValue(value);
        item.setCompare(compare);
        return item;
    }

}