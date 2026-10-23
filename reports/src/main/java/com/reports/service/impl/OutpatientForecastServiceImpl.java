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
import java.util.stream.Collectors;

/**
 * 预测门诊量报表服务实现
 * 公式:预测门诊量 = (实时预约量 + 近30天挂号量日均) × 就诊系数,再clamp到±10%上下限
 * 就诊系数 = (1 - 近30天爽约退号率) × 天气出勤系数 × 节假日系数(无数据系数按1处理)
 */
@Slf4j
@Service
public class OutpatientForecastServiceImpl implements OutpatientForecastService {

    /** 上下限浮动比例 */
    private static final double LIMIT_RATE = 0.1;
    /** 30天明细天数 */
    private static final int DAILY_DAYS = 30;
    /** 节假日系数取样:近一年法定节假日,最多30个 */
    private static final int HOLIDAY_SAMPLE_LIMIT = 30;

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
     * 分段缓存: 过去段(全表按日聚合+就诊系数, 仅夜间抽数后变化)缓存至次日0点自动过期;
     * 日预测整体10min刷新(近30天预约日均/天气/节假日可在维护页随时改, 快速生效); 概览/月图/年图共用
     */
    private static final long FC_TTL = 10 * 60 * 1000L;
    private final Map<String, DailyContext> dailyCache = new ConcurrentHashMap<>();
    private final Map<String, List<MonthPoint>> yearCache = new ConcurrentHashMap<>();
    private final Map<String, PastEntry> pastVolCache = new ConcurrentHashMap<>();
    private final Map<String, CoefEntry> visitCoefCache = new ConcurrentHashMap<>();

    /** 带过期时间的过去段聚合缓存 */
    private static class PastEntry {
        private final Map<String, Double> vol;
        private final long expireAt;

        private PastEntry(Map<String, Double> vol, long expireAt) {
            this.vol = vol;
            this.expireAt = expireAt;
        }
    }

    /** 带过期时间的就诊系数缓存 */
    private static class CoefEntry {
        private final double value;
        private final long expireAt;

