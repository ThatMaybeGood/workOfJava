package com.reports.etl.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.reports.etl.entity.EtlTask;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface EtlTaskMapper extends BaseMapper<EtlTask> {
}