package com.mergedata.server.impl;

import com.mergedata.mapper.YQCashMapper;
import com.mergedata.model.YQCashRegRecord;
import com.mergedata.server.YQCashService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class YQCashServiceImpl implements YQCashService {


    @Autowired
    private YQCashMapper yqStoredProcedureDao;

    /**
     * 业务方法：根据日期查询现金记录，并进行一些业务处理。
     * * @param date 查询日期字符串
     *
     * @return 经过处理的现金记录列表
     */
    @Override
    public List<YQCashRegRecord> findByDate(String date) {
        try {
            // 1. 调用 DAO 方法获取存储过程返回的结果列表
            List<YQCashRegRecord> rawRecords = yqStoredProcedureDao.getMultParams(Collections.singletonMap("A_REPORTDATE", date));

            // 3. 返回最终处理结果
            return rawRecords;

        } catch (Exception e) {
            log.error("获取YQ数据异常", e);
            return new ArrayList<>();
        }
    }

}
