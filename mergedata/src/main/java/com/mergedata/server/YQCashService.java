package com.mergedata.server;

import com.mergedata.model.YQCashRegRecordDTO;

import java.util.List;

public interface YQCashService {

    List<YQCashRegRecordDTO> findByDate(String reportdate);

    Boolean insert(List<YQCashRegRecordDTO> list);


}
