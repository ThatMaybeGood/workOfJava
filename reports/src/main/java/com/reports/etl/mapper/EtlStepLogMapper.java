package com.reports.etl.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.reports.etl.entity.EtlStepLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface EtlStepLogMapper extends BaseMapper<EtlStepLog> {
}