package com.mergedata.server.impl;

import com.mergedata.mapper.CashMapper;
import com.mergedata.model.YQCashRegRecord;
import com.mergedata.server.YQCashService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class CashServiceImpl implements YQCashService {

    // 注入 Mapper 接口
    @Autowired
    private CashMapper cashMapper;

    /**
     * 根据日期查询资金记录
     */

    @Override
    public List<YQCashRegRecord> findByDate(String reportdate) {
        return cashMapper.selectByDate(reportdate);
    }
}