package com.mergedata.server.impl;

import com.mergedata.constants.ReqConstant;
import com.mergedata.mapper.YQOperatorMapper;
import com.mergedata.model.HisIncomeDTO;
import com.mergedata.model.YQOperator;
import com.mergedata.server.HisDataService;
import com.mergedata.server.YQOperatorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Service
public class YQOperatorServiceImpl implements YQOperatorService {


    @Autowired
    YQOperatorMapper yqOperatorMapper;

    @Autowired
    HisDataService hisdata;

    @Override
    public List<YQOperator> findData() {
        try {

            // 1. 调用 DAO 方法获取存储过程返回的结果列表
            List<YQOperator> rawRecords = yqOperatorMapper.getNoParams();

            // 2. Service 层业务逻辑处理
            if (rawRecords.isEmpty()) {
                log.info("查询日期无记录返回。");
                return rawRecords;
            }
            log.info("成功获取 " + rawRecords.size() + " 条记录。");
            // 3. 返回最终处理结果
            return rawRecords;


        } catch (Exception e) {
            log.error("获取YQ数据异常", e);
            return new ArrayList<>();
        }
    }

    /**
     * 批量插入操作员数据
     *
     * @param yqOperatorList
     * @return
     */
    /**
     * @Transactional 开启事务。
     * 事务的开始：方法调用前。
     * 事务的结束：方法成功执行后自动 COMMIT；如果抛出运行时异常，自动 ROLLBACK。
     */
        @Transactional
        @Override
        public Boolean batchInsert(List<YQOperator> yqOperatorList) {

            List<HisIncomeDTO> byDate = hisdata.findByDate("2023-01-30");

            // ❗ 写入之前需要传入一次存储过程调用作废数据
            Map<String,Object> mapUpdate = new HashMap<>();
            mapUpdate.put("A_OPERATOR_NO","22");
            mapUpdate.put("A_OPERATOR_NAME","12");
            mapUpdate.put("A_TYPE", ReqConstant.SP_TYPE_UPDATE);
            yqOperatorMapper.insertMultParams(mapUpdate);


            for (HisIncomeDTO dto : byDate) {

                Map<String,Object> map = new HashMap<>();
                map.put("A_OPERATOR_NO",dto.getOperatorNo());
                map.put("A_OPERATOR_NAME",dto.getOperatorName());
                map.put("A_TYPE", ReqConstant.SP_TYPE_INSERT);


                // 1. 调用 DAO 方法获取存储过程返回的结果列表
                Boolean b = yqOperatorMapper.insertMultParams(map);

                // ❗ 如果存储过程返回失败（b=false），应该抛出运行时异常
                // 这样 Spring 才能捕获到错误并触发 ROLLBACK
                if (!b) {
                    throw new RuntimeException("存储过程调用失败，数据同步中断。");
                }
            }
            return true;

        }

}


