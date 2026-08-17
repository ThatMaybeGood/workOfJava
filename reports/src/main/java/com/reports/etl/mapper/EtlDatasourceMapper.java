package com.reports.etl.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.reports.etl.entity.EtlDatasource;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface EtlDatasourceMapper extends BaseMapper<EtlDatasource> {
}