        private CoefEntry(double value, long expireAt) {
            this.value = value;
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
            List<MonthPoint> rows = calcYearForecast(request, fresh.daily, fresh.dayVol);
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

    /** 近30天就诊系数: 过去段指标, 与按日聚合同策略缓存至次日0点 */
    private double visitCoefCached(OutpatientForecastRequest request) {
        String key = fcKey(request);
        long now = System.currentTimeMillis();
        CoefEntry hit = visitCoefCache.get(key);
        if (hit != null && hit.expireAt > now) {
            return hit.value;
        }
        synchronized (("coef|" + key).intern()) {
            now = System.currentTimeMillis();
            hit = visitCoefCache.get(key);
            if (hit != null && hit.expireAt > now) {
                return hit.value;
            }
            LocalDate today = LocalDate.now();
            double value = nvl(forecastMapper.queryVisitCoef(
                    sqlDay(toDate(today.minusDays(DAILY_DAYS))), sqlDay(toDate(today.minusDays(1))),
                    request.getDeptCode(), request.getDeptName()), 1d);
            visitCoefCache.put(key, new CoefEntry(value, nextMidnightMillis()));
            return value;
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
     * 未来30天每日预测
     * 取数分段: 过去段(门诊量按日聚合前年1-1~昨天)缓存至次日0点; 未来已有预约行随日预测10min刷新;
     *       天气/法定假日小表直查; 区间指标在Java内对按日Map计算
     * 公式(逐日对应, 不用窗口平均):
     *   第i天基础量 = 一个月前对应日(今天+i-30)的实际挂号行数 + 当日实时预约量
     *   当日实时预约量 = TR_OUTP_FIN_APPOINTS 未来预约行(当前约15天, 记N);
     *       第1~N天用当天, 第N+1~30天沿用第1~N天对应位置(第N+1天对第1天); 对应不到按0
     *   预测值 = 基础量 × 就诊系数 × 当日天气系数(查不到按1) × 节假日系数(仅法定假日日,非法定假日按1)
     *   最后clampForecast把预测值压回基础量的±10%区间
     */
    private DailyContext calcDailyForecast(OutpatientForecastRequest request) {
        LocalDate today = LocalDate.now();
        LocalDate horizonStartL = today.plusDays(1);
        LocalDate horizonEndL = today.plusDays(DAILY_DAYS);

        // 过去段: 按日聚合缓存至次日0点, 分月/YTD/节假日样本全在这里算
        Map<String, Double> dayVol = pastVolCached(request);
        // 未来已有预约行(实时预约表TR_OUTP_FIN_APPOINTS, 当前约15天), 随日预测10min刷新
        Map<String, Double> futureAppoint = new HashMap<>();
        for (ForecastDayRow r : nvlList(forecastMapper.queryFutureAppointRange(
                sqlDay(toDate(today)), sqlDay(toDate(horizonEndL)),
                request.getDeptCode(), request.getDeptName()))) {
            if (r.getDd() != null) {
                futureAppoint.put(r.getDd(), nvl(r.getAppoint()));
            }
        }
        // 诊断日志: 打印实际加载到的未来预约行, 部署验证用
        StringBuilder sample = new StringBuilder();
        futureAppoint.forEach((k, v) -> {
            if (sample.length() < 200) {
                sample.append(k).append('=').append(v).append(',');
            }
        });
        log.info("未来预约行加载: 共{}天 [{}]", futureAppoint.size(), sample);
        double visitCoef = visitCoefCached(request);
        Map<String, Double> weatherMap = toDayMap(forecastMapper.queryWeatherRange(
                sqlDay(toDate(horizonStartL)), sqlDay(toDate(horizonEndL))));
        Set<String> legalHolidays = new HashSet<>(nvlList(forecastMapper.queryLegalHolidayRange(
                sqlDay(toDate(today.minusDays(365))), sqlDay(toDate(horizonEndL)))));

        // 逐日对应: 未来第i天配今天+i-30(一个月前同一天)的实际挂号行数; 不再用窗口平均
        int appointDays = Math.min(futureAppoint.size(), DAILY_DAYS);
        log.info("逐日对应参数: N(预约覆盖)={}", appointDays);

        List<DailyForecast> result = new ArrayList<>();
        // 节假日系数30天内不变,首次遇到法定假日日才算,后续复用
        Double holidayCoefCache = null;
        for (int i = 1; i <= DAILY_DAYS; i++) {
            LocalDate day = today.plusDays(i);
            String dd = day.toString();
            // 挂号项: 一个月前对应日的实际行数, 当天无记录按0
            double reg = nvl(dayVol.get(day.minusDays(30).toString()));
            // 预约项: 第1~N天用当天; 第N+1~30天沿用第1~N天对应位置(第N+1天对第1天)
            String refDd = i <= appointDays ? dd : today.plusDays(i - appointDays).toString();
            double appoint = futureAppoint.getOrDefault(refDd, 0d);
            double base = reg + appoint;
            double weatherCoef = nvl(weatherMap.get(dd), 1d);
            double holidayCoef = 1d;
            if (legalHolidays.contains(dd)) {
                if (holidayCoefCache == null) {
                    holidayCoefCache = calcHolidayCoef(legalHolidays, dayVol);
                }
                holidayCoef = holidayCoefCache;
            }
            // 乘系数后clamp到±10%
            result.add(new DailyForecast(day, clampForecast(base, base * visitCoef * weatherCoef * holidayCoef)));
        }
        // expireAt由外层dailyContextCached包装时赋值
        return new DailyContext(result, dayVol, 0L);
    }

    /**
     * 当前月起12个月预测(跨年)
     * 前置数据:
     *  - lastYear/prevYear:去年、前年的分月门诊量
     *  - curYear:今年已过的分月门诊量(预测明年已过月用)
     *  - projectedYear:今年全年外推量 = 今年截至昨日YTD ÷ 今年已过天数 × 今年天数(闰年366)
     *  - growth:今年同比 = 今年YTD ÷ 去年同期YTD
     *  - avgDaily90:近90天挂号日均,历史不足时兜底
     * 逐月规则:
     *  - 今年剩余月:projectedYear × 去年同月量 ÷ 去年总量(按去年月度分布拆分全年预测),
     *    其中落在未来30天内的天数替换为日预测值(blendWithDaily,保证日/月两套口径一致)
     *  - 今年1月(仅当前月为1月时命中):去年1月 × 去年总量÷前年总量
     *  - 明年已过月(月号<当前月):今年同月实际 × growth
     *  - 明年未过月(月号>=当前月):去年同月量 × growth
     *  - 任一月算出来≤0:改用 avgDaily90 × 当月天数 兜底
     */
    private List<MonthPoint> calcYearForecast(OutpatientForecastRequest request, List<DailyForecast> daily,
                                              Map<String, Double> dayVol) {
        LocalDate today = LocalDate.now();
        int year = today.getYear();
        int curMonth = today.getMonthValue();
        LocalDate yesterdayL = today.minusDays(1);

        // 分月量/YTD/同比/兜底日均全部由日聚合Map在Java内算(与日预测同一份查询,不重复查库)
        Map<Integer, Integer> lastYear = monthSums(dayVol, year - 1);
        int lastYearTotal = lastYear.values().stream().mapToInt(Integer::intValue).sum();
        Map<Integer, Integer> prevYear = monthSums(dayVol, year - 2);
        int prevYearTotal = prevYear.values().stream().mapToInt(Integer::intValue).sum();
        Map<Integer, Integer> curYear = monthSums(dayVol, year);

        // 前置:今年全年外推量 YTD/已过天数*今年天数(1月1日当天elapsedDays=0则外推量为0)
        int elapsedDays = today.getDayOfYear() - 1;
        int yearDays = java.time.Year.of(year).length();
        double ytd = sumInRange(dayVol, LocalDate.of(year, 1, 1), yesterdayL);
        double projectedYear = elapsedDays > 0 ? ytd / elapsedDays * yearDays : 0;

        // 前置:同比增幅 今年YTD/去年同期YTD
        double ytdPrev = sumInRange(dayVol, LocalDate.of(year - 1, 1, 1), today.minusYears(1).minusDays(1));
        double growth = ytdPrev > 0 ? ytd / ytdPrev : 0;

        // 前置:近90天挂号日均(兜底用)
        double avgDaily90 = avgInRange(dayVol, today.minusDays(90), yesterdayL);

        List<MonthPoint> values = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            java.time.YearMonth ym = java.time.YearMonth.of(year, curMonth).plusMonths(i);
            int m = ym.getMonthValue();
            double value;
            if (ym.getYear() == year) {
                if (m == 1) {
                    // 今年1月:去年1月 × 去年总量÷前年总量
                    value = prevYearTotal > 0
                            ? lastYear.getOrDefault(1, 0) * ((double) lastYearTotal / prevYearTotal)
                            : 0;
                } else {
                    // 今年剩余月:预测全年量 × 去年同月占比, 未来30天内的天数用日预测值替换
                    double model = lastYearTotal > 0 ? projectedYear * lastYear.getOrDefault(m, 0) / lastYearTotal : 0;
                    value = blendWithDaily(ym, model, daily);
                }
            } else if (m < curMonth) {
                // 明年已过月:今年同月实际 × 同比
                value = growth > 0 ? curYear.getOrDefault(m, 0) * growth : 0;
            } else {
                // 明年未过月:去年同月 × 同比
                value = growth > 0 ? lastYear.getOrDefault(m, 0) * growth : 0;
            }
            if (value <= 0) {
                // 历史不足兜底:近90天日均 × 当月天数
                value = avgDaily90 * ym.lengthOfMonth();
            }
            values.add(new MonthPoint(ym, (int) Math.round(value)));
        }
        return values;
    }

    /**
     * 节假日系数 = 1 + 近30次法定节假日较前一正常工作日门诊量增长率的平均数
     * 每个样本增长率 = (节假日量 - 前一工作日量) / 前一工作日量,最多取近一年最近30个法定假日
     * 取数: 法定假日清单与门诊量按日聚合均为上层批量查询结果, 前一工作日内存推算, 零额外DB交互
     * 找不到前一个工作日或前一工作日门诊量为0的样本跳过;一个有效样本都没有则增长率按0(系数按1)
     */
    private double calcHolidayCoef(Set<String> legalHolidays, Map<String, Double> dayVol) {
        LocalDate today = LocalDate.now();
        String pastStart = today.minusDays(365).toString();
        String yesterday = today.minusDays(1).toString();
        // 近一年法定假日倒序取前30个样本
        List<String> samples = legalHolidays.stream()
                .filter(dd -> dd.compareTo(pastStart) >= 0 && dd.compareTo(yesterday) <= 0)
                .sorted(Comparator.reverseOrder())
                .limit(HOLIDAY_SAMPLE_LIMIT)
                .collect(Collectors.toList());
        double growthSum = 0;
        int count = 0;
        for (String hol : samples) {
            String prev = findPrevWorkday(dayVol, legalHolidays, LocalDate.parse(hol));
            if (prev == null) {
                continue;
            }
            double prevVol = nvl(dayVol.get(prev));
            if (prevVol > 0) {
                growthSum += (nvl(dayVol.get(hol)) - prevVol) / prevVol;
                count++;
            }
        }
        // 系数 = 1 + 增长率平均数(与直接平均 节假日量/工作日量 恒等)
        return count > 0 ? 1 + growthSum / count : 1d;
    }

    /** 前一正常工作日: 就诊量表中有记录、非周末、非节假日的最近日(回看10天,与原SQL口径一致) */
    private static String findPrevWorkday(Map<String, Double> dayVol, Set<String> legalHolidays, LocalDate before) {
        for (int i = 1; i <= 10; i++) {
            LocalDate d = before.minusDays(i);
            String dd = d.toString();
            if (dayVol.containsKey(dd) && !legalHolidays.contains(dd)
                    && d.getDayOfWeek().getValue() < 6) {
                return dd;
            }
        }
        return null;
    }

    /**
     * 月模型值与日预测融合: 落在未来30天内的天数直接取日预测之和, 其余天数按月模型日均折算
     * 整月都在30天外→纯月模型; 整月都在30天内→纯日预测和(日/月图自然一致)
     */
    private double blendWithDaily(java.time.YearMonth ym, double model, List<DailyForecast> daily) {
        int days = ym.lengthOfMonth();
        int inHorizon = 0;
        double dailySum = 0;
        for (DailyForecast df : daily) {
            if (df.date.getYear() == ym.getYear() && df.date.getMonthValue() == ym.getMonthValue()) {
                inHorizon++;
                dailySum += df.value;
            }
        }
        if (inHorizon == 0) {
            return model;
        }
        if (inHorizon == days) {
            return dailySum;
        }
        return dailySum + model * (days - inHorizon) / days;
    }

    /**
     * 预测值clamp到基础量的±10%上下限
     */
    private int clampForecast(double base, double value) {
        if (base <= 0) {
            return 0;
        }
        double lo = base * (1 - LIMIT_RATE);
        double hi = base * (1 + LIMIT_RATE);
        return (int) Math.round(Math.max(lo, Math.min(hi, value)));
    }

    private int sumRange(List<DailyForecast> daily, int from, int to) {
        return daily.subList(from, Math.min(to, daily.size())).stream().mapToInt(d -> d.value).sum();
    }

    /** 区间日均: 有记录的天占分母, 与SQL AVG(按日GROUP BY)口径一致 */
    private static double avgInRange(Map<String, Double> dayMap, LocalDate start, LocalDate end) {
        double sum = 0;
        int n = 0;
        for (Map.Entry<String, Double> e : dayMap.entrySet()) {
            String dd = e.getKey();
            if (dd.compareTo(start.toString()) >= 0 && dd.compareTo(end.toString()) <= 0) {
                sum += e.getValue() == null ? 0 : e.getValue();
                n++;
            }
        }
        return n > 0 ? sum / n : 0;
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

    /** 按日聚合Map归并为某年分月量 */
    private static Map<Integer, Integer> monthSums(Map<String, Double> dayMap, int year) {
        Map<Integer, Integer> result = new HashMap<>();
        String prefix = year + "-";
        for (Map.Entry<String, Double> e : dayMap.entrySet()) {
            String dd = e.getKey();
            if (dd.startsWith(prefix)) {
                int month = Integer.parseInt(dd.substring(5, 7));
                result.merge(month, e.getValue() == null ? 0 : e.getValue().intValue(), Integer::sum);
            }
        }
        return result;
    }

    private double nvl(Double value) {
        return value != null ? value : 0d;
    }

    private double nvl(Double value, double defaultValue) {
        return value != null ? value : defaultValue;
    }

    private static <T> List<T> nvlList(List<T> list) {
        return list == null ? Collections.emptyList() : list;
    }

    /** 按日聚合行(dd yyyy-MM-dd)转Map, 循环内O(1)取数 */
    private static Map<String, Double> toDayMap(List<ForecastDayRow> rows) {
        Map<String, Double> map = new HashMap<>();
        for (ForecastDayRow r : nvlList(rows)) {
            if (r.getDd() != null) {
                map.put(r.getDd(), r.getNum());
            }
        }
        return map;
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

    // ==================== 预测逻辑整体说明(以 2026-09-23 为锚点示例) ====================
    //
    // 报表所有指标都以「查询当天」为锚实时计算,不查预测结果表。
    //
    // 【基准窗口】近30天 = 2026-08-24 ~ 2026-09-22(截至昨天),从挂号日汇总表取:
    //   avgReg     近30天挂号日均(按天汇总后取平均,无记录的天不占分母)
    //   avgAppoint 近30天预约日均(预约表无当日数据时的兜底值)
    //   visitCoef  就诊系数 = 1 - 近30天(退号+爽约)/挂号总量
    //
    // 【未来30天每日预测】预测 2026-09-24 ~ 2026-10-23,每天独立计算:
    //   基础量base = 当日实时预约量(tr_fc_appoint,查不到退回avgAppoint) + avgReg
    //   预测值   = base × visitCoef × 当日天气系数(tr_fc_weather,无数据按1)
    //              × 节假日系数(仅法定假日日;30天内只算一次,假日日复用)
    //   节假日系数 = 1 + 近30次法定假日「(节假日量-前一正常工作日量)/前一工作日量」的增长率平均数
    //              (前一正常工作日 = 排除周末及节假日的最近统计日;无样本按1)
    //   最后 clamp 到 base 的 ±10% 区间
    //
    // 【概览四指标】
    //   明日     = 日预测第1天(09-24)
    //   未来一周 = 日预测前7天求和(09-24~09-30)
    //   未来一月 = 日预测30天求和(09-24~10-23,即今天+30天)
    //   未来一年 = 月预测12个月求和
    //
    // 【当前月起12个月预测】预测 2026-09 ~ 2027-08:
    //   前置:projectedYear = 今年YTD(01-01~09-22) ÷ 已过天数265 × 366/365(闰年)
    //         growth = 今年YTD ÷ 去年同期YTD(2025-01-01~2025-09-22)
    //   今年剩余月(09~12): projectedYear × 去年同月量 ÷ 去年总量,
    //         其中落在未来30天内的天数替换为日预测值, 其余天数按月模型日均折算(日/月口径一致)
    //   今年1月(仅当前月为1月时命中):去年1月 × 去年总量÷前年总量
    //   明年已过月(01~08):  今年同月实际 × growth
    //   明年未过月(09~12):  去年同月量 × growth
    //   任一月≤0 兜底: 近90天挂号日均 × 当月天数


    /** java.util.Date→java.sql.Date：绑定按DATE类型发送， 避免TIMESTAMP绑定触发的列隐式转换致索引失效 */
    private static java.sql.Date sqlDay(java.util.Date d) {
        return d == null ? null : new java.sql.Date(d.getTime());
    }
}
