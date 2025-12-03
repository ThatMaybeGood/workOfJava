package com.mergedata.server.impl;

import com.mergedata.constants.ReqConstant;
import com.mergedata.mapper.YQHolidayMapper;
import com.mergedata.model.YQHolidayCalendar;
import com.mergedata.server.YQHolidayService;
import com.mergedata.util.PrimaryKeyGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class YQHolidayServiceImpl implements YQHolidayService {

    @Autowired
    YQHolidayMapper yqHolidayMapper;

    @Override
    public List<YQHolidayCalendar> findAll() {
        try {
            return yqHolidayMapper.getNoParams();
        } catch (Exception e) {
            log.error("获取YQ数据异常", e);
            return null;
        }
    }

    @Override
    public List<YQHolidayCalendar> findByDate() {
        return Collections.emptyList();
    }

    @Override
    @Transactional
    public Boolean batchInsert(YQHolidayCalendar holiday) {
        PrimaryKeyGenerator pks = new PrimaryKeyGenerator();

        Boolean b = yqHolidayMapper.insertMultParams(buildParams(pks.generateKey(), holiday, ReqConstant.SP_TYPE_INSERT));
        if (!b) {
            throw new RuntimeException("单条写入存储过程调用失败，数据同步中断。");
        }
        return true;
    }

    @Override
    @Transactional
    public Boolean delete(YQHolidayCalendar holiday) {
        PrimaryKeyGenerator pks = new PrimaryKeyGenerator();

        Boolean b = yqHolidayMapper.insertMultParams(buildParams(pks.generateKey(), holiday, ReqConstant.SP_TYPE_UPDATE));
        if (!b) {
            throw new RuntimeException("删除调用存储过程调用失败，数据同步中断。");
        }
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
        // 执行作废操作
        executeInvalidate(pks.generateKey());

        for (YQHolidayCalendar dto : list) {

            Boolean b = yqHolidayMapper.insertMultParams(buildParams(pks.generateKey(), dto, ReqConstant.SP_TYPE_INSERT));

            if (!b) {
                throw new RuntimeException("批量写入存储过程调用失败，数据同步中断。");
            }
        }
        return true;
    }



    /**
     * 封装：构建存储过程所需的参数 Map
     * @param pk 主键生成器
     * @param dto 当前 DTO 对象 (作废时可传入 null)
     * @param type 操作类型 (INSERT/UPDATE/INVALIDATE)
     * @return 封装好的 Map
     */
    private Map<String, Object> buildParams(String pk,
            YQHolidayCalendar dto,
            String type) {
        Map<String, Object> maps = new HashMap<>();

        // ❗ 优化：只有 INSERT/UPDATE 时才传入日期数据，作废时传入 null 减少冗余
        maps.put("A_SERIALNO", pk);
        maps.put("A_REPORT_DATE", dto.getHolidayDate());
        maps.put("A_HOLIDAY_TYPE", dto.getHolidayType());
        maps.put("A_HOLIDAY_MONTH", dto.getMonth());
        maps.put("A_HOLIDAY_YEAR", dto.getYear());
        // 填充操作类型
        maps.put("A_TYPE", type);

        return maps;
    }

    /**
     * 封装：执行作废操作
     */
    private void executeInvalidate(String pk) {
        // ❗ 优化：调用封装方法，只传入作废必需的类型和序列号
        Map<String, Object> invalidateMap = buildParams(pk,null, ReqConstant.SP_TYPE_UPDATE);
        // 假设您的存储过程能识别 A_TYPE=UPDATE 时执行全局作废
        yqHolidayMapper.insertMultParams(invalidateMap);
    }
}
