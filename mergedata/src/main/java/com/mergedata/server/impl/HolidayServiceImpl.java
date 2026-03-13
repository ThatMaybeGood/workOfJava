package com.mergedata.server.impl;

import com.alibaba.druid.sql.dialect.db2.visitor.DB2ASTVisitor;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.mergedata.constants.Constant;
import com.mergedata.mapper.HolidayMapper;
import com.mergedata.model.dto.CommonRequestBody;
import com.mergedata.model.entity.YQHolidayEntity;
import com.mergedata.model.vo.YQHolidayCalendarVO;
import com.mergedata.server.YQHolidayService;
import com.mergedata.util.OracleBatchUtil;
import com.mergedata.util.PrimaryKeyGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class HolidayServiceImpl implements YQHolidayService {

    @Autowired
    HolidayMapper holidayMapper;

    @Override
    public List<YQHolidayEntity> findAll() {
        return Db.lambdaQuery(YQHolidayEntity.class)
                .list();
    }


    @Override
    public List<YQHolidayEntity> findByDate(LocalDate reportDate) {
         return Db.lambdaQuery(YQHolidayEntity.class)
                .eq(YQHolidayEntity::getHolidayDate, reportDate)
                 .orderByAsc(YQHolidayEntity::getHolidayYear,YQHolidayEntity::getHolidayMonth,YQHolidayEntity::getHolidayDate)
                .list();
    }

    @Override
    public List<YQHolidayEntity> findByYear(Integer year) {
        return Db.lambdaQuery(YQHolidayEntity.class)
                .eq(YQHolidayEntity::getHolidayYear, year)
                .orderByAsc(YQHolidayEntity::getHolidayMonth,YQHolidayEntity::getHolidayDate)
                .list();
     }

    @Override
    public List<YQHolidayEntity> findByYearMonth(CommonRequestBody body) {
        Integer year = body.getExtendParams1() != null ? Integer.parseInt(body.getExtendParams1()) : null;
        Integer month = body.getExtendParams2() != null ? Integer.parseInt(body.getExtendParams2()) : null;

        return Db.lambdaQuery(YQHolidayEntity.class)
                .eq(year != null , YQHolidayEntity::getHolidayYear, year)
                .eq(month != null , YQHolidayEntity::getHolidayMonth, month)
                .orderByAsc(YQHolidayEntity::getHolidayDate)
                .list();
    }


    /**
     * 插入或更新节假日信息
     * @param holiday 节假日实体
     * @return 是否成功
     */
    @Override
    @Transactional
    public Boolean insert(YQHolidayEntity holiday) {

        if(findByDate(holiday.getHolidayDate()).size() > 0 ){
            update(holiday);
        }

        PrimaryKeyGenerator pks = new PrimaryKeyGenerator();
        holiday.setSerialNo(pks.generateKey());

        holiday.setHolidayMonth(String.valueOf(holiday.getHolidayDate().getMonthValue()));
        holiday.setHolidayYear(String.valueOf(holiday.getHolidayDate().getYear()));

        try {
            return Db.save(holiday);
        } catch (Exception e) {
            throw new RuntimeException("节假日数据保存失败！！！");
        }
    }

    /**
     * 查询日期类型
     * @param holidayDate 日期
     * @param queryType 查询类型
     * @return 日期类型
     */
    @Override
    public String queryDateType(LocalDate holidayDate,String queryType) {

//        // 判断是住院/门诊
//        if (queryType== Constant.TYPE_OUTP){
//
//        }

        if (isHoliday( holidayDate)){
            //判断日期是否节假日 且是月末最后一天
            if(holidayDate.getDayOfMonth() == holidayDate.lengthOfMonth()) {
                return Constant.HOLIDAY_MONTH_LASTDAY;
            }

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
     * 前端查询日期时候，返回对应日期的节假日类型以及是否汇总
     * @param currentDate
     * @param queryType
     * @return
     */
    @Override
    public YQHolidayCalendarVO queryHolidayTotalType(LocalDate currentDate, String queryType,String totalFlag) {
        YQHolidayCalendarVO vo = new YQHolidayCalendarVO();
        String type = queryDateType(currentDate, queryType);
        LocalDate minDate = findMinBacktrackDate(currentDate);

        vo.setHolidayDate(currentDate);
        vo.setQueryType(queryType);
        vo.setHolidayType(type);

        //是否需要添加汇总标志 ，排除对应汇总时候，但是又不符合汇总条件的情况

        //type为 2 和 4 时 汇总
        if (totalFlag.equals(Constant.YES) && (type.equals(Constant.HOLIDAY_AFTER) || type.equals(Constant.HOLIDAY_MONTH_LASTDAY))){
            // 汇总的标题
            String totalTitle = minDate.format(DateTimeFormatter.ofPattern("yyyy.MM.dd"))
                    +"-"
                    +currentDate.format(DateTimeFormatter.ofPattern("yyyy.MM.dd"))
                    +" "+ Constant.OUTP_HOLIDAY_TOTAL_TITLE;

            vo.setMisDate(minDate);
            vo.setTotalFlag(Constant.NO);
            vo.setTotalTitle(totalTitle);
        }else {
            vo.setTotalFlag(Constant.YES);
            vo.setTotalTitle(currentDate.format(DateTimeFormatter.ofPattern("yyyy.MM.dd")) +" "+ Constant.HOLIDAY_NOT_TOTAL_TITLE);
        }


        return vo;
    }


    /**
     * 判断日期是否为 节假日
     * @param targetDate 日期
     * @return 是否为节假日
     */
    private boolean isHoliday(LocalDate targetDate) {
        // 1. 获取所有必需的原始数据
        List<YQHolidayEntity> holidays = findByDate(targetDate);

        if (holidays.size() > 0){
            return true;
        }
        return false;

    }

    /**
     * 批量写入节假日信息
     * @param entities 节假日实体列表
     * @return 是否成功
     */
    @Transactional
    @Override
    public Boolean batchInsertList(List<YQHolidayEntity> entities) {
        try {
//            OracleBatchUtil.fastBatchInsert(jdbcTemplate, list, "mpp_cash_reg_holiday", 2000,true);
            return Db.saveBatch(entities) ;
        } catch (Exception e) {
            log.error("保存节假日信息失败：", e);
            throw new RuntimeException("保存节假日信息失败：" + e.getMessage());
        }
    }

    /**
     * 作废节假日信息
     * @param entity 节假日实体
     * @return 是否成功
     */
    @Override
    @Transactional
    public Boolean update(YQHolidayEntity entity) {
        //作废标志
        entity.setValidFlag("0");
        return Db.updateById(entity);
    }

    /**
     * 删除节假日信息
     * @param entity 节假日实体
     * @return 是否成功
     */
    @Transactional
    @Override
    public Boolean delete(YQHolidayEntity entity) {
        return Db.removeById(entity);
    }




    /**
     * 查找最近的工作日或月初 1 号，作为回溯截止日期
     */
    @Override
    public LocalDate findMinBacktrackDate(LocalDate localDate) {

        Set<LocalDate> holidaySet = findByYear(localDate.getYear()).stream()
                .map(YQHolidayEntity::getHolidayDate).collect(Collectors.toSet());


        LocalDate current = localDate.minusDays(1);
        // 限制最大回溯 30 天防止死循环
        for (int i = 0; i < 30; i++) {
            // 如果是工作日，这就是我们要找的边界
            if (!holidaySet.contains(current)) {
                return current;
            }
            // 如果是月初 1 号，也必须停止
            if (current.getDayOfMonth() == 1) {
                return current;
            }
            current = current.minusDays(1);
        }
        return current;
    }

    /**
     * 判断是否符合特殊节假日需要进行回溯汇总计算
     * @param localDate 日期
     * @param totalFlag 汇总标
     * @return
     * 0  不是汇总查询                正常计算
     * 1  是汇总查询，且是特殊节假日     需要进行回溯汇总计算
     * 2  是汇总查询，但是不是特殊节假日  直接返回空列表
     */
    @Override
    public Integer isSpecialHolidaySum(LocalDate localDate, String totalFlag) {
             /*
            先判断是否是汇总  还是单独查询
            1、先判断是否是汇总标志
                1、1  如果是汇总标志，再判断是否是特殊节假日
                1、2  是汇总，但是不符合 特殊节假日  直接返回空列表
            */

            String holidayType = queryDateType(localDate, Constant.TYPE_OUTP);

            if (Constant.YES.equals(totalFlag)) {
                if (Constant.HOLIDAY_AFTER.equals(holidayType) || Constant.HOLIDAY_MONTH_LASTDAY.equals(holidayType)) {
                    return 1;
                } else {
                    return 2;
                }
            }
            return 0;
        }

}
