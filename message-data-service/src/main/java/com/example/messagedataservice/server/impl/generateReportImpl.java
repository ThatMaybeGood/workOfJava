package com.example.messagedataservice.server.impl;

import com.example.messagedataservice.entity.BusinessException;
import com.example.messagedataservice.model.CashStattisticsMain;
import com.example.messagedataservice.model.HisData;
import com.example.messagedataservice.model.YQCashRegRecord;
import com.example.messagedataservice.server.CommonExecuteProcedureService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Date;


@Service
public class CommonExecuteProcedureImpl implements CommonExecuteProcedureService {

    private static final Logger log = LoggerFactory.getLogger(CommonExecuteProcedureImpl.class);

    @Value("${api.hisdata.url}")
    private  String HIS_DATA_URL ;

    @Value("${api.yqdata.url}")
    private  String YQ_DATA_URL ;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private RestTemplate restTemplate;




    @Override
    public CashStattisticsMain exec(String parm) {
        // 这里是执行存储过程的代码或者是调用平台保留的接口 获取his的数据
        try {
            // 实际的API调用应该放在单独的方法中
            // 这里只是示例代码框架

        } catch (DataAccessException ex) {
            log.error("调用存储过程失败", ex);
            throw new BusinessException("DB_001", "获取员工数量失败", ex);
        }

        return null;
    }




    // 获取his数据
    public HisData getHisDate(Date reportDate) {
        ResponseEntity<HisData> response = restTemplate.getForEntity(HIS_DATA_URL, HisData.class, reportDate);

        if (response.getStatusCode() == HttpStatus.OK) {
            return response.getBody();
        } else {
            log.error("Failed to get HIS data, status: {}", response.getStatusCode());
            throw new RuntimeException("API call failed");
        }
    }


    // 获取YQ数据
    public YQCashRegRecord getYQDate(Date reportDate) {
        ResponseEntity<YQCashRegRecord> response = restTemplate.getForEntity(YQ_DATA_URL, YQCashRegRecord.class, reportDate);

        if (response.getStatusCode() == HttpStatus.OK) {
            return response.getBody();
        } else {
            log.error("Failed to get YQ data, status: {}", response.getStatusCode());
            throw new RuntimeException("API call failed");
        }
    }
}
