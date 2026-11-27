package com.reports.service.impl;

import com.reports.config.ReportDataConfig;
import com.reports.dto.request.OutpatientForecastRequest;
import com.reports.dto.response.outpatient.forecast.ForecastDayRow;
import com.reports.dto.response.outpatient.forecast.*;import com.reports.mapper.OutpatientForecastMapper;
import com.reports.service.OutpatientForecastService;
import com.reports.util.SeqUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 预测门诊量报表服务实现
 * 公式: 预测日量 = 去年同期日实际门诊量 × 增长率
 * 增长率 = 近30天门诊量合计 ÷ 去年同期30天门诊量合计(去年同期无数据按1)
 */
@Slf4j
@Service
public class OutpatientForecastServiceImpl implements OutpatientForecastService {

    /** 30天明细天数 */
    private static final int DAILY_DAYS = 30;

    private final ReportDataConfig dataConfig;
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    private OutpatientForecastMapper forecastMapper;

    @Autowired
    public OutpatientForecastServiceImpl(ReportDataConfig dataConfig, JdbcTemplate jdbcTemplate) {
        this.dataConfig = dataConfig;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public OverviewData queryOverview(OutpatientForecastRequest request) {
        log.info("查询预测门诊量概览数据，mode={}", dataConfig.getMode());
        if (dataConfig.isMock()) {
            return queryOverviewMock(request);
        }
        return queryOverviewCompute(request);
    }

    @Override
    public MonthForecast queryMonthForecast(OutpatientForecastRequest request) {
        log.info("查询未来30天预测，mode={}", dataConfig.getMode());
        if (dataConfig.isMock()) {
            return queryMonthForecastMock(request);
        }
        return queryMonthForecastCompute(request);
    }

    @Override
    public YearForecast queryYearForecast(OutpatientForecastRequest request) {
        log.info("查询未来12个月预测，mode={}", dataConfig.getMode());
        if (dataConfig.isMock()) {
            return queryYearForecastMock(request);
        }
        return queryYearForecastCompute(request);
    }

    // ==================== 实时计算 ====================

    /**
     * 分段缓存: 过去段(门诊量按日聚合, 仅夜间抽数后变化)缓存至次日0点自动过期;
     * 日预测整体10min刷新; 概览/月图/年图共用
     */
    private static final long FC_TTL = 10 * 60 * 1000L;
    private final Map<String, DailyContext> dailyCache = new ConcurrentHashMap<>();
    private final Map<String, List<MonthPoint>> yearCache = new ConcurrentHashMap<>();
    private final Map<String, PastEntry> pastVolCache = new ConcurrentHashMap<>();

    /** 带过期时间的过去段聚合缓存 */
    private static class PastEntry {
        private final Map<String, Double> vol;
        private final long expireAt;

        private PastEntry(Map<String, Double> vol, long expireAt) {
            this.vol = vol;
            this.expireAt = expireAt;
        }
    }

    /** 次日0点毫秒值: 过去段缓存过期锚(抽数按日覆盖, 跨天必刷新) */
    private static long nextMidnightMillis() {
        return LocalDate.now().plusDays(1).atStartOfDay(java.time.ZoneId.systemDefault())
                .toInstant().toEpochMilli();
    }

    private String fcKey(OutpatientForecastRequest request) {
        String dc = request.getDeptCode() == null ? "" : request.getDeptCode().trim();
        String dn = request.getDeptName() == null ? "" : request.getDeptName().trim();
        return dc + "|" + dn;
    }

    private List<DailyForecast> calcDailyForecastCached(OutpatientForecastRequest request) {
        return dailyContextCached(request).daily;
    }

    private DailyContext dailyContextCached(OutpatientForecastRequest request) {
        String key = fcKey(request);
        long expireAt = System.currentTimeMillis() + FC_TTL;
        DailyContext hit = dailyCache.get(key);
        if (hit != null && hit.expireAt > System.currentTimeMillis()) {
            return hit;
        }
        // single-flight: 同科室并发请求只算一次, 其余等结果复用(避免冷缓存时双倍重查询)
        synchronized (key.intern()) {
            hit = dailyCache.get(key);
            if (hit != null && hit.expireAt > System.currentTimeMillis()) {
                return hit;
            }
            DailyContext ctx = calcDailyForecast(request);
            dailyCache.put(key, new DailyContext(ctx.daily, ctx.dayVol, expireAt));
            return dailyCache.get(key);
        }
    }

    private List<MonthPoint> calcYearForecastCached(OutpatientForecastRequest request) {
        String key = fcKey(request);
        // single-flight: 年预测复用日预测结果, 与日预测同锁串行, 并发只算一遍
        synchronized (key.intern()) {
            DailyContext ctx = dailyCache.get(key);
            List<MonthPoint> hit = yearCache.get(key);
            if (ctx != null && ctx.expireAt > System.currentTimeMillis() && hit != null) {
                return hit;
            }
            DailyContext fresh = dailyContextCached(request);
            List<MonthPoint> rows = calcYearForecast(fresh.dayVol);
            yearCache.put(key, rows);
            return rows;
        }
    }

    /** 过去段门诊量按日聚合(前年1-1~昨天): 重查询(674万行级)当天只跑一次, 缓存至次日0点 */
    private Map<String, Double> pastVolCached(OutpatientForecastRequest request) {
        String key = fcKey(request);
        long now = System.currentTimeMillis();
        PastEntry hit = pastVolCache.get(key);
        if (hit != null && hit.expireAt > now) {
            return hit.vol;
        }
        synchronized (("past|" + key).intern()) {
            now = System.currentTimeMillis();
            hit = pastVolCache.get(key);
            if (hit != null && hit.expireAt > now) {
                return hit.vol;
            }
            LocalDate today = LocalDate.now();
            List<ForecastDayRow> rows = forecastMapper.queryDailyVolumeRange(
                    sqlDay(toDate(LocalDate.of(today.getYear() - 2, 1, 1))), sqlDay(toDate(today.minusDays(1))),
                    request.getDeptCode(), request.getDeptName());
            Map<String, Double> vol = new HashMap<>();
            for (ForecastDayRow r : nvlList(rows)) {
                if (r.getDd() != null) {
                    vol.put(r.getDd(), r.getNum());
                }
            }
            PastEntry entry = new PastEntry(vol, nextMidnightMillis());
            pastVolCache.put(key, entry);
            return vol;
        }
    }

    private OverviewData queryOverviewCompute(OutpatientForecastRequest request) {
        try {
            List<DailyForecast> daily = calcDailyForecastCached(request);
            List<MonthPoint> monthValues = calcYearForecastCached(request);
            OverviewData overview = new OverviewData();
            overview.setTomorrow(daily.isEmpty() ? 0 : daily.get(0).value);
            overview.setNextWeek(sumRange(daily, 0, 7));
            overview.setNextMonth(sumRange(daily, 0, DAILY_DAYS));
            overview.setNextYear(monthValues.stream().mapToInt(p -> p.value).sum());
            return overview;
        } catch (Exception e) {
            log.warn("查询预测门诊量概览失败", e);
            return new OverviewData();
        }
    }

    private MonthForecast queryMonthForecastCompute(OutpatientForecastRequest request) {
        try {
            List<DailyForecast> daily = calcDailyForecastCached(request);
            MonthForecast forecast = new MonthForecast();
            List<String> dates = new ArrayList<>();
            List<Integer> data = new ArrayList<>();
            for (DailyForecast row : daily) {
                dates.add(row.date.toString());
                data.add(row.value);
            }
            forecast.setDates(dates);
            forecast.setData(data);
            // 诊断日志: 打印最终输出的30个预测值, 部署验证用
            log.info("未来30天预测值: {}", data);
            return forecast;
        } catch (Exception e) {
            log.warn("查询未来30天预测失败", e);
            MonthForecast empty = new MonthForecast();
            empty.setDates(new ArrayList<>());
            empty.setData(new ArrayList<>());
            return empty;
        }
    }

    private YearForecast queryYearForecastCompute(OutpatientForecastRequest request) {
        try {
            List<MonthPoint> monthValues = calcYearForecastCached(request);
            YearForecast forecast = new YearForecast();
            List<String> months = new ArrayList<>();
            List<Integer> data = new ArrayList<>();
            for (MonthPoint p : monthValues) {
                months.add(p.yearMonth.toString());
                data.add(p.value);
            }
            forecast.setMonths(months);
            forecast.setData(data);
            return forecast;
        } catch (Exception e) {
            log.warn("查询未来12个月预测失败", e);
            YearForecast empty = new YearForecast();
            empty.setMonths(new ArrayList<>());
            empty.setData(new ArrayList<>());
            return empty;
        }
    }

    /**
     * 未来30天每日预测: 预测值 = 去年同期日实际门诊量 × 增长率
     * 增长率 = 近30天合计 ÷ 去年同期30天合计; 过去段按日聚合缓存至次日0点
     */
    private DailyContext calcDailyForecast(OutpatientForecastRequest request) {
        LocalDate today = LocalDate.now();
        Map<String, Double> dayVol = pastVolCached(request);
        double growth = calcGrowth(dayVol, today);

        List<DailyForecast> result = new ArrayList<>();
        for (int i = 1; i <= DAILY_DAYS; i++) {
            LocalDate day = today.plusDays(i);
            double base = nvl(dayVol.get(day.minusYears(1).toString()));
            result.add(new DailyForecast(day, (int) Math.round(base * growth)));
        }
        // expireAt由外层dailyContextCached包装时赋值
        return new DailyContext(result, dayVol, 0L);
    }

    /** 增长率 = 近30天门诊量合计 ÷ 去年同期30天合计; 去年同期无数据(分母≤0)按1 */
    private static double calcGrowth(Map<String, Double> dayVol, LocalDate today) {
        double recent = sumInRange(dayVol, today.minusDays(DAILY_DAYS), today.minusDays(1));
        double lastYear = sumInRange(dayVol,
                today.minusDays(DAILY_DAYS).minusYears(1), today.minusDays(1).minusYears(1));
        return lastYear > 0 ? recent / lastYear : 1d;
    }

    /** 当前月起12个月预测: 每月 = 去年同期月实际门诊量 × 增长率(与日预测同口径) */
    private List<MonthPoint> calcYearForecast(Map<String, Double> dayVol) {
        LocalDate today = LocalDate.now();
        double growth = calcGrowth(dayVol, today);
        // 按日聚合Map归并为 yyyy-MM 月合计
        Map<String, Double> monthVol = new HashMap<>();
        for (Map.Entry<String, Double> e : dayVol.entrySet()) {
            if (e.getKey() != null && e.getKey().length() >= 7) {
                monthVol.merge(e.getKey().substring(0, 7), e.getValue() == null ? 0 : e.getValue(), Double::sum);
            }
        }
        List<MonthPoint> values = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            java.time.YearMonth ym = java.time.YearMonth.from(today).plusMonths(i);
            double base = monthVol.getOrDefault(ym.minusYears(1).toString(), 0d);
            values.add(new MonthPoint(ym, (int) Math.round(base * growth)));
        }
        return values;
    }

