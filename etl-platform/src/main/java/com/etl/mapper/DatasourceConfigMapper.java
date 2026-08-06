package com.etl.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.etl.entity.DatasourceConfig;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DatasourceConfigMapper extends BaseMapper<DatasourceConfig> {
}
