package com.mergedata.server.impl;

import com.mergedata.constants.ReqConstant;
import com.mergedata.mapper.YQHolidayMapper;
import com.mergedata.model.YQHolidayCalendarDTO;
import com.mergedata.server.YQHolidayService;
import com.mergedata.util.PrimaryKeyGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class YQHolidayServiceImpl implements YQHolidayService {

    @Autowired
    YQHolidayMapper yqHolidayMapper;

    @Override
    public List<YQHolidayCalendarDTO> findByDate() {
        try {
            return yqHolidayMapper.getNoParams();
        } catch (Exception e) {
            log.error("获取YQ数据异常", e);
            return null;
        }
    }

    @Transactional
    @Override
    public Boolean insert(List<YQHolidayCalendarDTO> list) {
        // 生成唯一序列号，此处使用 PrimaryKeyGenerator 类生成主键

        PrimaryKeyGenerator pk = new PrimaryKeyGenerator();


        //❗  写入之前需要传入一次存储过程调用作废数据
        Map<String,Object> map = new HashMap<>();
        String serialNo = pk.generateKey();
        map.put("A_SERIALNO", serialNo);
        map.put("A_REPORT_DATE", "");
        map.put("A_HOLIDAY_TYPE", "");
        map.put("A_HOLIDAY_MONTH", "");
        map.put("A_HOLIDAY_YEAR", "");
        map.put("A_TYPE", ReqConstant.SP_TYPE_UPDATE);
        yqHolidayMapper.insertMultParams(map);


        for (YQHolidayCalendarDTO dto : list) {
            String serialNos = pk.generateKey();
            Map<String, Object> maps = new HashMap<>();
            maps.put("A_SERIALNO", serialNos);
            maps.put("A_REPORT_DATE", dto.getHolidayDate());
            maps.put("A_HOLIDAY_TYPE", dto.getHolidayType());
            maps.put("A_HOLIDAY_MONTH", dto.getMonth());
            maps.put("A_HOLIDAY_YEAR", dto.getYear());
            maps.put("A_TYPE", ReqConstant.SP_TYPE_INSERT);

            yqHolidayMapper.insertMultParams(maps);
        }
        return true;
    }
}
