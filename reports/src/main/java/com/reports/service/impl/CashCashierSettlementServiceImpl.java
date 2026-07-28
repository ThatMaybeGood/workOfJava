package com.reports.service.impl;

import com.reports.config.ReportDataConfig;
import com.reports.dto.common.PageResult;
import com.reports.dto.request.CashCashierSettlementRequest;
import com.reports.dto.response.cash.cashier.settlement.*;
import com.reports.service.CashCashierSettlementService;
import com.reports.mapper.CashSettleMapper;
import com.reports.entity.CashSettleOvEntity;
import com.reports.entity.CashSettleDtlEntity;
import com.reports.entity.CashSettleChtEntity;
import com.reports.util.SeqUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.text.SimpleDateFormat;

/**
 * 收费员结账统计服务实现
 */
@Slf4j
@Service
public class CashCashierSettlementServiceImpl implements CashCashierSettlementService {

    private final ReportDataConfig dataConfig;
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    private CashSettleMapper cashSettleMapper;

    @Autowired
    public CashCashierSettlementServiceImpl(ReportDataConfig dataConfig, JdbcTemplate jdbcTemplate) {
        this.dataConfig = dataConfig;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public OverviewData queryOverview(CashCashierSettlementRequest request) {
        log.info("查询收费员结账概览数据，mode={}", dataConfig.getMode());
        if (dataConfig.isMock()) {
            return queryOverviewMock(request);
        } else if (dataConfig.isJdbc()) {
            return queryOverviewByJdbc(request);
        } else {
            return queryOverviewByMybatisPlus(request);
        }
    }

    @Override
    public PageResult<TableItem> queryTable(CashCashierSettlementRequest request, Integer page, Integer pageSize) {
        log.info("查询收费员结账表格数据，mode={}", dataConfig.getMode());
        if (dataConfig.isMock()) {
            return queryTableMock(request, page, pageSize);
        } else if (dataConfig.isJdbc()) {
            return queryTableByJdbc(request, page, pageSize);
        } else {
            return queryTableByMybatisPlus(request, page, pageSize);
        }
    }

    @Override
    public ChartData queryChart(CashCashierSettlementRequest request) {
        log.info("查询收费员结账图表数据，mode={}", dataConfig.getMode());
        if (dataConfig.isMock()) {
            return queryChartMock(request);
        } else if (dataConfig.isJdbc()) {
            return queryChartByJdbc(request);
        } else {
            return queryChartByMybatisPlus(request);
        }
    }

    // ==================== Mock 模式 ====================

    private OverviewData queryOverviewMock(CashCashierSettlementRequest request) {
        SeqUtil.next();
        OverviewData overview = new OverviewData();
        overview.setAppointmentRegister(1256);
        overview.setAppointmentRegisterCompare(128);
        overview.setAppointmentFetch(1120);
        overview.setAppointmentFetchCompare(95);
        overview.setTodayRegister(856);
        overview.setTodayRegisterCompare(72);
        overview.setRefund(68);
        overview.setRefundCompare(12);
        overview.setOutpatientCharge(3256);
        overview.setOutpatientChargeCompare(268);
        overview.setOutpatientRefund(45);
        overview.setOutpatientRefundCompare(8);
        overview.setPrepayment(1200);
        overview.setPrepaymentCompare(105);
        overview.setHospitalRefund(32);
        overview.setHospitalRefundCompare(5);
        overview.setDischargeSettlement(88);
        overview.setDischargeSettlementCompare(10);
        return overview;
    }

    private PageResult<TableItem> queryTableMock(CashCashierSettlementRequest request, Integer page, Integer pageSize) {
        SeqUtil.next();
        List<TableItem> list = new ArrayList<>();
        for (int i = 0; i < pageSize; i++) {
            TableItem item = new TableItem();
            item.setDate("2024-01-" + String.format("%02d", i + 1));
            Map<String, Object> columns = new HashMap<>();
            columns.put("cashierA", 5000 + i * 100);
            columns.put("cashierB", 6000 + i * 120);
            columns.put("total", 11000 + i * 220);
            item.setColumns(columns);
            list.add(item);
        }
        return PageResult.of(list, 55L, page, pageSize);
    }

    private ChartData queryChartMock(CashCashierSettlementRequest request) {
        SeqUtil.next();
        ChartData chart = new ChartData();
        chart.setTitle("收费员结账统计图表");
        chart.setSubTitle("支付方式分布");
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        chart.setDateRange(dateFormat.format(request.getStartDate()) + " ~ " + dateFormat.format(request.getEndDate()));
        List<String> categories = new ArrayList<>();
        categories.add("现金");
        categories.add("刷卡");
        chart.setCategories(categories);
        List<Integer> data = new ArrayList<>();
        data.add(30);
        data.add(70);
        chart.setData(data);
        return chart;
    }

    // ==================== JdbcTemplate 模式 ====================

    private OverviewData queryOverviewByJdbc(CashCashierSettlementRequest request) {
        log.info("使用 JdbcTemplate 查询概览数据");
        return queryOverviewMock(request);
    }

    private PageResult<TableItem> queryTableByJdbc(CashCashierSettlementRequest request, Integer page, Integer pageSize) {
        log.info("使用 JdbcTemplate 查询表格数据");
        return queryTableMock(request, page, pageSize);
    }

    private ChartData queryChartByJdbc(CashCashierSettlementRequest request) {
        log.info("使用 JdbcTemplate 查询图表数据");
        return queryChartMock(request);
    }

    // ==================== MyBatis-Plus 模式 ====================

    private OverviewData queryOverviewByMybatisPlus(CashCashierSettlementRequest request) {
        try {
            CashSettleOvEntity entity = cashSettleMapper.queryOverview(request.getStartDate(), request.getEndDate());
            return buildOverviewData(entity);
        } catch (Exception e) {
            log.warn("查询收费员结账概览失败", e);
            return new OverviewData();
        }
    }

    private PageResult<TableItem> queryTableByMybatisPlus(CashCashierSettlementRequest request, Integer page, Integer pageSize) {
        try {
            List<CashSettleDtlEntity> rows = cashSettleMapper.queryDetail(request.getStartDate(), request.getEndDate(), null);
            // 按日期分组
            Map<String, Map<String, Double>> dateCashierMap = new LinkedHashMap<>();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            for (CashSettleDtlEntity row : rows) {
                String dateStr = sdf.format(row.getItemDate());
                String cashier = row.getCashierName();
                double value = row.getItemValue() != null ? row.getItemValue().doubleValue() : 0.0;
                dateCashierMap.computeIfAbsent(dateStr, k -> new HashMap<>());
                dateCashierMap.get(dateStr).merge(cashier, value, Double::sum);
            }
            List<TableItem> allItems = new ArrayList<>();
            for (Map.Entry<String, Map<String, Double>> entry : dateCashierMap.entrySet()) {
                TableItem item = new TableItem();
                item.setDate(entry.getKey());
                Map<String, Object> columns = new HashMap<>(entry.getValue());
                double total = entry.getValue().values().stream().mapToDouble(Double::doubleValue).sum();
                columns.put("total", total);
                item.setColumns(columns);
                allItems.add(item);
            }
            int totalItems = allItems.size();
            int start = (page - 1) * pageSize;
            int end = Math.min(start + pageSize, totalItems);
            List<TableItem> pageList = start < totalItems ? allItems.subList(start, end) : new ArrayList<>();
            return PageResult.of(pageList, (long) totalItems, page, pageSize);
        } catch (Exception e) {
            log.warn("查询收费员结账表格失败", e);
            return PageResult.of(new ArrayList<>(), 0L, page, pageSize);
        }
    }

    private ChartData queryChartByMybatisPlus(CashCashierSettlementRequest request) {
        try {
            List<CashSettleChtEntity> entities = cashSettleMapper.queryChart(request.getStartDate(), request.getEndDate());
            if (entities == null || entities.isEmpty()) {
                return new ChartData();
            }
            ChartData chart = new ChartData();
            CashSettleChtEntity first = entities.get(0);
            chart.setTitle(first.getChartTitle());
            chart.setSubTitle(first.getChartSubtitle());
            chart.setDateRange(first.getDateRange());
            List<String> categories = new ArrayList<>();
            List<Integer> data = new ArrayList<>();
            for (CashSettleChtEntity entity : entities) {
                categories.add(entity.getCategory());
                data.add(entity.getDataValue());
            }
            chart.setCategories(categories);
            chart.setData(data);
            return chart;
        } catch (Exception e) {
            log.warn("查询收费员结账图表失败", e);
            return new ChartData();
        }
    }

    // ==================== 实体转换 ====================

    private OverviewData buildOverviewData(CashSettleOvEntity entity) {
        if (entity == null) {
            return new OverviewData();
        }
        OverviewData overview = new OverviewData();
        overview.setAppointmentRegister(entity.getAppointmentRegister());
        overview.setAppointmentRegisterCompare(entity.getAppointmentRegisterCompare());
        overview.setAppointmentFetch(entity.getAppointmentFetch());
        overview.setAppointmentFetchCompare(entity.getAppointmentFetchCompare());
        overview.setTodayRegister(entity.getTodayRegister());
        overview.setTodayRegisterCompare(entity.getTodayRegisterCompare());
        overview.setRefund(entity.getRefund());
        overview.setRefundCompare(entity.getRefundCompare());
        overview.setOutpatientCharge(entity.getOutpatientCharge());
        overview.setOutpatientChargeCompare(entity.getOutpatientChargeCompare());
        overview.setOutpatientRefund(entity.getOutpatientRefund());
        overview.setOutpatientRefundCompare(entity.getOutpatientRefundCompare());
        overview.setPrepayment(entity.getPrepayment());
        overview.setPrepaymentCompare(entity.getPrepaymentCompare());
        overview.setHospitalRefund(entity.getHospitalRefund());
        overview.setHospitalRefundCompare(entity.getHospitalRefundCompare());
        overview.setDischargeSettlement(entity.getDischargeSettlement());
        overview.setDischargeSettlementCompare(entity.getDischargeSettlementCompare());
        return overview;
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
