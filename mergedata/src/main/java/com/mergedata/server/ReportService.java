package com.mergedata.server;

import com.mergedata.model.dto.ReportRequestBody;
import com.mergedata.model.vo.OutpReportVO;

import java.util.List;

public interface ReportService {

    /*
     * 根据日期查询所有报表数据
     */
    List<OutpReportVO> getAll(String reportDate)  ;

    /*
     * 组装请求类型获取
     */
    List<OutpReportVO> getAll(ReportRequestBody body)  ;

    /*
     * 根据日期插入报表数据
     */
    Boolean batchInsert(List<OutpReportVO> list);


}
