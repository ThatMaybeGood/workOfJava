package com.reports.service.impl;

import com.reports.config.ReportDataConfig;
import com.reports.dto.common.PageResult;
import com.reports.dto.request.WeatherMaintainRequest;
import com.reports.dto.response.outpatient.forecast.WeatherItem;
import com.reports.entity.WeatherEntity;
import com.reports.mapper.WeatherMaintainMapper;
import com.reports.service.WeatherMaintainService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 天气数据维护服务实现
 */
@Slf4j
@Service
public class WeatherMaintainServiceImpl implements WeatherMaintainService {

    /** 界面人工登记的天气来源标识 */
    private static final String SOURCE_MANUAL = "人工登记";

    private final ReportDataConfig dataConfig;
    private final WeatherMaintainMapper weatherMaintainMapper;

    @Autowired
    public WeatherMaintainServiceImpl(ReportDataConfig dataConfig, WeatherMaintainMapper weatherMaintainMapper) {
        this.dataConfig = dataConfig;
        this.weatherMaintainMapper = weatherMaintainMapper;
    }

    @Override
    public PageResult<WeatherItem> queryMaintainList(WeatherMaintainRequest request, Integer page, Integer pageSize) {
        log.info("查询天气维护明细，mode={}", dataConfig.getMode());
        if (!dataConfig.isMybatisPlus()) {
            return PageResult.of(new ArrayList<>(), 0L, page, pageSize);
        }
        try {
            List<WeatherEntity> rows = weatherMaintainMapper.queryWeatherByDate(request.getStartDate(), request.getEndDate());
            List<WeatherItem> allItems = new ArrayList<>();
            for (WeatherEntity row : rows) {
                allItems.add(buildWeatherItem(row));
            }
            int total = allItems.size();
            int start = (page - 1) * pageSize;
            int end = Math.min(start + pageSize, total);
            List<WeatherItem> pageList = start < total ? allItems.subList(start, end) : new ArrayList<>();
            return PageResult.of(pageList, (long) total, page, pageSize);
        } catch (Exception e) {
            log.warn("查询天气维护明细失败", e);
            return PageResult.of(new ArrayList<>(), 0L, page, pageSize);
        }
    }

    @Override
    public int saveMaintain(WeatherMaintainRequest request) {
        log.info("保存天气维护明细，mode={}", dataConfig.getMode());
        if (!dataConfig.isMybatisPlus() || request.getList() == null) {
            return 0;
        }
        int affected = 0;
        for (WeatherItem item : request.getList()) {
            WeatherEntity entity = new WeatherEntity();
            entity.setWeatherDate(item.getWeatherDate());
            entity.setWeatherType(item.getWeatherType());
            // 出勤系数不手填：取登记日前30天内同天气日期的爽约退号率，系数=1-率
            entity.setWeatherCoef(calcWeatherCoef(item.getWeatherType(), item.getWeatherDate()));
            entity.setWeatherSource(SOURCE_MANUAL);
            affected += weatherMaintainMapper.mergeWeather(entity);
        }
        return affected;
    }

    /**
     * 出勤系数 = 1 - 近30天同天气日期的爽约退号率，无数据返回空(按1处理)
     */
    private Double calcWeatherCoef(String weatherType, Date weatherDate) {
        if (weatherType == null || weatherType.isEmpty() || weatherDate == null) {
            return null;
        }
        // 登记日往前推30天为统计窗口
        Date end = new Date(weatherDate.getTime() - 24L * 3600 * 1000);
        Date start = new Date(end.getTime() - 29L * 24 * 3600 * 1000);
        Double rate = weatherMaintainMapper.queryNoShowRateByWeather(weatherType, start, end);
        if (rate == null) {
            return null;
        }
        return Math.round((1 - rate) * 100) / 100.0;
    }

    @Override
    public int deleteMaintain(WeatherMaintainRequest request) {
        log.info("删除天气维护明细，weatherDate={}，mode={}", request.getWeatherDate(), dataConfig.getMode());
        if (!dataConfig.isMybatisPlus() || request.getWeatherDate() == null) {
            return 0;
        }
        return weatherMaintainMapper.deleteWeatherByDate(request.getWeatherDate());
    }

    private WeatherItem buildWeatherItem(WeatherEntity entity) {
        WeatherItem item = new WeatherItem();
        item.setWeatherDate(entity.getWeatherDate());
        item.setWeatherType(entity.getWeatherType());
        item.setWeatherCoef(entity.getWeatherCoef());
        item.setWeatherSource(entity.getWeatherSource());
        return item;
    }
}
