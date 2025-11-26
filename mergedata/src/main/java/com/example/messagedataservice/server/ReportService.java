package com.example.messagedataservice.server;

import com.example.messagedataservice.dto.ReportDTO;

import java.time.LocalDate;
import java.util.List;

public interface ReportService {

    /*
     * 根据日期查询所有报表数据
     */
    List<ReportDTO> getAll(LocalDate reportDate);

    /*
     * 根据日期插入报表数据
     */
    Boolean insert(LocalDate reportDate);


}
