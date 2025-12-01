package com.mergedata.server.impl;

import com.mergedata.mapper.YQHolidayMapper;
import com.mergedata.model.YQHolidayCalendarDTO;
import com.mergedata.server.YQHolidayService;
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

        for (YQHolidayCalendarDTO dto : list) {
            Map<String, Object> map = new HashMap<>();
            map.put("A_REPORTDATE", dto.getHolidayDate());
            map.put("A_HOLIDAYFLAG", dto.getMonth());
            yqHolidayMapper.insertMultParams(map);
        }
        return true;
    }
}
