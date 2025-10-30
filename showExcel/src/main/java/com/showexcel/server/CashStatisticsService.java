package com.showexcel.server;

import com.showexcel.dao.CashStatisticsTableDTO;
import com.showexcel.model.CashStatistics;
import com.showexcel.dao.CashStatisticsDTO;

import java.util.List;

/**
 * @author Mine
 * @version 1.0
 * 描述:
 * @date 2025/10/29 15:52
 */



public interface CashStatisticsService {

    /**
     * 获取所有统计数据（包含会计室数据、预约中心数据和统计行）
     */
    List<CashStatisticsDTO> getAllStatistics();

    /**
     * 根据类型获取数据
     */
    List<CashStatistics> getDataByType(Integer type);

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
    CashStatistics calculateGrandTotal(CashStatistics accountingTotal, CashStatistics appointmentTotal);



    List<CashStatisticsTableDTO> getAllStatisticsTable();

}