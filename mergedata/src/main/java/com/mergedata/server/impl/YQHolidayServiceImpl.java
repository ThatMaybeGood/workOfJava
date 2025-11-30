package com.mergedata.server.impl;

import com.mergedata.mapper.YQHolidayMapper;
import com.mergedata.model.YQHolidayCalendarDTO;
import com.mergedata.server.YQHolidayService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
        Map<String, Object> params = new HashMap<>();
        params.put("A_REPORT_DATE", date);
        return  yqHolidayMapper.getYQHlidayList(params);
    }
}
