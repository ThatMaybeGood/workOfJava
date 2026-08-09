package com.etl.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.etl.entity.EtlPipeline;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface EtlPipelineMapper extends BaseMapper<EtlPipeline> {
}