    private int sumRange(List<DailyForecast> daily, int from, int to) {
        return daily.subList(from, Math.min(to, daily.size())).stream().mapToInt(d -> d.value).sum();
    }

    /** 区间合计 */
    private static double sumInRange(Map<String, Double> dayMap, LocalDate start, LocalDate end) {
        double sum = 0;
        for (Map.Entry<String, Double> e : dayMap.entrySet()) {
            String dd = e.getKey();
            if (dd.compareTo(start.toString()) >= 0 && dd.compareTo(end.toString()) <= 0) {
                sum += e.getValue() == null ? 0 : e.getValue();
            }
        }
        return sum;
    }

    private double nvl(Double value) {
        return value != null ? value : 0d;
    }

    private static <T> List<T> nvlList(List<T> list) {
        return list == null ? Collections.emptyList() : list;
    }

    private Date toDate(LocalDate day) {
        return java.sql.Date.valueOf(day);
    }

    private static class DailyForecast {
        private final LocalDate date;
        private final int value;

        private DailyForecast(LocalDate date, int value) {
            this.date = date;
            this.value = value;
        }
    }

    /** 日预测结果/门诊量按日聚合(过去段)/过期时间: 年预测复用同一份聚合, 不重复查库 */
    private static class DailyContext {
        private final List<DailyForecast> daily;
        private final Map<String, Double> dayVol;
        private final long expireAt;

