package com.mergedata.server;

import com.mergedata.model.ReportDTO;

import java.util.List;

public interface ReportService {

    /*
     * 根据日期查询所有报表数据
     */
    List<ReportDTO> getAll(String reportDate)  ;

    /*
     * 根据日期插入报表数据
     */
    Boolean insert(String reportDate);


}
