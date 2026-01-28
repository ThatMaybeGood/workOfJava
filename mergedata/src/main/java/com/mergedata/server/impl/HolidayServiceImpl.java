package com.mergedata.server.impl;

import com.mergedata.constants.ResConstant;
import com.mergedata.mapper.HolidayMapper;
import com.mergedata.model.dto.HolidayRequestBody;
import com.mergedata.model.entity.YQHolidayCalendarEntity;
import com.mergedata.model.vo.YQHolidayCalendarVO;
import com.mergedata.server.YQHolidayService;
import com.mergedata.util.PrimaryKeyGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Slf4j
public class HolidayServiceImpl implements YQHolidayService {

    @Autowired
    HolidayMapper holidayMapper;

    @Override
    public List<YQHolidayCalendarEntity> findAll() {
        return holidayMapper.selectAll();
    }


    @Override
    public List<YQHolidayCalendarEntity> findByDate(LocalDate reportDate) {
         return holidayMapper.selectByDate(reportDate);
    }


    @Override
    @Transactional
    public Boolean insert(YQHolidayCalendarEntity holiday) {

        List<YQHolidayCalendarEntity> holidayCalendar = holidayMapper.selectByDate(holiday.getHolidayDate());

        if(holidayCalendar.size() > 0 ){
            holidayMapper.update(holiday.getHolidayDate());
        }

        PrimaryKeyGenerator pks = new PrimaryKeyGenerator();
        holiday.setSerialNo(pks.generateKey());

        holiday.setHolidayMonth(String.valueOf(holiday.getHolidayDate().getMonthValue()));
        holiday.setHolidayYear(String.valueOf(holiday.getHolidayDate().getYear()));

        int insert = holidayMapper.insert(holiday);

        if (insert != 1) {
            throw new RuntimeException("单条写入存储过程调用失败，数据同步中断。");
        }
        return true;
    }


    @Override
    @Transactional
    public Boolean delete(YQHolidayCalendarEntity holiday) {
         return holidayMapper.delete(holiday.getSerialNo())>0?true:false;
    }

    @Override
    public YQHolidayCalendarVO queryDateType(HolidayRequestBody holidayRequestBody) {
        LocalDate holidayDate  = holidayRequestBody.getReportDate();

        YQHolidayCalendarVO yqHolidayCalendarVO = new YQHolidayCalendarVO();
        yqHolidayCalendarVO.setHolidayDate(holidayDate);


        // 判断是住院/门诊
        if (holidayRequestBody.getQueryType()== ResConstant.TYPE_OUTP){


        }

        if (isHoliday( holidayDate)){
            yqHolidayCalendarVO.setHolidayType(ResConstant.HOLIDAY_IS);

           return yqHolidayCalendarVO;

        }else {
            // ❗当前是工作日 且 前一天是节假日/周末
            if(isHoliday(holidayDate.minusDays(1))){
                yqHolidayCalendarVO.setHolidayType(ResConstant.HOLIDAY_PRE);
                return yqHolidayCalendarVO;
            }
            // ❗当前是工作日 且 后一天是节假日/周末
            if(isHoliday(holidayDate.plusDays(1))){
                yqHolidayCalendarVO.setHolidayType(ResConstant.HOLIDAY_AFTER);
                return yqHolidayCalendarVO;
            }
        }


        yqHolidayCalendarVO.setHolidayType(ResConstant.HOLIDAY_NOT);
        return yqHolidayCalendarVO;
    }

    /**
     * 判断日期是否为节假日 (使用 Set 版本)
     */
    private boolean isHoliday(LocalDate targetDate) {
        // 1. 获取所有必需的原始数据
        List<YQHolidayCalendarEntity> holidays = holidayMapper.selectByDate(targetDate);

        if (holidays.size() > 0){
            return true;
        }
        return false;

    }

    @Transactional
    @Override
    public Boolean batchInsertList(List<YQHolidayCalendarEntity> list) {
        PrimaryKeyGenerator pks = new PrimaryKeyGenerator();

        if (list == null || list.isEmpty()) {
            // 如果列表为空，直接返回
            return false;
        }

        for (YQHolidayCalendarEntity dto : list) {
            // 生成主键
            dto.setSerialNo(pks.generateKey());
            dto.setValidStatus("1");
        }
        // 执行作废操作
        int i = holidayMapper.batchInsertList(list);

        if (i != list.size()) {
            throw new RuntimeException("批量写入存储过程调用失败，数据同步中断。");
        }
        return true;
    }

    @Override
    public Boolean update(YQHolidayCalendarEntity holiday) {
        return holidayMapper.delete(holiday.getSerialNo())>0?true:false;
    }

}
