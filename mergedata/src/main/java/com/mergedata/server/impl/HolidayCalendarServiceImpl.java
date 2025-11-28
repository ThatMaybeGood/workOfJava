package com.mergedata.server.impl;

import com.mergedata.dao.YQStoredProcedureDao;
import com.mergedata.mapper.YQHolidayCalendarMapper;
import com.mergedata.model.YQHolidayCalendarDTO;
import com.mergedata.server.HolidayCalendarService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class HolidayCalendarServiceImpl implements HolidayCalendarService {
    @Autowired
    YQStoredProcedureDao yqStoredProcedureDao;

    @Autowired
    YQHolidayCalendarMapper yqHolidayCalendarMapper;

    @Override
    public List<YQHolidayCalendarDTO> findByDate(String date) {
            try {
                List<YQHolidayCalendarDTO> getAllProducts = yqStoredProcedureDao.executeQueryNoParam(
                        "GET_ALL_PRODUCTS",  // 存储过程名称
                        yqHolidayCalendarMapper     // 对应的 RowMapper Bean
                );

                // 调用通用方法，传入过程名和 Mapper
                return getAllProducts;

            } catch (Exception e) {
                log.error("获取节假日日历异常", e);
                return new ArrayList<>();
            }
        }
}