        private DailyContext(List<DailyForecast> daily, Map<String, Double> dayVol, long expireAt) {
            this.daily = daily;
            this.dayVol = dayVol;
            this.expireAt = expireAt;
        }
    }

    /** 月预测点:某年某月及其预测量 */
    private static class MonthPoint {
        private final java.time.YearMonth yearMonth;
        private final int value;

        private MonthPoint(java.time.YearMonth yearMonth, int value) {
            this.yearMonth = yearMonth;
            this.value = value;
        }
    }

    // ==================== Mock 模式 ====================

    private OverviewData queryOverviewMock(OutpatientForecastRequest request) {
        SeqUtil.next();
        List<Integer> daily = mockDailyValues();
        List<Integer> months = mockYearValues();
        OverviewData overview = new OverviewData();
        overview.setTomorrow(daily.get(0));
        overview.setNextWeek(daily.subList(0, 7).stream().mapToInt(Integer::intValue).sum());
        overview.setNextMonth(daily.stream().mapToInt(Integer::intValue).sum());
        overview.setNextYear(months.stream().mapToInt(Integer::intValue).sum());
        return overview;
    }

    private MonthForecast queryMonthForecastMock(OutpatientForecastRequest request) {
        SeqUtil.next();
        MonthForecast forecast = new MonthForecast();
        List<String> dates = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (int i = 1; i <= DAILY_DAYS; i++) {
            dates.add(today.plusDays(i).toString());
        }
        forecast.setDates(dates);
        forecast.setData(mockDailyValues());
        return forecast;
    }

