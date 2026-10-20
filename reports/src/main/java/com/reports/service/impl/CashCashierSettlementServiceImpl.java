package com.reports.service.impl;

import com.reports.config.ReportDataConfig;
import com.reports.dto.common.PageResult;
import com.reports.dto.request.CashCashierSettlementRequest;
import com.reports.dto.response.cash.cashier.settlement.*;
import com.reports.service.CashCashierSettlementService;
import com.reports.mapper.CashSettleMapper;
import com.reports.entity.CashSettleDtlEntity;
import com.reports.entity.CashSettleChtEntity;
import com.reports.util.SeqUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;
import java.text.SimpleDateFormat;

/**
 * 收费员结账统计服务实现
 */
@Slf4j
@Service
public class CashCashierSettlementServiceImpl implements CashCashierSettlementService {

    /** 概览/图表多源表查询并行执行(Spring代理Mapper线程安全,各调用独立连接); 池max-active=20足够4路并发 */
    private static final ExecutorService QUERY_EXECUTOR = Executors.newFixedThreadPool(4, r -> {
        Thread t = new Thread(r, "cashier-settle-query");
        t.setDaemon(true);
        return t;
    });

    /** 单查询失败降级空结果, 不拖垮整页 */
    private <T> CompletableFuture<T> supplyQuery(Supplier<T> query, T fallback) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return query.get();
            } catch (Exception e) {
                log.warn("收费员结账并行子查询失败,降级空结果", e);
                return fallback;
            }
        }, QUERY_EXECUTOR);
    }

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
            java.sql.Date[] range = currentAndLastRange(request);
            CompletableFuture<RegisterOverviewData> regF = supplyQuery(
                    () -> cashSettleMapper.queryRegisterOverview(range[0], range[1], range[2], range[3]), new RegisterOverviewData());
            CompletableFuture<RcptOverviewData> rcptF = supplyQuery(
                    () -> cashSettleMapper.queryRcptOverview(range[0], range[1], range[2], range[3]), new RcptOverviewData());
            CompletableFuture<PrepayOverviewData> prepayF = supplyQuery(
                    () -> cashSettleMapper.queryPrepayOverview(range[0], range[1], range[2], range[3]), new PrepayOverviewData());
            CompletableFuture<DischargeOverviewData> dischF = supplyQuery(
                    () -> cashSettleMapper.queryDischargeOverview(range[0], range[1], range[2], range[3]), new DischargeOverviewData());
            CompletableFuture.allOf(regF, rcptF, prepayF, dischF).join();
            RegisterOverviewData reg = regF.join();
            OverviewData overview = new OverviewData();
            overview.setAppointmentRegister(reg.getAppointmentRegister());
            overview.setAppointmentRegisterCompare(yoy(reg.getAppointmentRegister(), reg.getAppointmentRegisterLast()));
            overview.setAppointmentFetch(reg.getAppointmentFetch());
            overview.setAppointmentFetchCompare(yoy(reg.getAppointmentFetch(), reg.getAppointmentFetchLast()));
            overview.setTodayRegister(reg.getTodayRegister());
            overview.setTodayRegisterCompare(yoy(reg.getTodayRegister(), reg.getTodayRegisterLast()));
            overview.setRefund(reg.getRefund());
            overview.setRefundCompare(yoy(reg.getRefund(), reg.getRefundLast()));
            // 收费类2指标: 源表TR_OUTP_FIN_RCPT_ACCT(口径同门诊财务进项/退项)
            RcptOverviewData rcpt = rcptF.join();
            overview.setOutpatientCharge(rcpt.getOutpatientCharge());
            overview.setOutpatientChargeCompare(yoy(rcpt.getOutpatientCharge(), rcpt.getOutpatientChargeLast()));
            overview.setOutpatientRefund(rcpt.getOutpatientRefund());
            overview.setOutpatientRefundCompare(yoy(rcpt.getOutpatientRefund(), rcpt.getOutpatientRefundLast()));
            // 预交金类2指标: 源表tr_inpat_prepay_rcpt(口径同住院预交金统计)
            PrepayOverviewData prepay = prepayF.join();
            overview.setPrepayment(prepay.getPrepayment());
            overview.setPrepaymentCompare(yoy(prepay.getPrepayment(), prepay.getPrepaymentLast()));
            overview.setHospitalRefund(prepay.getHospitalRefund());
            overview.setHospitalRefundCompare(yoy(prepay.getHospitalRefund(), prepay.getHospitalRefundLast()));
            // 出院结算量: 源表tr_disch_settle_visit(口径同出院结算报表已结算人次)
            DischargeOverviewData disch = dischF.join();
            overview.setDischargeSettlement(disch.getDischargeSettlement());
            overview.setDischargeSettlementCompare(yoy(disch.getDischargeSettlement(), disch.getDischargeSettlementLast()));
            return overview;
        } catch (Exception e) {
            log.warn("查询收费员结账概览失败", e);
            return new OverviewData();
        }
    }

    /** 本期区间 + 去年同期区间(月份减12)，与出院结算口径一致 */
    private static java.sql.Date[] currentAndLastRange(CashCashierSettlementRequest request) {
        Date start = request.getStartDate();
        Date end = request.getEndDate();
        return new java.sql.Date[]{sqlDay(start), sqlDay(end), sqlDay(addMonths(start, -12)), sqlDay(addMonths(end, -12))};
    }

    /** java.util.Date→java.sql.Date：绑定按DATE类型发送, 避免TIMESTAMP绑定触发的列隐式转换致索引失效 */
    private static java.sql.Date sqlDay(Date d) {
        return d == null ? null : new java.sql.Date(d.getTime());
    }

    private static Date addMonths(Date date, int months) {
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(java.util.Calendar.MONTH, months);
        return calendar.getTime();
    }

    /** 同比%：同期为0或无数据返回null(前端显示—)，否则(本期-同期)/同期*100取整 */
    private static Integer yoy(Integer cur, Integer last) {
        if (last == null || last == 0) {
            return null;
        }
        int c = cur == null ? 0 : cur;
        return Math.round((c - last) / (float) last * 100);
    }

    private PageResult<TableItem> queryTableByMybatisPlus(CashCashierSettlementRequest request, Integer page, Integer pageSize) {
        try {
            boolean month = "month".equalsIgnoreCase(request.getDimension());
            String tab = request.getTab() == null ? "" : request.getTab();
            // 工作量报表：行=时间+操作员(姓名)，列=5个工作量指标
            if ("workload".equalsIgnoreCase(tab)) {
                return buildWorkloadReportTable(request, page, pageSize, month);
            }
            // 按收费员统计：行=时间，列=全部操作员(姓名)，单元格=该天该操作员9个业务量指标之和
            if ("cashier".equalsIgnoreCase(tab)) {
                return buildCashierTotalTable(request, page, pageSize, month);
            }
            // 按来源方式统计：行=时间，列=9个业务量指标(与来源图表指标一致)，值=所选操作员的笔数
            String dimValue = request.getChannel() != null && !request.getChannel().isEmpty()
                    ? request.getChannel() : request.getCashier();
            List<Map<String, Object>> pivotRows = queryOperatorPivotCached(
                    sqlDay(request.getStartDate()), sqlDay(request.getEndDate()), month);
            List<DimOption> dims = dimsFromRows(pivotRows);
            // 选定操作员时Java侧过滤(dimValue非空时NULL工号行本就不计入,与原SQL过滤等价)
            List<Map<String, Object>> rows = pivotRows;
            if (dimValue != null && !dimValue.isEmpty()) {
                rows = new ArrayList<>();
                for (Map<String, Object> row : pivotRows) {
                    if (dimValue.equals(String.valueOf(row.get("dim")))) {
                        rows.add(row);
                    }
                }
            }
            SimpleDateFormat sdf = new SimpleDateFormat(month ? "yyyy-MM" : "yyyy-MM-dd");
            Map<String, Map<String, Object>> periodMap = new LinkedHashMap<>();
            for (Map<String, Object> row : rows) {
                String period = row.get("period") == null ? "" : sdf.format((Date) row.get("period"));
                Map<String, Object> cols = periodMap.computeIfAbsent(period, k -> new LinkedHashMap<>());
                cols.merge(String.valueOf(row.get("metric")), toDouble(row.get("cnt")), (a, b) -> toDouble(a) + toDouble(b));
            }
            List<TableItem> allItems = new ArrayList<>();
            for (Map.Entry<String, Map<String, Object>> entry : periodMap.entrySet()) {
                Map<String, Object> cols = new LinkedHashMap<>();
                for (String metric : TABLE_METRICS) {
                    cols.put(metric, entry.getValue().getOrDefault(metric, 0.0));
                }
                double total = cols.values().stream().mapToDouble(v -> (Double) v).sum();
                cols.put("汇总", total);
                TableItem item = new TableItem();
                item.setDate(entry.getKey());
                item.setColumns(cols);
                allItems.add(item);
            }
            return pageResult(allItems, page, pageSize, dims);
        } catch (Exception e) {
            log.warn("查询收费员结账表格失败", e);
            return PageResult.of(new ArrayList<>(), 0L, page, pageSize);
        }
    }

    /** 按收费员统计：行=时间，列=全部操作员(staff_dict姓名)，单元格=9个业务量指标笔数之和 */
    private CashierTableResult buildCashierTotalTable(CashCashierSettlementRequest request,
                                                      Integer page, Integer pageSize, boolean month) {
        List<Map<String, Object>> rows = queryOperatorPivotCached(
                sqlDay(request.getStartDate()), sqlDay(request.getEndDate()), month);
        List<DimOption> dims = dimsFromRows(rows);
        Map<String, String> nameByCode = new HashMap<>();
        for (DimOption d : dims) {
            nameByCode.put(d.getValue(), d.getLabel());
        }
        SimpleDateFormat sdf = new SimpleDateFormat(month ? "yyyy-MM" : "yyyy-MM-dd");
        // period -> 操作员姓名 -> 9指标之和
        Map<String, Map<String, Double>> periodMap = new LinkedHashMap<>();
        for (Map<String, Object> row : rows) {
            String period = row.get("period") == null ? "" : sdf.format((Date) row.get("period"));
            Object dim = row.get("dim");
            // 无工号记录归入固定列，避免笔数丢失导致与来源页签汇总对不上
            String name = dim == null ? "（无工号）"
                    : nameByCode.getOrDefault(String.valueOf(dim), String.valueOf(dim));
            double value = toDouble(row.get("cnt"));
            periodMap.computeIfAbsent(period, k -> new LinkedHashMap<>());
            periodMap.get(period).merge(name, value, Double::sum);
        }
        List<TableItem> allItems = new ArrayList<>();
        for (Map.Entry<String, Map<String, Double>> entry : periodMap.entrySet()) {
            Map<String, Object> cols = new LinkedHashMap<>(entry.getValue());
            double total = entry.getValue().values().stream().mapToDouble(Double::doubleValue).sum();
            cols.put("汇总", total);
            TableItem item = new TableItem();
            item.setDate(entry.getKey());
            item.setColumns(cols);
            allItems.add(item);
        }
        return pageResult(allItems, page, pageSize, dims);
    }

    /** 工作量报表：行=时间+操作员(姓名)，列=5个工作量指标 */
    private CashierTableResult buildWorkloadReportTable(CashCashierSettlementRequest request,
                                                        Integer page, Integer pageSize, boolean month) {
        List<Map<String, Object>> rows = cashSettleMapper.queryWorkloadPivot(
                sqlDay(request.getStartDate()), sqlDay(request.getEndDate()), month, null, null);
        List<DimOption> dims = dimsFromRows(rows);
        Map<String, String> nameByCode = new HashMap<>();
        for (DimOption d : dims) {
            nameByCode.put(d.getValue(), d.getLabel());
        }
        SimpleDateFormat sdf = new SimpleDateFormat(month ? "yyyy-MM" : "yyyy-MM-dd");
        List<TableItem> allItems = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            Map<String, Object> cols = workloadMetricColumns(row);
            double total = cols.values().stream().mapToDouble(v -> (Double) v).sum();
            cols.put("汇总", total);
            cols.put("操作员", nameByCode.getOrDefault(String.valueOf(row.get("dim")), String.valueOf(row.get("dim"))));
            TableItem item = new TableItem();
            item.setDate(row.get("period") == null ? "" : sdf.format((Date) row.get("period")));
            item.setColumns(cols);
            allItems.add(item);
        }
        return pageResult(allItems, page, pageSize, dims);
    }

    /** 按收费员/按来源两页签表格的9个业务量指标列（与来源图表指标一致） */
    private static final List<String> TABLE_METRICS = java.util.Arrays.asList(
            "预约挂号量", "预约取号量", "当日挂号量", "退号量", "门诊收费量",
            "门诊退费量", "收预交金量", "退院量", "出院结算量");

    /** 5个工作量指标由7个原始分量计算（口径见mapper注释; 回诊/医联暂无字段计0, 当日挂号≡有效挂号数） */
    /** 5个工作量指标由原始分量计算（口径: 当日挂号=ind2且return=1未退; 有效挂号=ind2全部含退; 退号按退号日归期） */
    private Map<String, Object> workloadMetricColumns(Map<String, Object> row) {
        double netCnt = toDouble(row.get("net_cnt"));
        double allCnt = toDouble(row.get("all_cnt"));
        double tuihao = toDouble(row.get("tuihao"));
        double yyOk = toDouble(row.get("yy_ok"));
        double yyCancel = toDouble(row.get("yy_cancel"));
        double jf = toDouble(row.get("jf"));
        double tf = toDouble(row.get("tf"));
        Map<String, Object> cols = new LinkedHashMap<>();
        cols.put("当日挂号", netCnt);
        cols.put("有效挂号数", allCnt);
        cols.put("预约挂号", yyOk - yyCancel);
        cols.put("门诊收费", netCnt + jf);
        cols.put("门诊退费", tuihao + tf);
        return cols;
    }

    private double toDouble(Object v) {
        return v == null ? 0.0 : ((Number) v).doubleValue();
    }

    /** 按收费员透视缓存：结果与所选收费员无关(收费员下拉只驱动图表), 10min TTL避免切人/翻页重扫全量 */
    private static final long PIVOT_TTL = 10 * 60 * 1000L;
    private static final int PIVOT_CACHE_MAX = 20;
    private final Map<String, long[]> pivotCacheAt = new ConcurrentHashMap<>();
    private final Map<String, List<Map<String, Object>>> pivotCache = new ConcurrentHashMap<>();

    private List<Map<String, Object>> queryOperatorPivotCached(java.sql.Date start, java.sql.Date end, boolean month) {
        String key = start.getTime() + "-" + end.getTime() + "-" + month;
        long now = System.currentTimeMillis();
        long[] at = pivotCacheAt.get(key);
        if (at != null && now - at[0] <= PIVOT_TTL) {
            return pivotCache.get(key);
        }
        List<Map<String, Object>> rows = cashSettleMapper.queryMetricPivot(start, end, month, "operator", null);
        if (pivotCache.size() >= PIVOT_CACHE_MAX) {
            pivotCache.clear();
            pivotCacheAt.clear();
        }
        pivotCacheAt.put(key, new long[]{now});
        pivotCache.put(key, rows);
        return rows;
    }

    /** 维度下拉从透视行派生：distinct工号并集(9指标覆盖同4张表), staff_dict批量取姓名(查不到回退工号) */
    private List<DimOption> dimsFromRows(List<Map<String, Object>> rows) {
        Set<String> codes = new LinkedHashSet<>();
        for (Map<String, Object> row : rows) {
            Object dim = row.get("dim");
            if (dim != null) {
                codes.add(String.valueOf(dim));
            }
        }
        List<String> sorted = new ArrayList<>(codes);
        Collections.sort(sorted);
        Map<String, String> nameByCode = queryStaffNameMap(sorted);
        List<DimOption> dims = new ArrayList<>();
        for (String code : sorted) {
            dims.add(new DimOption(code, nameByCode.getOrDefault(code, code)));
        }
        return dims;
    }

    private Map<String, String> queryStaffNameMap(List<String> codes) {
        Map<String, String> map = new HashMap<>();
        if (codes.isEmpty()) {
            return map;
        }
        for (Map<String, Object> row : cashSettleMapper.queryStaffNames(codes)) {
            Object value = row.get("value");
            Object label = row.get("label");
            if (value != null && label != null) {
                map.put(String.valueOf(value), String.valueOf(label));
            }
        }
        return map;
    }

    private CashierTableResult pageResult(List<TableItem> allItems, Integer page, Integer pageSize, List<DimOption> dims) {
        int totalItems = allItems.size();
        int start = (page - 1) * pageSize;
        int end = Math.min(start + pageSize, totalItems);
        List<TableItem> pageList = start < totalItems ? allItems.subList(start, end) : new ArrayList<>();
        return new CashierTableResult(pageList, (long) totalItems, page, pageSize, dims);
    }

    private ChartData queryChartByMybatisPlus(CashCashierSettlementRequest request) {
        try {
            // 按收费员统计：图表按单个收费员维度，9类业务量直取源表
            if ("cashier".equalsIgnoreCase(request.getTab())) {
                return buildCashierWorkloadChart(request);
            }
            // 按来源方式统计：图表=选定日/月+选定指标按工号拆
            if ("source".equalsIgnoreCase(request.getTab())) {
                return buildSourceChart(request);
            }
            return new ChartData();
        } catch (Exception e) {
            log.warn("查询收费员结账图表失败", e);
            return new ChartData();
        }
    }

    /**
     * 收费员业务工作量分析：按单个收费员维度出9类业务量，
     * 挂号类4项直取源表（口径同概览），其余5项口径待给先占坑0
     */
    private ChartData buildCashierWorkloadChart(CashCashierSettlementRequest request) {
        ChartData chart = new ChartData();
        chart.setTitle("收费员业务工作量分析");
        chart.setSubTitle(request.getCashier());
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        chart.setDateRange(dateFormat.format(request.getStartDate()) + "~" + dateFormat.format(request.getEndDate()));
        chart.setCategories(new ArrayList<>(java.util.Arrays.asList(
                "预约挂号", "预约取号", "当日挂号", "退号", "门诊收费", "门诊退费", "收预交金", "退院", "出院结算")));

        List<Integer> data = new ArrayList<>(java.util.Arrays.asList(0, 0, 0, 0, 0, 0, 0, 0, 0));
        String cashier = request.getCashier();
        if (cashier != null && !cashier.isEmpty()) {
            java.sql.Date start = sqlDay(request.getStartDate());
            java.sql.Date end = sqlDay(request.getEndDate());
            CompletableFuture<RegisterOverviewData> regF = supplyQuery(
                    () -> cashSettleMapper.queryCashierWorkload(start, end, cashier), new RegisterOverviewData());
            CompletableFuture<RcptOverviewData> rcptF = supplyQuery(
                    () -> cashSettleMapper.queryCashierCharge(start, end, cashier), new RcptOverviewData());
            CompletableFuture<PrepayOverviewData> prepayF = supplyQuery(
                    () -> cashSettleMapper.queryCashierPrepay(start, end, cashier), new PrepayOverviewData());
            CompletableFuture<DischargeOverviewData> dischF = supplyQuery(
                    () -> cashSettleMapper.queryCashierDischarge(start, end, cashier), new DischargeOverviewData());
            CompletableFuture.allOf(regF, rcptF, prepayF, dischF).join();
            RegisterOverviewData reg = regF.join();
            data.set(0, reg.getAppointmentRegister() != null ? reg.getAppointmentRegister() : 0);
            data.set(1, reg.getAppointmentFetch() != null ? reg.getAppointmentFetch() : 0);
            data.set(2, reg.getTodayRegister() != null ? reg.getTodayRegister() : 0);
            data.set(3, reg.getRefund() != null ? reg.getRefund() : 0);
            RcptOverviewData rcpt = rcptF.join();
            data.set(4, rcpt.getOutpatientCharge() != null ? rcpt.getOutpatientCharge() : 0);
            data.set(5, rcpt.getOutpatientRefund() != null ? rcpt.getOutpatientRefund() : 0);
            PrepayOverviewData prepay = prepayF.join();
            data.set(6, prepay.getPrepayment() != null ? prepay.getPrepayment() : 0);
            data.set(7, prepay.getHospitalRefund() != null ? prepay.getHospitalRefund() : 0);
            DischargeOverviewData disch = dischF.join();
            data.set(8, disch.getDischargeSettlement() != null ? disch.getDischargeSettlement() : 0);
        }
        chart.setData(data);
        return chart;
    }

    /**
     * 来源方式工作量分析：选定日/月+选定指标，按工号出柱
     */
    private ChartData buildSourceChart(CashCashierSettlementRequest request) {
        ChartData chart = new ChartData();
        chart.setTitle("来源方式工作量分析");
        chart.setSubTitle(request.getChartMetric());
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        chart.setDateRange(request.getChartDate() != null ? dateFormat.format(request.getChartDate()) : "");
        List<String> categories = new ArrayList<>();
        List<Integer> data = new ArrayList<>();
        if (request.getChartDate() != null && request.getChartMetric() != null && !request.getChartMetric().isEmpty()) {
            java.sql.Date[] dayRange = chartDayRange(request);
            List<CashSettleDtlEntity> rows = cashSettleMapper.querySourceChart(
                    dayRange[0], dayRange[1], request.getChartMetric());
            if (rows != null) {
                for (CashSettleDtlEntity row : rows) {
                    categories.add(row.getCashierName());
                    data.add(row.getItemValue() != null ? row.getItemValue().intValue() : 0);
                }
            }
        }
        chart.setCategories(categories);
        chart.setData(data);
        return chart;
    }

    /** 来源方式图表选定日/月的[起,止)区间：按天=当天~次日，按月=月初~次月 */
    private static java.sql.Date[] chartDayRange(CashCashierSettlementRequest request) {
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        calendar.setTime(request.getChartDate());
        Date start = calendar.getTime();
        if ("month".equalsIgnoreCase(request.getDimension())) {
            calendar.set(java.util.Calendar.DAY_OF_MONTH, 1);
            start = calendar.getTime();
        }
        calendar.add(java.util.Calendar.MONTH, "month".equalsIgnoreCase(request.getDimension()) ? 1 : 0);
        calendar.add(java.util.Calendar.DAY_OF_MONTH, "month".equalsIgnoreCase(request.getDimension()) ? 0 : 1);
        return new java.sql.Date[]{sqlDay(start), sqlDay(calendar.getTime())};
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
