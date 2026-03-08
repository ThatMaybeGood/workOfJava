package com.mergedata.server;

import com.mergedata.model.dto.InpReportRequestBody;
import com.mergedata.model.dto.OutpReportRequestBody;
import com.mergedata.model.entity.InpCashMainEntity;
import com.mergedata.model.vo.OutpReportMainVO;

public interface ReportService {

    /**
     * 根据日期查询门诊报表数据
     */
    OutpReportMainVO getOutpReport(OutpReportRequestBody body)  ;


    /**
     * 批量插入门诊报表数据
     */
    Integer insertOutpReport(OutpReportMainVO mainVO);

    /**
     * 根据日期查询住院报表数据
     */
    InpCashMainEntity getInpReport(InpReportRequestBody body)  ;

    /**
     * 批量插入住院报表数据
     */
    Integer insertInpReport(InpCashMainEntity main);

}
