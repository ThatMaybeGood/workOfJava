package com.mergedata.server;

import com.mergedata.dto.ReportRequestBody;
import com.mergedata.model.Report;

import java.util.List;

public interface ReportService {

    /*
     * 根据日期查询所有报表数据
     */
    List<Report> getAll(String reportDate)  ;

    /*
     * 组装请求类型获取
     */
    List<Report> getAll(ReportRequestBody body)  ;

    /*
     * 根据日期插入报表数据
     */
    Boolean batchInsert(List<Report> list);


}
