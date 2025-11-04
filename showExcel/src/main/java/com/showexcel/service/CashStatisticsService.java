package com.showexcel.service;

import com.showexcel.dto.CashStatisticsTableDTO;
import com.showexcel.model.CashStatistics;
import com.showexcel.dto.CashStatisticsDTO;

import java.util.Date;
import java.util.List;

/**
 * @author Mine
 * @version 1.0
 * 描述:
 * @date 2025/10/29 15:52
 */



public interface CashStatisticsService {



    /**
     * 获取会计室数据
     */
    List<CashStatistics> getAccountingData();

    /**
     * 获取预约中心数据
     */
    List<CashStatistics> getAppointmentData();

    /**
     * 根据ID获取单条数据
     */
    CashStatistics getById(Integer id);

    /**
     * 添加数据
     */
    CashStatistics add(CashStatistics item);

    /**
     * 更新数据
     */
    CashStatistics update(Integer id, CashStatistics item);

    /**
     * 删除数据
     */
    boolean delete(Integer id);

    /**
     * 计算会计室合计
     */
    CashStatistics calculateAccountingTotal(List<CashStatistics> data);

    /**
     * 计算预约合计
     */
    CashStatistics calculateAppointmentTotal(List<CashStatistics> data);

    /**
     * 计算总计
     */
    CashStatistics calculateGrandTotal(List<CashStatistics> data);



    List<CashStatisticsTableDTO> getAllStatisticsTable();
    /**
     * 根据日期获取数据
     */
    CashStatisticsTableDTO getAllStatisticsTableByDate(String date);


}