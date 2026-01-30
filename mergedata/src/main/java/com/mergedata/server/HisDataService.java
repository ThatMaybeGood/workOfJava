package com.mergedata.server;

import com.mergedata.model.dto.external.HisInpCashReportResponseDTO;
import com.mergedata.model.dto.external.HisOutpIncomeResponseDTO;

import java.util.List;

public interface HisDataService {
    /*
     * 根据日期查询his门诊结算数据
     */
    List<HisOutpIncomeResponseDTO> findByDateOutp(String reportDate);


    /*
     * 根据日期查询his住院结算数据
     */
    List<HisInpCashReportResponseDTO> findByDateInp(String reportDate);

}
