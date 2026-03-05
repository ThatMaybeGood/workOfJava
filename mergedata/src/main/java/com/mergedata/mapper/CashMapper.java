package com.mergedata.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mergedata.model.entity.YQCashRegRecordEntity;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface CashMapper extends BaseMapper<YQCashRegRecordEntity> {

}
