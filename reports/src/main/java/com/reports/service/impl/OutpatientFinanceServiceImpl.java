package com.reports.service.impl;

import com.reports.config.ReportDataConfig;
import com.reports.dto.request.OutpatientFinanceRequest;
import com.reports.dto.response.cash.outpatient.finance.BarItem;
import com.reports.dto.response.cash.outpatient.finance.DetailListItem;
import com.reports.dto.response.cash.outpatient.finance.IndicatorData;
import com.reports.dto.response.cash.outpatient.finance.PieItem;
import com.reports.entity.cash.OutpFinanceClinicMaster;
import com.reports.entity.cash.OutpFinanceRcptAcct;
import com.reports.mapper.OutpatientFinanceMapper;
import com.reports.service.OutpatientFinanceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;

/**
 * 门诊财务报表服务实现
 * <p>读取 ETL 抽取的明细存储，指标计算（人次去重、业务类型分界、同比自关联）均在 Java 完成。</p>
 * <p>核心规则：汇总 = 进项 − 退项，四个指标全部满足。</p>
 */
@Slf4j
@Service
public class OutpatientFinanceServiceImpl implements OutpatientFinanceService {

    /** bt8 业务类型金额分界日期：此前挂号费取自 clinic，此后按收据 bill_class 区分 */
    private static final LocalDate BIZ_SPLIT_DATE = LocalDate.of(2025, 2, 8);

    private final ReportDataConfig dataConfig;
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    private OutpatientFinanceMapper financeMapper;

