package com.mergedata.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mergedata.model.entity.OutpCashMainEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface InpCashMainMapper extends BaseMapper<OutpCashMainEntity> {
    // 这里继承了 BaseMapper，就自动拥有了增删改查能力
}