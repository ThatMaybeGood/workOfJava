package com.mergedata.server;

import com.mergedata.model.dto.InpAuditRequestBody;
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


    /**
     * 查询住院审核报表数据（只查已有数据，不生成）
     */
    InpCashMainEntity getInpAuditReport(InpReportRequestBody body);

    /**
     * 保存住院报表审核结果（通过/不通过/取消审核）
     */
    Integer saveInpAuditReport(InpAuditRequestBody body);

}
