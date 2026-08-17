package com.reports.etl.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.reports.etl.entity.EtlProcConfig;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface EtlProcConfigMapper extends BaseMapper<EtlProcConfig> {
}