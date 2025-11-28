package com.mergedata.server.impl;

import com.mergedata.dao.YQStoredProcedureDao;
import com.mergedata.mapper.YQOperatorMapper;
import com.mergedata.model.YQOperatorDTO;
import com.mergedata.server.HisOperatorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class HisOperatorServiceImpl implements HisOperatorService {

    @Autowired
    YQStoredProcedureDao yqStoredProcedureDao;

    @Autowired
    YQOperatorMapper yqOperatorMapper;


    @Override
    public List<YQOperatorDTO> findData() {
        try {
            // 调用通用方法，传入过程名和 Mapper
            List<YQOperatorDTO> getAllProducts = yqStoredProcedureDao.executeQueryNoParam(
                    "GET_ALL_PRODUCTS",  // 存储过程名称
                    yqOperatorMapper     // 对应的 RowMapper Bean
            );
            return getAllProducts ;
        } catch (Exception e) {
            log.error("获取操作员数据异常", e);
            return new ArrayList<>();
        }
}
}