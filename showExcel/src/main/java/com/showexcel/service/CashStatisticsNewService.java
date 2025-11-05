package com.showexcel.service;

import com.showexcel.response.CashStatisticsResponse;

public interface CashStatisticsNewService {

    /**
     * 根据日期获取数据
     */
    CashStatisticsResponse getAllStatisticsTableByDate(String date);


}
