package com.mergedata.server;

import com.mergedata.model.Report;

import java.util.List;

public interface ReportService {

    /*
     * 根据日期查询所有报表数据
     */
    List<Report> getAll(String reportDate)  ;

    /*
     * 根据日期插入报表数据
     */
    Boolean batchInsert(List<Report> list);


}
