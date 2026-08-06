package com.etl.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.etl.entity.EtlColumnMapping;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface EtlColumnMappingMapper extends BaseMapper<EtlColumnMapping> {
}
