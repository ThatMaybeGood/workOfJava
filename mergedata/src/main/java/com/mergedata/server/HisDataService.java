package com.mergedata.server;

import com.mergedata.model.dto.external.HisIncomeResponseDTO;

import java.util.List;

public interface HisDataService {
    /*
     * 根据日期查询his数据
     */
    List<HisIncomeResponseDTO> findByDate(String reportDate);

}