    @Autowired
    public OutpatientFinanceServiceImpl(ReportDataConfig dataConfig, JdbcTemplate jdbcTemplate) {
        this.dataConfig = dataConfig;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public IndicatorData queryIndicator(OutpatientFinanceRequest request, List<DetailListItem> detailList) {
        if (dataConfig.isMock()) {
            return buildIndicatorFromDetail(queryDetailListMock(request));
        } else {
            return buildIndicatorFromDetail(detailList);
        }
    }

    @Override
    public IndicatorData queryIndicator(OutpatientFinanceRequest request) {
        if (dataConfig.isMock()) {
            return queryIndicatorMock(request);
        } else {
            return queryIndicatorByMybatisPlus(request);
        }
    }

    @Override
    public List<DetailListItem> queryDetailList(OutpatientFinanceRequest request) {
        if (dataConfig.isMock()) {
            return queryDetailListMock(request);
        } else {
            return queryDetailListByMybatisPlus(request);
        }
    }

    @Override
    public Map<String, List<BarItem>> queryBarList(OutpatientFinanceRequest request, List<DetailListItem> detailList) {
        return buildBarFromDetail(detailList);
    }

    @Override
    public Map<String, List<BarItem>> queryBarList(OutpatientFinanceRequest request) {
        return buildBarFromDetail(queryDetailList(request));
    }

    @Override
    public Map<String, List<PieItem>> queryPieList(OutpatientFinanceRequest request) {
        return queryPieList(request, null);
    }

    @Override
    public Map<String, List<PieItem>> queryPieList(OutpatientFinanceRequest request, String pieTypes) {
        Set<String> types = new HashSet<>();
        if (pieTypes != null && !pieTypes.trim().isEmpty()) {
            for (String t : pieTypes.split(",")) {
                String v = t.trim();
                if (!v.isEmpty()) {
                    types.add(v);
                }
            }
        } else {
            for (int i = 1; i <= 10; i++) {
                types.add(String.valueOf(i));
            }
        }
        if (dataConfig.isMock()) {
            return buildPieListMock(types);
        } else {
            return queryPieListByMybatisPlus(request, types);
        }
    }

    // ==================== Mock 模式 ====================

    private IndicatorData queryIndicatorMock(OutpatientFinanceRequest request) {
        return buildIndicatorFromDetail(queryDetailListMock(request));
    }

    private List<DetailListItem> queryDetailListMock(OutpatientFinanceRequest request) {
        List<String> dates = buildMockDates(request);
        List<DetailListItem> list = new ArrayList<>();
        for (int i = 0; i < dates.size(); i++) {
            list.add(buildMockDetailItem(dates.get(i), i));
        }
        return list;
    }

    // ==================== MyBatis-Plus 模式 ====================

    private IndicatorData queryIndicatorByMybatisPlus(OutpatientFinanceRequest request) {
        return buildIndicatorFromDetail(queryDetailListByMybatisPlus(request));
    }

    private List<PieItem> buildBizTypePie(OutpatientFinanceRequest request) {
        Integer type = request.getStatisticType();
        Integer tt = request.getTimeType();
        Date start = normalizeDate(type, tt, request.getStartDate());
        Date end = normalizeDate(type, tt, request.getEndDate());
        Map<String, Double> curr = computeBizType(type, tt, start, end);
        Date pStart = offsetDate(start, -12);
        Date pEnd = offsetDate(end, -12);
        Map<String, Double> prev = computeBizType(type, tt, pStart, pEnd);
        List<PieItem> items = new ArrayList<>();
        for (Map.Entry<String, Double> e : curr.entrySet()) {
            PieItem item = new PieItem();
            item.setName(e.getKey());
            item.setCurrValue(round(e.getValue()));
            item.setPrevValue(round(prev.getOrDefault(e.getKey(), 0.0)));
            items.add(item);
        }
        return items;
    }

    private List<DetailListItem> queryDetailListByMybatisPlus(OutpatientFinanceRequest request) {
        try {
            Integer type = request.getStatisticType();
            Integer tt = request.getTimeType();
            Date start = normalizeDate(type, tt, request.getStartDate());
            Date end = normalizeDate(type, tt, request.getEndDate());

            List<DetailListItem> curr = loadPeriods(type, tt, start, end);

            // 同比：同期（区间向前推 12 个月）
            Date pStart = offsetDate(start, -12);
            Date pEnd = offsetDate(end, -12);
            Map<String, DetailListItem> prevByPeriod = new HashMap<>();
            for (DetailListItem item : loadPeriods(type, tt, pStart, pEnd)) {
                prevByPeriod.put(item.getDateTime(), item);
            }
            for (DetailListItem item : curr) {
                String period = item.getDateTime();
                if (period == null) continue;
                Date prevDate = offsetDate(parseDate(period), -12);
                DetailListItem prev = prevByPeriod.get(formatPeriod(prevDate, tt));
                double prevOv = prev != null ? prev.getCurrentDateOutpatientVolume() : 0.0;
                double prevNc = prev != null ? prev.getCurrentDateNumberCharges() : 0.0;
                double prevNr = prev != null ? prev.getCurrentDateNumberReceipt() : 0.0;
                double prevAm = prev != null ? prev.getCurrentDateAmount() : 0.0;
                item.setLastYearOutpatientVolume(prevOv);
                item.setLastYearNumberCharges(prevNc);
                item.setLastYearNumberReceipt(prevNr);
                item.setLastYearAmount(prevAm);
            }
            return curr;
        } catch (Exception e) {
            log.warn("查询门诊财务明细失败", e);
            return new ArrayList<>();
        }
    }

    /**
     * 加载各周期指标。
     * 人次（netCares）对 T1 拆成 T2 − T3，T2/T3 直接用对应过滤行计算。
     */
    private List<DetailListItem> loadPeriods(Integer statisticType, Integer timeType,
                                             Date startDate, Date endDate) {
        Map<String, Double> clinic = toPeriodMap(
                financeMapper.queryClinicCounts(statisticType, startDate, endDate, timeType), "cnt");

        Map<String, Double> amount = new HashMap<>();
        Map<String, Double> receipt = new HashMap<>();
        for (Map<String, Object> row : financeMapper.queryAcctAmounts(statisticType, startDate, endDate, timeType)) {
            Object p = row.get("period");
            if (p == null) {
                log.warn("queryAcctAmounts 结果缺少 period 字段，row={}", row);
                continue;
            }
            String period = String.valueOf(p);
            amount.put(period, toMapDouble(row.get("amount")));
            receipt.put(period, toMapDouble(row.get("receipt")));
        }

        // 人次：T1 = T2 − T3（净量），人次合并由 SQL 窗口函数完成（同患者+同日+同前缀+票号连续）
        Map<String, Double> netCares;
        if (statisticType != null && statisticType == 1) {
            Map<String, Double> careIn = toPeriodMap(
                    financeMapper.queryVisitCounts(2, startDate, endDate, timeType), "cnt");
            Map<String, Double> careOut = toPeriodMap(
                    financeMapper.queryVisitCounts(3, startDate, endDate, timeType), "cnt");
            netCares = new HashMap<>();
            for (Map.Entry<String, Double> e : careIn.entrySet()) {
                netCares.merge(e.getKey(), e.getValue(), Double::sum);
            }
            for (Map.Entry<String, Double> e : careOut.entrySet()) {
                netCares.merge(e.getKey(), -e.getValue(), Double::sum);
            }
        } else {
            netCares = toPeriodMap(
                    financeMapper.queryVisitCounts(statisticType, startDate, endDate, timeType), "cnt");
        }

        Set<String> periods = new TreeSet<>();
        periods.addAll(clinic.keySet());
        periods.addAll(amount.keySet());
        periods.addAll(receipt.keySet());
        periods.addAll(netCares.keySet());

        List<DetailListItem> list = new ArrayList<>();
        for (String period : periods) {
            DetailListItem item = new DetailListItem();
            item.setDateTime(period);
            item.setCurrentDateOutpatientVolume(clinic.getOrDefault(period, 0.0));
            item.setCurrentDateNumberCharges(netCares.getOrDefault(period, 0.0));
            item.setCurrentDateNumberReceipt(receipt.getOrDefault(period, 0.0));
            item.setCurrentDateAmount(amount.getOrDefault(period, 0.0));
            list.add(item);
        }
        return list;
    }

    private Map<String, List<PieItem>> queryPieListByMybatisPlus(OutpatientFinanceRequest request, Set<String> types) {
        Map<String, List<PieItem>> map = new LinkedHashMap<>();
        try {
            Integer type = request.getStatisticType();
            Integer tt = request.getTimeType();
            Date start = normalizeDate(type, tt, request.getStartDate());
            Date end = normalizeDate(type, tt, request.getEndDate());
            Date pStart = offsetDate(start, -12);
            Date pEnd = offsetDate(end, -12);
            log.info("[门诊财务饼图][{}] 请求类型 bt={}, 当期 [{}, {}], 同比 [{}, {}]",
                    request.getPieTab(), types, start, end, pStart, pEnd);

            // bt1 订单来源：门诊挂号表按 SOURCE_TYPE 净人次（未退号+1/退号-1）
            if (types.contains("1")) {
                log.info("[门诊财务饼图] bt1: 订单来源(门诊量, 挂号表按 SOURCE_TYPE 净人次) → queryClinicCountBySource");
                map.put("1", buildPie(financeMapper.queryClinicCountBySource(type, start, end, tt),
                        financeMapper.queryClinicCountBySource(type, pStart, pEnd, tt), this::mapSourceType));
            }
            // bt3/bt4 共用同一查询（订单渠道/收据张数渠道：收据表按操作员净张数）
            if (types.contains("3") || types.contains("4")) {
                log.info("[门诊财务饼图] bt3/4: 订单渠道/收据张数渠道(收据表按操作员净张数) → queryRcptCountByOperator");
                List<PieItem> pieOperator = buildPie(
                        financeMapper.queryRcptCountByOperator(type, start, end, tt),
                        financeMapper.queryRcptCountByOperator(type, pStart, pEnd, tt), null);
                if (types.contains("3")) map.put("3", pieOperator);
                if (types.contains("4")) map.put("4", pieOperator);
            }
            if (types.contains("2")) {
                log.info("[门诊财务饼图] bt2: 取号渠道 → queryQueueCountByOperator");
                map.put("2", buildPie(financeMapper.queryQueueCountByOperator(type, start, end, tt),
                        financeMapper.queryQueueCountByOperator(type, pStart, pEnd, tt), null));
            }
            if (types.contains("5")) {
                log.info("[门诊财务饼图] bt5: 人次支付(按支付方式人次) → queryPaymentCountByMoneyType");
                map.put("5", buildPie(financeMapper.queryPaymentCountByMoneyType(type, start, end, tt),
                        financeMapper.queryPaymentCountByMoneyType(type, pStart, pEnd, tt), null));
            }
            if (types.contains("6")) {
                log.info("[门诊财务饼图] bt6: 渠道金额(按操作员金额) → queryRcptSumByOperator");
                map.put("6", buildPie(financeMapper.queryRcptSumByOperator(type, start, end, tt),
                        financeMapper.queryRcptSumByOperator(type, pStart, pEnd, tt), null));
            }
            if (types.contains("7")) {
                log.info("[门诊财务饼图] bt7: 支付方式金额(按支付方式金额) → queryPaymentSumByMoneyType");
                map.put("7", buildPie(financeMapper.queryPaymentSumByMoneyType(type, start, end, tt),
                        financeMapper.queryPaymentSumByMoneyType(type, pStart, pEnd, tt), null));
            }
            if (types.contains("8")) {
                log.info("[门诊财务饼图] bt8: 业务类型金额(当日挂号/门诊缴费) → computeBizType(分界: 2025-02-08)");
                map.put("8", buildBizTypePie(request));
            }
            if (types.contains("9")) {
                log.info("[门诊财务饼图] bt9: 应收/实收 → queryPaymentSumByCategory");
                map.put("9", buildPie(financeMapper.queryPaymentSumByCategory(type, start, end, tt),
                        financeMapper.queryPaymentSumByCategory(type, pStart, pEnd, tt), null));
            }
            if (types.contains("10")) {
                log.info("[门诊财务饼图] bt10: 应收金额(占位, 无数据)");
                map.put("10", new ArrayList<>());
            }
        } catch (Exception e) {
            log.warn("查询门诊财务饼图失败", e);
            for (String bt : types) {
                map.putIfAbsent(bt, new ArrayList<>());
            }
        }
        return map;
    }

    private List<PieItem> buildPie(List<Map<String, Object>> currRows, List<Map<String, Object>> prevRows,
                                   Function<String, String> nameMapper) {
        Map<String, Double> curr = aggregatePie(currRows, nameMapper);
        Map<String, Double> prev = aggregatePie(prevRows, nameMapper);
        List<PieItem> items = new ArrayList<>();
        for (Map.Entry<String, Double> e : curr.entrySet()) {
            PieItem item = new PieItem();
            item.setName(e.getKey());
            item.setCurrValue(round(e.getValue()));
            item.setPrevValue(round(prev.getOrDefault(e.getKey(), 0.0)));
            items.add(item);
        }
        return items;
    }

    private Map<String, Double> aggregatePie(List<Map<String, Object>> rows,
                                             Function<String, String> nameMapper) {
        Map<String, Double> agg = new LinkedHashMap<>();
        for (Map<String, Object> row : rows) {
            String name = String.valueOf(row.get("name"));
            if (nameMapper != null) {
                name = nameMapper.apply(name);
            }
            Object value = row.get("cnt") != null ? row.get("cnt") : row.get("amount");
            agg.merge(name, toMapDouble(value), Double::sum);
        }
        return agg;
    }

    /**
     * bt8 业务类型金额：分界前挂号费取自 clinic（REGIST_FEE+CLINIC_FEE），
     * 分界后按收据 bill_class='1' 判定当日挂号，其余为门诊缴费。
     */
    private Map<String, Double> computeBizType(Integer statisticType, Integer timeType,
                                               Date startDate, Date endDate) {
        Map<String, Double> m = new LinkedHashMap<>();
        m.put("当日挂号", 0.0);
        m.put("门诊缴费", 0.0);
        Date split = Date.from(BIZ_SPLIT_DATE.atStartOfDay(ZoneId.systemDefault()).toInstant());
        for (OutpFinanceClinicMaster c : financeMapper.queryClinicRows(statisticType, startDate, endDate, timeType)) {
            if (c.getVisitDate() != null && c.getVisitDate().before(split)) {
                m.merge("当日挂号", toDouble(c.getRegistFee()) + toDouble(c.getClinicFee()), Double::sum);
            }
        }
        for (OutpFinanceRcptAcct r : financeMapper.queryRcptRows(statisticType, startDate, endDate, timeType)) {
            boolean regist = r.getVisitDate() != null
                    && !r.getVisitDate().before(split)
                    && "1".equals(r.getBillClass());
            m.merge(regist ? "当日挂号" : "门诊缴费", toDouble(r.getTotalCharges()), Double::sum);
        }
        return m;
    }

    /**
     * SOURCE_TYPE 值字典：目前原样展示，待确认编码含义后可加映射
     */
    private String mapSourceType(String sourceType) {
        if (sourceType == null || sourceType.trim().isEmpty()) {
            return "未知来源";
        }
        return sourceType;
    }

    // ==================== 日期归一化工具 ====================

    /**
     * 前端已将月/天模式统一转为日期范围传入，直接解析 yyyy-MM-dd。
     */
    private Date normalizeDate(Integer statisticType, Integer timeType, String input) {
        if (input == null) {
            return null;
        }
        return java.sql.Date.valueOf(input);
    }

    private Date offsetDate(Date date, int months) {
        if (date == null) {
            return null;
        }
        LocalDate ld = new java.util.Date(date.getTime()).toInstant()
                .atZone(ZoneId.systemDefault()).toLocalDate();
        return java.sql.Date.valueOf(ld.plusMonths(months));
    }

    private Date parseDate(String period) {
        if (period == null) {
            return null;
        }
        // 去掉时间部分，只保留日期
        String dateStr = period.trim();
        if (dateStr.length() > 10) {
            dateStr = dateStr.substring(0, 10);
        }
        try {
            // 月模式 "yyyy-MM" 补上 "-01" 转 Date
            if (dateStr.length() == 7) {
                return java.sql.Date.valueOf(dateStr + "-01");
            }
            return java.sql.Date.valueOf(dateStr);
        } catch (IllegalArgumentException e) {
            log.warn("无法解析的周期字符串: [{}]", period);
            return null;
        }
    }

    // ==================== 组装与工具方法 ====================

    private List<String> buildMockDates(OutpatientFinanceRequest request) {
        List<String> dates = new ArrayList<>();
        if (request.getTimeType() != null && request.getTimeType() == 2) {
            LocalDate start = LocalDate.parse(request.getStartDate());
            LocalDate end = LocalDate.parse(request.getEndDate());
            for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
                dates.add(d.toString());
            }
        } else {
            YearMonth start = YearMonth.parse(request.getStartDate());
            YearMonth end = YearMonth.parse(request.getEndDate());
            for (YearMonth m = start; !m.isAfter(end); m = m.plusMonths(1)) {
                dates.add(m.toString());
            }
        }
        return dates;
    }

