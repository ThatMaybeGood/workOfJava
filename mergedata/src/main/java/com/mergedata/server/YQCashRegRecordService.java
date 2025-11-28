package com.mergedata.server;

import com.mergedata.model.YQCashRegRecordDTO;

import java.util.List;

public interface YQCashRegRecordService {

    List<YQCashRegRecordDTO> findByDate(String reportdate);



}
