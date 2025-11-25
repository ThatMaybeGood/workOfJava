package com.example.messagedataservice.server;

import com.example.messagedataservice.model.HisOperator;

import java.util.List;

public interface HisOperatorService {
    /*
     * 查询所有历史操作员数据
     */
    List<HisOperator> findData();

}
