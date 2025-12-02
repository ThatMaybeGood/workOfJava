package com.mergedata.server;

import com.mergedata.model.HisIncomeDTO;

import java.util.List;

public interface HisDataService {
    /*
     * 根据日期查询历史数据
     */
    List<HisIncomeDTO> findByDate(String reportDate);

}
