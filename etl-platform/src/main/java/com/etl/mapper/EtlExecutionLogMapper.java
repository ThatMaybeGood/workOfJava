package com.etl.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.etl.entity.EtlExecutionLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface EtlExecutionLogMapper extends BaseMapper<EtlExecutionLog> {
}
