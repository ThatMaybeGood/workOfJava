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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
            List<YQOperator> rawRecords = yqOperatorMapper.getOperatorsWithNoParams();

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
    @Override
    public Boolean batchInsert(List<YQOperator> yqOperatorList) {

        List<HisIncomeDTO> byDate = hisdata.findByDate("2023-01-30");

        for (HisIncomeDTO dto : byDate) {

            Map<String,Object> map = new HashMap<>();
            map.put("A_OPERATOR_NO",dto.getOperatorNo());
            map.put("A_OPERATOR_NAME",dto.getOperatorName());
            map.put("A_ISVALID","1");
            map.put("A_TYPE", ReqConstant.SP_TYPE_INSERT);


            // 1. 调用 DAO 方法获取存储过程返回的结果列表
            Boolean b = yqOperatorMapper.insertOperatorsMultParams(map);

        }
        return true;

    }

}


