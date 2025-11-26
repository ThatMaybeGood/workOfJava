package com.mergedata.server;

import com.mergedata.dto.YQCashRegRecordDTO;

import java.time.LocalDate;
import java.util.List;

public interface YQCashRegRecordService {

    List<YQCashRegRecordDTO> findByDate(LocalDate reportDate);



}
