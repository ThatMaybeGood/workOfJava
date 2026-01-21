package com.mergedata.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mergedata.model.entity.CashStattisticsMainEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CashStattisticsMainMapper extends BaseMapper<CashStattisticsMainEntity> {
    // 这里继承了 BaseMapper，就自动拥有了增删改查能力
}