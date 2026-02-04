package com.mergedata.server.impl;

import com.mergedata.mapper.CashMapper;
import com.mergedata.model.entity.YQCashRegRecordEntity;
import com.mergedata.server.YQCashService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;


@Service
public class CashServiceImpl implements YQCashService {

    // 注入 Mapper 接口
    @Autowired
    private CashMapper cashMapper;

    /**
     * 根据日期查询资金记录
     * @param reportdate 日期
     * @return 资金记录列表
     */
    @Override
    public List<YQCashRegRecordEntity> findByDate(String reportdate) {
        return cashMapper.selectByDate(LocalDate.parse(reportdate));
    }
}