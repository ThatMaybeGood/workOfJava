package com.example.messagedataservice.server;

import com.example.messagedataservice.dto.ReportDTO;

import java.time.LocalDate;
import java.util.List;

public interface generateReportService {

    // 生成报表，返回操作员编号和对应的DTO对象
    List<ReportDTO> getAll(LocalDate reportDate);

    // 插入数据到数据库，返回是否成功
    Boolean insert(LocalDate reportDate);


}
