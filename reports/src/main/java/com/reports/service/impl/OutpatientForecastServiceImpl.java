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
            List<Integer> monthValues = calcYearForecast(request);
            OverviewData overview = new OverviewData();
            overview.setTomorrow(daily.isEmpty() ? 0 : daily.get(0).value);
            overview.setNextWeek(sumRange(daily, 0, 7));
            overview.setNextMonth(sumRange(daily, 0, DAILY_DAYS));
            overview.setNextYear(monthValues.stream().mapToInt(Integer::intValue).sum());
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
            List<Integer> monthValues = calcYearForecast(request);
            YearForecast forecast = new YearForecast();
            List<String> months = new ArrayList<>();
            List<Integer> data = new ArrayList<>();
            int year = LocalDate.now().getYear();
            for (int i = 1; i <= 12; i++) {
                months.add(year + "-" + String.format("%02d", i));
                data.add(monthValues.get(i - 1));
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
     * 未来30天每日预测:基础量 × 就诊系数,clamp ±10%
     */
    private List<DailyForecast> calcDailyForecast(OutpatientForecastRequest request) {
        LocalDate today = LocalDate.now();
        Date last30Start = toDate(today.minusDays(DAILY_DAYS));
        Date yesterday = toDate(today.minusDays(1));

        double avgReg = nvl(forecastMapper.queryAvgReg(last30Start, yesterday, request.getDeptCode(), request.getDeptName()));
        double avgAppoint = nvl(forecastMapper.queryAvgAppoint(last30Start, yesterday, request.getDeptCode(), request.getDeptName()));
        double visitCoef = nvl(forecastMapper.queryVisitCoef(last30Start, yesterday, request.getDeptCode(), request.getDeptName()), 1d);

        List<DailyForecast> result = new ArrayList<>();
        for (int i = 1; i <= DAILY_DAYS; i++) {
            LocalDate day = today.plusDays(i);
            Double appoint = forecastMapper.queryAppoint(toDate(day), request.getDeptCode(), request.getDeptName());
            double base = (appoint != null ? appoint : avgAppoint) + avgReg;
            double weatherCoef = nvl(forecastMapper.queryWeatherCoef(toDate(day)), 1d);
            double holidayCoef = isLegalHoliday(day) ? calcHolidayCoef(request) : 1d;
            result.add(new DailyForecast(day, clampForecast(base, base * visitCoef * weatherCoef * holidayCoef)));
        }
        return result;
    }

    /**
     * 今年12个月预测:1月按去年比前年增率外推,2-12月按预测全年量 × 去年同月占比;历史不足按近90天日均兜底
     */
    private List<Integer> calcYearForecast(OutpatientForecastRequest request) {
        LocalDate today = LocalDate.now();
        int year = today.getYear();
        String deptCode = request.getDeptCode();
        String deptName = request.getDeptName();

        Map<Integer, Integer> lastYear = toMonthMap(forecastMapper.queryMonthlyVolume(String.valueOf(year - 1), deptCode, deptName));
        int lastYearTotal = lastYear.values().stream().mapToInt(Integer::intValue).sum();
        Map<Integer, Integer> prevYear = toMonthMap(forecastMapper.queryMonthlyVolume(String.valueOf(year - 2), deptCode, deptName));
        int prevYearTotal = prevYear.values().stream().mapToInt(Integer::intValue).sum();

        int elapsedDays = today.getDayOfYear() - 1;
        double ytd = forecastMapper.queryYtdVolume(toDate(LocalDate.of(year, 1, 1)), toDate(today.minusDays(1)), deptCode, deptName);
        double projectedYear = elapsedDays > 0 ? ytd / elapsedDays * 365 : 0;

        double avgDaily90 = nvl(forecastMapper.queryAvgReg(toDate(today.minusDays(90)), toDate(today.minusDays(1)), deptCode, deptName));

        List<Integer> values = new ArrayList<>();
        for (int m = 1; m <= 12; m++) {
            double value;
            if (m == 1) {
                // 1月:去年1月 × (1 + 去年比前年门诊量增率)
                value = prevYearTotal > 0
                        ? lastYear.getOrDefault(1, 0) * (1 + (double) lastYearTotal / prevYearTotal - 1)
                        : 0;
            } else {
                // 2-12月:预测全年量 × 去年同月占比
                value = lastYearTotal > 0 ? projectedYear * lastYear.getOrDefault(m, 0) / lastYearTotal : 0;
            }
            if (value <= 0) {
                value = avgDaily90 * LocalDate.of(year, m, 1).lengthOfMonth();
            }
            values.add((int) Math.round(value));
        }
        return values;
    }

    /**
     * 节假日系数:近30次法定节假日较前一正常工作日门诊量增长率的平均数
     */
    private double calcHolidayCoef(OutpatientForecastRequest request) {
        LocalDate today = LocalDate.now();
        List<Date> holidays = forecastMapper.queryRecentHolidays(toDate(today.minusDays(365)), toDate(today.minusDays(1)));
        double sum = 0;
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
                sum += (double) holidayVol / prevVol;
                count++;
            }
        }
        return count > 0 ? sum / count : 1d;
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

}
