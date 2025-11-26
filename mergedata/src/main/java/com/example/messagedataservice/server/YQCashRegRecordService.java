package com.example.messagedataservice.server;

import com.example.messagedataservice.dto.YQCashRegRecordDTO;

import java.time.LocalDate;
import java.util.List;

public interface YQCashRegRecordService {

    List<YQCashRegRecordDTO> findByDate(LocalDate reportDate);



}
