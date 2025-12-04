package com.mergedata.mapper;

import com.mergedata.model.YQCashRegRecord;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface CashMapper {
    /*
     * 根据日期查询现金报表记录
     */
    List<YQCashRegRecord> selectByDate(LocalDate date);


}
