package com.etl.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.etl.entity.EtlTaskConfig;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface EtlTaskConfigMapper extends BaseMapper<EtlTaskConfig> {
}
