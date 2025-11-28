package com.mergedata.server.impl;

import com.mergedata.mapper.YQCashRegRecordMapper;
import com.mergedata.model.YQCashRegRecordDTO;
import com.mergedata.server.YQCashRegRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class YQCashRegRecordServiceImpl implements YQCashRegRecordService {


    @Autowired
    private YQCashRegRecordMapper yqStoredProcedureDao;

    /**
     * 业务方法：根据日期查询现金记录，并进行一些业务处理。
     * * @param date 查询日期字符串
     * @return 经过处理的现金记录列表
     */
    @Override
    public List<YQCashRegRecordDTO> findByDate(String date) {
        try {

            // 1. 调用 DAO 方法获取存储过程返回的结果列表
            List<YQCashRegRecordDTO> rawRecords =
                    yqStoredProcedureDao.getCashRegRecordsByDate(date);

            // 2. Service 层业务逻辑处理
            if (rawRecords.isEmpty()) {
                System.out.println("查询日期 [" + date + "] 无记录返回。");
                return rawRecords;
            }

            System.out.println("成功获取 " + rawRecords.size() + " 条记录。");

            // 示例业务逻辑：过滤掉 email 地址为空的记录
            List<YQCashRegRecordDTO> validRecords = rawRecords.stream()
                    .filter(record -> record.getOperatType() != null && !record.getOperatType().isEmpty())
                    .collect(Collectors.toList());

            System.out.println("经过过滤，有效记录数量为: " + validRecords.size());

            // 3. 返回最终处理结果
            return validRecords;


        } catch (Exception e) {
            log.error("获取YQ数据异常", e);
            return new ArrayList<>();
        }    }
}
