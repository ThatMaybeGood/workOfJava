package com.showexcel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.showexcel.mapper.HolidayCalendarMapper;
import com.showexcel.mapper.HolidayTypeMapper;
import com.showexcel.model.HolidayCalendar;
import com.showexcel.model.HolidayType;
import com.showexcel.service.HolidayService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @author Mine
 * @version 1.0
 * 描述:
 * @date 2025/10/30 10:49
 */
@Service
@Slf4j
public class HolidayServiceImpl implements HolidayService {

    @Autowired
    private HolidayCalendarMapper holidayCalendarMapper;

    @Autowired
    private HolidayTypeMapper holidayTypeMapper;

    @Override
    public List<HolidayCalendar> getHolidayCalendar(Integer year) {
        QueryWrapper<HolidayCalendar> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("year", year)
                .orderByAsc("holiday_date");
        return holidayCalendarMapper.selectList(queryWrapper);
    }

    @Override
    public Boolean batchUpdateHolidays(List<HolidayCalendar> holidays) {
        try {
            for (HolidayCalendar holiday : holidays) {
                QueryWrapper<HolidayCalendar> queryWrapper = new QueryWrapper<>();
                // 使用日期格式进行精确匹配，避免时间部分的影响
                queryWrapper.apply("DATE(holiday_date) = DATE({0})", holiday.getHolidayDate());

                HolidayCalendar existing = holidayCalendarMapper.selectOne(queryWrapper);
                if (existing != null) {
                    holiday.setId(existing.getId());
                    holidayCalendarMapper.updateById(holiday);
                } else {
                    holidayCalendarMapper.insert(holiday);
                }
            }
            return true;
        } catch (Exception e) {
            log.error("批量更新节假日失败", e);
            return false;
        }
    }

    @Override
    public Boolean markHoliday(Date date, String holidayName, Boolean isHoliday, String holidayType) {
        try {
            HolidayCalendar holiday = new HolidayCalendar();
            holiday.setHolidayDate(date);
            holiday.setHolidayName(holidayName);
            holiday.setIsHoliday(isHoliday);
            holiday.setHolidayType(holidayType);
            holiday.setYear(getYearFromDate(date));

            QueryWrapper<HolidayCalendar> queryWrapper = new QueryWrapper<>();
            // 使用日期格式进行精确匹配，避免时间部分的影响
            queryWrapper.apply("DATE(holiday_date) = DATE({0})", date);

            HolidayCalendar existing = holidayCalendarMapper.selectOne(queryWrapper);
            if (existing != null) {
                holiday.setId(existing.getId());
                holidayCalendarMapper.updateById(holiday);
            } else {
                holidayCalendarMapper.insert(holiday);
            }
            return true;
        } catch (Exception e) {
            log.error("标记节假日失败", e);
            return false;
        }
    }

    @Override
    public List<HolidayType> getHolidayTypes() {
        return holidayTypeMapper.selectList(new QueryWrapper<>());
    }

    @Override
    public HolidayCalendar findByDate(Date date) {
        return null;
    }

    @Override
    public List<HolidayCalendar> findHolidayDatesByYear(Integer year) {
        List<HolidayCalendar> holidayDatesByYear = holidayCalendarMapper.getHolidayDatesByYear(year);
        log.info("查询到{}年的节假日信息：{}", year, holidayDatesByYear);
        return holidayDatesByYear;
    }

    @Override
    public Boolean initNationalHolidays(Integer year) {
        // 这里可以实现初始化国家法定节假日的逻辑
        // 比如从API获取或读取预设数据
        return true;
    }

    private Integer getYearFromDate(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.YEAR);
    }
}