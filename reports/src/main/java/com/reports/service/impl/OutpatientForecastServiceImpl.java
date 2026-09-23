package com.reports.service.impl;

import com.reports.config.ReportDataConfig;
import com.reports.dto.request.OutpatientForecastRequest;
import com.reports.dto.response.outpatient.forecast.*;
import com.reports.entity.OutpatientForecastYearEntity;
import com.reports.mapper.OutpatientForecastMapper;
import com.reports.service.OutpatientForecastService;
import com.reports.util.SeqUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
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

    private OverviewData queryOverviewCompute(OutpatientForecastRequest request) {
        try {
            List<DailyForecast> daily = calcDailyForecast(request);
            List<MonthPoint> monthValues = calcYearForecast(request);
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
            List<DailyForecast> daily = calcDailyForecast(request);
            MonthForecast forecast = new MonthForecast();
            List<String> dates = new ArrayList<>();
            List<Integer> data = new ArrayList<>();
            for (DailyForecast row : daily) {
                dates.add(row.date.toString());
                data.add(row.value);
            }
            forecast.setDates(dates);
            forecast.setData(data);
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
            List<MonthPoint> monthValues = calcYearForecast(request);
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
     * 步骤:
     * 1. 取三个基准值:近30天挂号日均avgReg、近30天预约日均avgAppoint(预约无当日数据时兜底用)、近30天就诊系数visitCoef
     * 2. 逐日循环未来30天,每天:
     *    基础量base = 当日实时预约量(查不到则退回avgAppoint) + avgReg
     *    预测值 = base × visitCoef × 当日天气系数(查不到按1) × 节假日系数(仅法定假日日,非法定假日按1)
     * 3. clampForecast把预测值压回base的±10%区间
     */
    private List<DailyForecast> calcDailyForecast(OutpatientForecastRequest request) {
        LocalDate today = LocalDate.now();
        Date last30Start = toDate(today.minusDays(DAILY_DAYS));
        Date yesterday = toDate(today.minusDays(1));

        // 步骤1:三个基准值
        double avgReg = nvl(forecastMapper.queryAvgReg(last30Start, yesterday, request.getDeptCode(), request.getDeptName()));
        double avgAppoint = nvl(forecastMapper.queryAvgAppoint(last30Start, yesterday, request.getDeptCode(), request.getDeptName()));
        double visitCoef = nvl(forecastMapper.queryVisitCoef(last30Start, yesterday, request.getDeptCode(), request.getDeptName()), 1d);

        List<DailyForecast> result = new ArrayList<>();
        // 节假日系数30天内不变,首次遇到法定假日日才算,后续复用
        Double holidayCoefCache = null;
        for (int i = 1; i <= DAILY_DAYS; i++) {
            LocalDate day = today.plusDays(i);
            // 步骤2:当日基础量 = 实时预约(无则兜底avgAppoint) + 挂号日均
            Double appoint = forecastMapper.queryAppoint(toDate(day), request.getDeptCode(), request.getDeptName());
            double base = (appoint != null ? appoint : avgAppoint) + avgReg;
            double weatherCoef = nvl(forecastMapper.queryWeatherCoef(toDate(day)), 1d);
            double holidayCoef = 1d;
            if (isLegalHoliday(day)) {
                if (holidayCoefCache == null) {
                    holidayCoefCache = calcHolidayCoef(request);
                }
                holidayCoef = holidayCoefCache;
            }
            // 步骤3:乘系数后clamp到±10%
            result.add(new DailyForecast(day, clampForecast(base, base * visitCoef * weatherCoef * holidayCoef)));
        }
        return result;
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
     *  - 今年剩余月:projectedYear × 去年同月量 ÷ 去年总量(按去年月度分布拆分全年预测)
     *  - 今年1月(仅当前月为1月时命中):去年1月 × 去年总量÷前年总量
     *  - 明年已过月(月号<当前月):今年同月实际 × growth
     *  - 明年未过月(月号>=当前月):去年同月量 × growth
     *  - 任一月算出来≤0:改用 avgDaily90 × 当月天数 兜底
     */
    private List<MonthPoint> calcYearForecast(OutpatientForecastRequest request) {
        LocalDate today = LocalDate.now();
        int year = today.getYear();
        int curMonth = today.getMonthValue();
        String deptCode = request.getDeptCode();
        String deptName = request.getDeptName();

        // 前置:去年/前年/今年已过的分月量
        Map<Integer, Integer> lastYear = toMonthMap(forecastMapper.queryMonthlyVolume(String.valueOf(year - 1), deptCode, deptName));
        int lastYearTotal = lastYear.values().stream().mapToInt(Integer::intValue).sum();
        Map<Integer, Integer> prevYear = toMonthMap(forecastMapper.queryMonthlyVolume(String.valueOf(year - 2), deptCode, deptName));
        int prevYearTotal = prevYear.values().stream().mapToInt(Integer::intValue).sum();
        Map<Integer, Integer> curYear = toMonthMap(forecastMapper.queryMonthlyVolume(String.valueOf(year), deptCode, deptName));

        // 前置:今年全年外推量 YTD/已过天数*今年天数(1月1日当天elapsedDays=0则外推量为0)
        int elapsedDays = today.getDayOfYear() - 1;
        int yearDays = java.time.Year.of(year).length();
        double ytd = forecastMapper.queryYtdVolume(toDate(LocalDate.of(year, 1, 1)), toDate(today.minusDays(1)), deptCode, deptName);
        double projectedYear = elapsedDays > 0 ? ytd / elapsedDays * yearDays : 0;

        // 前置:同比增幅 今年YTD/去年同期YTD
        double ytdPrev = forecastMapper.queryYtdVolume(toDate(LocalDate.of(year - 1, 1, 1)), toDate(today.minusYears(1).minusDays(1)), deptCode, deptName);
        double growth = ytdPrev > 0 ? ytd / ytdPrev : 0;

        // 前置:近90天挂号日均(兜底用)
        double avgDaily90 = nvl(forecastMapper.queryAvgReg(toDate(today.minusDays(90)), toDate(today.minusDays(1)), deptCode, deptName));

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
                    // 今年剩余月:预测全年量 × 去年同月占比
                    value = lastYearTotal > 0 ? projectedYear * lastYear.getOrDefault(m, 0) / lastYearTotal : 0;
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
     * 找不到前一个工作日或前一工作日门诊量为0的样本跳过;一个有效样本都没有则增长率按0(系数按1)
     */
    private double calcHolidayCoef(OutpatientForecastRequest request) {
        LocalDate today = LocalDate.now();
        List<Date> holidays = forecastMapper.queryRecentHolidays(toDate(today.minusDays(365)), toDate(today.minusDays(1)));
        double growthSum = 0;
        int count = 0;
        for (Date holiday : holidays) {
            if (count >= HOLIDAY_SAMPLE_LIMIT) {
                break;
            }
            Date prevWorkday = forecastMapper.queryPrevWorkday(holiday);
            if (prevWorkday == null) {
                continue;
            }
            int holidayVol = forecastMapper.queryVolume(holiday, request.getDeptCode(), request.getDeptName());
            int prevVol = forecastMapper.queryVolume(prevWorkday, request.getDeptCode(), request.getDeptName());
            if (prevVol > 0) {
                growthSum += (double) (holidayVol - prevVol) / prevVol;
                count++;
            }
        }
        // 系数 = 1 + 增长率平均数(与直接平均 节假日量/工作日量 恒等)
        return count > 0 ? 1 + growthSum / count : 1d;
    }

    private boolean isLegalHoliday(LocalDate day) {
        return "LEGAL_HOLIDAY".equals(forecastMapper.queryHolidayType(toDate(day)));
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

    private Map<Integer, Integer> toMonthMap(List<OutpatientForecastYearEntity> rows) {
        if (rows == null) {
            return Collections.emptyMap();
        }
        return rows.stream().collect(Collectors.toMap(
                r -> Integer.parseInt(r.getForecastMonth()), OutpatientForecastYearEntity::getForecastValue, (a, b) -> a));
    }

    private double nvl(Double value) {
        return value != null ? value : 0d;
    }

    private double nvl(Double value, double defaultValue) {
        return value != null ? value : defaultValue;
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
    //   今年剩余月(09~12): projectedYear × 去年同月量 ÷ 去年总量
    //   明年已过月(01~08):  今年同月实际 × growth
    //   明年未过月(09~12):  去年同月量 × growth
    //   任一月≤0 兜底: 近90天挂号日均 × 当月天数

}