    private YearForecast queryYearForecastMock(OutpatientForecastRequest request) {
        SeqUtil.next();
        YearForecast forecast = new YearForecast();
        List<String> months = new ArrayList<>();
        int year = LocalDate.now().getYear();
        for (int i = 1; i <= 12; i++) {
            months.add(year + "-" + String.format("%02d", i));
        }
        forecast.setMonths(months);
        forecast.setData(mockYearValues());
        return forecast;
    }

    /**
     * mock每日预测:工作日460上下,周末按8折,体现公式形态
     */
    private List<Integer> mockDailyValues() {
        List<Integer> values = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (int i = 1; i <= DAILY_DAYS; i++) {
            LocalDate day = today.plusDays(i);
            boolean weekend = day.getDayOfWeek().getValue() >= 6;
            values.add((int) Math.round(500 * 0.92 * (weekend ? 0.8 : 1)));
        }
        return values;
    }

    private List<Integer> mockYearValues() {
        List<Integer> values = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            values.add(12000 + i * 200);
        }
        return values;
    }

    // ==================== 预测逻辑整体说明(以 2026-11-27 为锚点示例) ====================
    //
    // 报表所有指标都以「查询当天」为锚实时计算,不查预测结果表。
    // 数据源: TR_OUTP_FIN_CLINIC_MASTER 按就诊日聚合(前年1-1~昨天), 缓存至次日0点。
    //
    // 【增长率】growth = 近30天门诊量合计(2026-10-28~11-26) ÷ 去年同期30天合计(2025-10-28~11-26)
    //   去年同期无数据(分母≤0)时 growth 按 1
    //
    // 【未来30天每日预测】预测 2026-11-28 ~ 2026-12-27,每天独立:
    //   预测值 = 去年同期日(2025-11-28 ~ 2025-12-27)实际门诊量 × growth, 去年当日无记录按0
    //
    // 【概览四指标】
    //   明日     = 日预测第1天
    //   未来一周 = 日预测前7天求和
    //   未来一月 = 日预测30天求和
    //   未来一年 = 月预测12个月求和
    //
    // 【当前月起12个月预测】预测 2026-11 ~ 2027-10:
    //   每月 = 去年同期月(2025-11 ~ 2026-10)实际门诊量 × growth(与日预测同口径)


    /** java.util.Date→java.sql.Date：绑定按DATE类型发送， 避免TIMESTAMP绑定触发的列隐式转换致索引失效 */
    private static java.sql.Date sqlDay(java.util.Date d) {
        return d == null ? null : new java.sql.Date(d.getTime());
    }
}