    private DetailListItem buildMockDetailItem(String date, int idx) {
        double ov = 320 + (idx * 7) % 60;
        double nc = 210 + (idx * 5) % 40;
        double nr = 260 + (idx * 6) % 50;
        double am = 95000 + (idx * 1300) % 20000;
        DetailListItem item = new DetailListItem();
        item.setDateTime(date);
        item.setLastYearOutpatientVolume(round(ov * 0.9));
        item.setCurrentDateOutpatientVolume(round(ov));
        item.setLastYearNumberCharges(round(nc * 0.92));
        item.setCurrentDateNumberCharges(round(nc));
        item.setLastYearNumberReceipt(round(nr * 0.9));
        item.setCurrentDateNumberReceipt(round(nr));
        item.setLastYearAmount(round(am * 0.88));
        item.setCurrentDateAmount(round(am));
        return item;
    }

    private IndicatorData buildIndicatorFromDetail(List<DetailListItem> detail) {
        double ov = 0, nc = 0, nr = 0, am = 0;
        double ovY = 0, ncY = 0, nrY = 0, amY = 0;
        for (DetailListItem item : detail) {
            ov += item.getCurrentDateOutpatientVolume();
            nc += item.getCurrentDateNumberCharges();
            nr += item.getCurrentDateNumberReceipt();
            am += item.getCurrentDateAmount();
            ovY += item.getLastYearOutpatientVolume();
            ncY += item.getLastYearNumberCharges();
            nrY += item.getLastYearNumberReceipt();
            amY += item.getLastYearAmount();
        }
        IndicatorData data = new IndicatorData();
        data.setOutpatientVolume(round(ov));
        data.setNumberCharges(round(nc));
        data.setNumberReceipt(round(nr));
        data.setAmount(round(am));
        data.setOutpatientVolumeYoy(calcYoy(ov, ovY));
        data.setNumberChargesYoy(calcYoy(nc, ncY));
        data.setNumberReceiptYoy(calcYoy(nr, nrY));
        data.setAmountYoy(calcYoy(am, amY));
        return data;
    }

