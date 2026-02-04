package com.mergedata.server.impl;

import com.mergedata.constants.Constant;
import com.mergedata.mapper.HolidayMapper;
import com.mergedata.model.entity.YQHolidayEntity;
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
    public List<YQHolidayEntity> findAll() {
        return holidayMapper.selectAll();
    }


    @Override
    public List<YQHolidayEntity> findByDate(LocalDate reportDate) {
         return holidayMapper.selectByDate(reportDate);
    }

    /**
     * 插入或更新节假日信息
     * @param holiday 节假日实体
     * @return 是否成功
     */
    @Override
    @Transactional
    public Boolean insert(YQHolidayEntity holiday) {

        List<YQHolidayEntity> holidayCalendar = holidayMapper.selectByDate(holiday.getHolidayDate());

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

    /**
     * 查询日期类型
     * @param holidayDate 日期
     * @param queryType 查询类型
     * @return 日期类型
     */
    @Override
    public String queryDateType(LocalDate holidayDate,String queryType) {

        // 判断是住院/门诊
        if (queryType== Constant.TYPE_OUTP){

        }

        if (isHoliday( holidayDate)){
           return Constant.HOLIDAY_IS;
        }else {
            // ❗当前是工作日 且 前一天是节假日/周末
            if(isHoliday(holidayDate.minusDays(1))){
                 return Constant.HOLIDAY_AFTER;
            }
            // ❗当前是工作日 且 后一天是节假日/周末
            if(isHoliday(holidayDate.plusDays(1))){
                return Constant.HOLIDAY_PRE;
            }
        }
        return Constant.HOLIDAY_NOT;
    }


    /**
     * 判断日期是否为 节假日
     * @param targetDate 日期
     * @return 是否为节假日
     */
    private boolean isHoliday(LocalDate targetDate) {
        // 1. 获取所有必需的原始数据
        List<YQHolidayEntity> holidays = holidayMapper.selectByDate(targetDate);

        if (holidays.size() > 0){
            return true;
        }
        return false;

    }

    /**
     * 批量写入节假日信息
     * @param list 节假日实体列表
     * @return 是否成功
     */
    @Transactional
    @Override
    public Boolean batchInsertList(List<YQHolidayEntity> list) {
        PrimaryKeyGenerator pks = new PrimaryKeyGenerator();

        if (list == null || list.isEmpty()) {
            // 如果列表为空，直接返回
            return false;
        }

        for (YQHolidayEntity dto : list) {
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

    /**
     * 作废节假日信息
     * @param holiday 节假日实体
     * @return 是否成功
     */
    @Override
    @Transactional
    public Boolean update(YQHolidayEntity holiday) {
        return holidayMapper.delete(holiday.getSerialNo())>0?true:false;
    }

}
