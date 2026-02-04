package com.mergedata.server;

import com.mergedata.model.entity.YQCashRegRecordEntity;

import java.util.List;

public interface YQCashService {

    /**
     * 根据日期查询现金登记记录
     * @param reportdate 日期
     * @return 现金登记记录列表
     */
    List<YQCashRegRecordEntity> findByDate(String reportdate);

}
