package com.mergedata.server.impl;

import com.mergedata.mapper.HolidayMapper;
import com.mergedata.model.YQHolidayCalendar;
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
    public List<YQHolidayCalendar> findAll() {
        return holidayMapper.selectAll();
    }


    @Override
    public List<YQHolidayCalendar> findByDate(LocalDate reportDate) {
         return holidayMapper.selectByDate(reportDate);
    }


    @Override
    @Transactional
    public Boolean insert(YQHolidayCalendar holiday) {

        List<YQHolidayCalendar> holidayCalendar = holidayMapper.selectByDate(holiday.getHolidayDate());

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
    public Boolean delete(YQHolidayCalendar holiday) {
         return holidayMapper.delete(holiday.getSerialNo())>0?true:false;
    }


    @Transactional
    @Override
    public Boolean batchInsertList(List<YQHolidayCalendar> list) {
        PrimaryKeyGenerator pks = new PrimaryKeyGenerator();

        if (list == null || list.isEmpty()) {
            // 如果列表为空，直接返回
            return false;
        }

        for (YQHolidayCalendar dto : list) {
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
    public Boolean update(YQHolidayCalendar holiday) {
        return holidayMapper.delete(holiday.getSerialNo())>0?true:false;
    }

}
