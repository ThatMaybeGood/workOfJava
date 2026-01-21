package com.mergedata.server;

import com.mergedata.model.entity.YQCashRegRecordEntity;

import java.util.List;

public interface YQCashService {

    List<YQCashRegRecordEntity> findByDate(String reportdate);


}
