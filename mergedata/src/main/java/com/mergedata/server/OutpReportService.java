package com.mergedata.server;

import com.mergedata.model.dto.OutpReportRequestBody;
import com.mergedata.model.entity.OutpCashMainEntity;
import com.mergedata.model.vo.OutpReportMainVO;

import java.time.LocalDate;

public interface OutpReportService {

    /**
     * 根据请求参数查询门诊报表数据,只查询有效的
     */
     OutpCashMainEntity findByRequestBody(OutpReportRequestBody requestBody)  ;


    /**
     * 根据日期查询门诊报表数据
     */
    OutpCashMainEntity findByDate(LocalDate date)  ;



    /**
     * 根据流水号主键查询门诊报表数据
     */
    OutpCashMainEntity findByPk(String serialNo)  ;


    /*
     * 单条写入
     */
    void insert(OutpCashMainEntity entity);

    /*
     * 作废原有在写入
     */
    Boolean insertOrUpdate(OutpCashMainEntity entity);


    /*
     * 作废
     */
    Boolean update(OutpCashMainEntity entity);

    /**
     * 通过日期，作废对应日期的门诊报表数据
     */
    Boolean updateByDate(LocalDate date, String totalFlag);

    /*
     * 作废
     */
    Boolean delete(OutpCashMainEntity entity);

}
