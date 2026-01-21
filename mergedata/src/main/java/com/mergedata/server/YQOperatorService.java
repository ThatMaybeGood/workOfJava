package com.mergedata.server;

import com.mergedata.model.entity.YQOperatorEntity;

import java.util.List;

public interface YQOperatorService {
    /*
     * 查询所有历史操作员数据
     */
    List<YQOperatorEntity> findAll();


    List<YQOperatorEntity> findByID(YQOperatorEntity operator);

    /*
     * 单条插入操作员数据
     */
    Boolean insert(YQOperatorEntity yqOperatorsEntity);

    /*
     * 批量插入操作员数据
     */
    Boolean batchInsert(List<YQOperatorEntity> yqOperatorEntities);

    /*
     * 作废
     */
    Boolean delete(YQOperatorEntity yqOperatorEntity);
    /*
     * 作废
     */
    Boolean update(YQOperatorEntity yqOperatorEntity);

}
