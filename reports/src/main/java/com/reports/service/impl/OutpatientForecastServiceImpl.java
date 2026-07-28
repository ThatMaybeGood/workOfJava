package com.reports.service.impl;

import com.reports.config.ReportDataConfig;
import com.reports.dto.request.OutpatientForecastRequest;
import com.reports.dto.response.outpatient.forecast.*;
import com.reports.entity.OutpatientForecastMonthEntity;
import com.reports.entity.OutpatientForecastOvEntity;
import com.reports.entity.OutpatientForecastYearEntity;
import com.reports.mapper.OutpatientForecastMapper;
import com.reports.service.OutpatientForecastService;
import com.reports.util.SeqUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 预测门诊量报表服务实现
 */
@Slf4j
@Service
public class OutpatientForecastServiceImpl implements OutpatientForecastService {

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
        } else if (dataConfig.isJdbc()) {
            return queryOverviewByJdbc(request);
        } else {
            return queryOverviewByMybatisPlus(request);
        }
    }

    @Override
    public MonthForecast queryMonthForecast(OutpatientForecastRequest request) {
        log.info("查询未来30天预测，mode={}", dataConfig.getMode());
        if (dataConfig.isMock()) {
            return queryMonthForecastMock(request);
        } else if (dataConfig.isJdbc()) {
            return queryMonthForecastByJdbc(request);
        } else {
            return queryMonthForecastByMybatisPlus(request);
        }
    }

    @Override
    public YearForecast queryYearForecast(OutpatientForecastRequest request) {
        log.info("查询未来12个月预测，mode={}", dataConfig.getMode());
        if (dataConfig.isMock()) {
            return queryYearForecastMock(request);
        } else if (dataConfig.isJdbc()) {
            return queryYearForecastByJdbc(request);
        } else {
            return queryYearForecastByMybatisPlus(request);
        }
    }

    // ==================== Mock 模式 ====================

    private OverviewData queryOverviewMock(OutpatientForecastRequest request) {
        SeqUtil.next();
        OverviewData overview = new OverviewData();
        overview.setTomorrow(520);
        overview.setNextWeek(3680);
        overview.setNextMonth(15200);
        overview.setNextYear(168000);
        return overview;
    }

    private MonthForecast queryMonthForecastMock(OutpatientForecastRequest request) {
        SeqUtil.next();
        MonthForecast forecast = new MonthForecast();
        List<String> dates = new ArrayList<>();
        List<Integer> data = new ArrayList<>();
        for (int i = 1; i <= 30; i++) {
            dates.add("2024-02-" + String.format("%02d", i));
            data.add(400 + i * 5);
        }
        forecast.setDates(dates);
        forecast.setData(data);
        return forecast;
    }

    private YearForecast queryYearForecastMock(OutpatientForecastRequest request) {
        SeqUtil.next();
        YearForecast forecast = new YearForecast();
        List<String> months = new ArrayList<>();
        List<Integer> data = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            months.add("2024-" + String.format("%02d", i));
            data.add(12000 + i * 200);
        }
        forecast.setMonths(months);
        forecast.setData(data);
        return forecast;
    }

    // ==================== JdbcTemplate 模式 ====================

    private OverviewData queryOverviewByJdbc(OutpatientForecastRequest request) {
        log.info("使用 JdbcTemplate 查询概览数据");
        return queryOverviewMock(request);
    }

    private MonthForecast queryMonthForecastByJdbc(OutpatientForecastRequest request) {
        return queryMonthForecastMock(request);
    }

    private YearForecast queryYearForecastByJdbc(OutpatientForecastRequest request) {
        return queryYearForecastMock(request);
    }

    // ==================== MyBatis-Plus 模式 ====================

    private OverviewData queryOverviewByMybatisPlus(OutpatientForecastRequest request) {
        try {
            Date statDate = new Date();
            OutpatientForecastOvEntity entity = forecastMapper.queryOverview(statDate);
            return buildOverviewData(entity);
        } catch (Exception e) {
            log.warn("查询预测门诊量概览失败", e);
            return new OverviewData();
        }
    }

    private MonthForecast queryMonthForecastByMybatisPlus(OutpatientForecastRequest request) {
        try {
            Date statDate = new Date();
            List<OutpatientForecastMonthEntity> rows = forecastMapper.queryMonthForecast(statDate);
            return buildMonthForecast(rows);
        } catch (Exception e) {
            log.warn("查询未来30天预测失败", e);
            MonthForecast empty = new MonthForecast();
            empty.setDates(new ArrayList<>());
            empty.setData(new ArrayList<>());
            return empty;
        }
    }

    private YearForecast queryYearForecastByMybatisPlus(OutpatientForecastRequest request) {
        try {
            Date statDate = new Date();
            List<OutpatientForecastYearEntity> rows = forecastMapper.queryYearForecast(statDate);
            return buildYearForecast(rows);
        } catch (Exception e) {
            log.warn("查询未来12个月预测失败", e);
            YearForecast empty = new YearForecast();
            empty.setMonths(new ArrayList<>());
            empty.setData(new ArrayList<>());
            return empty;
        }
    }

    // ==================== 工具方法 ====================

    private OverviewData buildOverviewData(OutpatientForecastOvEntity entity) {
        if (entity == null) return new OverviewData();
        OverviewData dto = new OverviewData();
        dto.setTomorrow(entity.getTomorrow());
        dto.setNextWeek(entity.getNextWeek());
        dto.setNextMonth(entity.getNextMonth());
        dto.setNextYear(entity.getNextYear());
        return dto;
    }

    private MonthForecast buildMonthForecast(List<OutpatientForecastMonthEntity> rows) {
        MonthForecast forecast = new MonthForecast();
        List<String> dates = new ArrayList<>();
        List<Integer> data = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        for (OutpatientForecastMonthEntity row : rows) {
            if (row.getForecastDate() != null) {
                dates.add(sdf.format(row.getForecastDate()));
            }
            data.add(row.getForecastValue());
        }
        forecast.setDates(dates);
        forecast.setData(data);
        return forecast;
    }

    private YearForecast buildYearForecast(List<OutpatientForecastYearEntity> rows) {
        YearForecast forecast = new YearForecast();
        List<String> months = new ArrayList<>();
        List<Integer> data = new ArrayList<>();
        for (OutpatientForecastYearEntity row : rows) {
            months.add(row.getForecastMonth());
            data.add(row.getForecastValue());
        }
        forecast.setMonths(months);
        forecast.setData(data);
        return forecast;
    }

}
