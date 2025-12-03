package com.mergedata.server;

import com.mergedata.model.YQCashRegRecord;

import java.util.List;

public interface YQCashService {

    List<YQCashRegRecord> findByDate(String reportdate);


}
