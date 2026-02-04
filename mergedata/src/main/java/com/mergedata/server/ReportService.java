package com.mergedata.server;

import com.mergedata.model.dto.InpReportRequestBody;
import com.mergedata.model.dto.OutpReportRequestBody;
import com.mergedata.model.entity.InpCashMainEntity;
import com.mergedata.model.vo.InpReportVO;
import com.mergedata.model.vo.OutpReportVO;

import java.util.List;

public interface ReportService {

    /*
     * 根据日期查询门诊报表数据
     */
    List<OutpReportVO> getOutpReport(OutpReportRequestBody body)  ;

    /*
     * 批量插入门诊报表数据
     */
    Integer insertOutpReport(List<OutpReportVO> list);

    /**
     * 根据日期查询住院报表数据
     */
    InpCashMainEntity getInpReport(InpReportRequestBody body)  ;

    /*
     * 批量插入住院报表数据
     */
    Integer insertInpReport(InpCashMainEntity main);

}
