package com.etl.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.etl.entity.EtlPipelineStep;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface EtlPipelineStepMapper extends BaseMapper<EtlPipelineStep> {
}
