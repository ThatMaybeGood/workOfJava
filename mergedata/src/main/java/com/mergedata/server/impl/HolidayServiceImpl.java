package com.mergedata.server.impl;

import com.mergedata.mapper.HolidayMapper;
import com.mergedata.model.YQHolidayCalendar;
import com.mergedata.server.YQHolidayService;
import com.mergedata.util.PrimaryKeyGenerator;
import com.mergedata.util.ValidStatusEnum;
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
    public List<YQHolidayCalendar> findByDate(String reportDate) {
         return holidayMapper.selectByDate(LocalDate.parse(reportDate));
    }


    @Override
    @Transactional
    public Boolean batchInsert(YQHolidayCalendar holiday) {

        PrimaryKeyGenerator pks = new PrimaryKeyGenerator();
        holiday.setSerialNo(pks.generateKey());

        int insert = holidayMapper.insert(holiday);

        if (insert != 1) {
            throw new RuntimeException("单条写入存储过程调用失败，数据同步中断。");
        }
        return true;
    }


    @Override
    @Transactional
    public Boolean update(YQHolidayCalendar holiday) {
        int update = holidayMapper.update(holiday.getSerialNo());
        return true;
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
            dto.setValidStatus(ValidStatusEnum.VALID);
        }
        // 执行作废操作
        int i = holidayMapper.batchInsertList(list);

        if (i != list.size()) {
            throw new RuntimeException("批量写入存储过程调用失败，数据同步中断。");
        }
        return true;
    }

}
