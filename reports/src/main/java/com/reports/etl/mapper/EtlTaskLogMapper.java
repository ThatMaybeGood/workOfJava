package com.reports.etl.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.reports.etl.entity.EtlTaskLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface EtlTaskLogMapper extends BaseMapper<EtlTaskLog> {
}