    private Map<String, List<BarItem>> buildBarFromDetail(List<DetailListItem> detail) {
        Map<String, List<BarItem>> map = new LinkedHashMap<>();
        for (int bt = 1; bt <= 4; bt++) {
            List<BarItem> bars = new ArrayList<>();
            for (DetailListItem item : detail) {
                BarItem bar = new BarItem();
                bar.setDateTime(item.getDateTime());
                bar.setLastYearNumber(pickLastYear(item, bt));
                bar.setCurrentDateNumber(pickCurrent(item, bt));
                bars.add(bar);
            }
            map.put(String.valueOf(bt), bars);
        }
        return map;
    }

    private Double pickCurrent(DetailListItem item, int bt) {
        switch (bt) {
            case 1: return item.getCurrentDateOutpatientVolume();
            case 2: return item.getCurrentDateNumberCharges();
            case 3: return item.getCurrentDateNumberReceipt();
            default: return item.getCurrentDateAmount();
        }
    }

    private Double pickLastYear(DetailListItem item, int bt) {
        switch (bt) {
            case 1: return item.getLastYearOutpatientVolume();
            case 2: return item.getLastYearNumberCharges();
            case 3: return item.getLastYearNumberReceipt();
            default: return item.getLastYearAmount();
        }
    }

    private Map<String, List<PieItem>> buildPieListMock(Set<String> types) {
        String[][] categories = {
                {"窗口", "自助机", "掌上医院", "电话预约", "网络预约"},
                {"线下取号", "线上取号"},
                {"微信公众号", "APP", "官网", "线下"},
                {"窗口", "自助机", "移动支付", "医保"},
                {"现金", "微信", "支付宝", "银行卡", "医保"},
                {"窗口", "自助机", "移动支付", "医保"},
                {"现金", "微信", "支付宝", "银行卡", "医保"},
                {"挂号", "检查", "检验", "药品", "治疗"},
                {"应收账款", "实收金额"},
                {"挂号", "检查", "检验", "药品", "治疗"}
        };
        Map<String, List<PieItem>> map = new LinkedHashMap<>();
        for (int bt = 1; bt <= 10; bt++) {
            if (!types.contains(String.valueOf(bt))) continue;
            List<PieItem> items = new ArrayList<>();
            String[] cats = categories[bt - 1];
            double base = 5000 - bt * 300;
            for (int i = 0; i < cats.length; i++) {
                PieItem item = new PieItem();
                item.setName(cats[i]);
                item.setCurrValue(round(base + i * 800 + ((bt * 13) % 7) * 100));
                item.setPrevValue(round((base + i * 800) * 0.9));
                items.add(item);
            }
            map.put(String.valueOf(bt), items);
        }
        return map;
    }

    private Map<String, Double> toPeriodMap(List<Map<String, Object>> rows, String key) {
        Map<String, Double> map = new LinkedHashMap<>();
        for (Map<String, Object> row : rows) {
            Object p = row.get("period");
            if (p == null) {
                log.warn("查询结果缺少 period 字段，row={}", row);
                continue;
            }
            map.put(String.valueOf(p), toMapDouble(row.get(key)));
        }
        return map;
    }

    private double toMapDouble(Object value) {
        return value instanceof Number ? ((Number) value).doubleValue() : 0.0;
    }

    private String formatPeriod(Date date, Integer timeType) {
        if (date == null) {
            return "";
        }
        LocalDate ld = new java.util.Date(date.getTime()).toInstant()
                .atZone(ZoneId.systemDefault()).toLocalDate();
        return (timeType != null && timeType == 2) ? ld.toString() : YearMonth.from(ld).toString();
    }


    private String calcYoy(double current, double lastYear) {
        if (lastYear == 0) {
            return "--";
        }
        return String.format("%.2f%%", (current - lastYear) / lastYear * 100);
    }

    private double toDouble(BigDecimal value) {
        return value != null ? value.doubleValue() : 0.0;
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

}
