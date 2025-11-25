package com.example.messagedataservice.server;

import com.example.messagedataservice.model.YQCashRegRecord;

import java.time.LocalDate;
import java.util.List;

public interface YQCashRegRecordService {

    List<YQCashRegRecord> findByDate(LocalDate reportDate);



}
