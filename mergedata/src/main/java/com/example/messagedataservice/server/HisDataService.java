package com.example.messagedataservice.server;

import com.example.messagedataservice.dto.HisIncomeDTO;

import java.time.LocalDate;
import java.util.List;

public interface HisDataService {
    /*
     * 根据日期查询历史数据
     */
    List<HisIncomeDTO> findByDate(LocalDate reportDate);

}
