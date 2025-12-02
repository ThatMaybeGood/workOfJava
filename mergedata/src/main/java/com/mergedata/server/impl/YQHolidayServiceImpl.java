package com.mergedata.server.impl;

import com.mergedata.constants.ReqConstant;
import com.mergedata.mapper.YQHolidayMapper;
import com.mergedata.model.YQHolidayCalendarDTO;
import com.mergedata.server.YQHolidayService;
import com.mergedata.util.PrimaryKeyGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
    public List<YQHolidayCalendarDTO> findByDate(String date) {
        try {
            return yqHolidayMapper.getMultParams(Collections.singletonMap("A_REPORTDATE", date));
        } catch (Exception e) {
            log.error("获取YQ数据异常", e);
            return null;
        }
    }

    @Override
    public Boolean insert(List<YQHolidayCalendarDTO> list) {
        // 生成唯一序列号，此处使用 PrimaryKeyGenerator 类生成主键

        PrimaryKeyGenerator pk = new PrimaryKeyGenerator();

        String serialNo = pk.generateKey();

        for (YQHolidayCalendarDTO dto : list) {

            Map<String, Object> map = new HashMap<>();
            map.put("A_SERIALNO", serialNo);
            map.put("A_REPORT_DATE", dto.getHolidayDate());
            map.put("A_HOLIDAY_TYPE", dto.getHolidayType());
            map.put("A_HOLIDAY_MONTH", dto.getMonth());
            map.put("A_HOLIDAY_YEAR", dto.getYear());
            map.put("A_TYPE", ReqConstant.SP_TYPE_INSERT);

            yqHolidayMapper.insertMultParams(map);
        }
        return true;
    }
}
