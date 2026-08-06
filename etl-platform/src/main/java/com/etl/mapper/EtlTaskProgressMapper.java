package com.etl.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.etl.entity.EtlTaskProgress;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface EtlTaskProgressMapper extends BaseMapper<EtlTaskProgress> {
}
