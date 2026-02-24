package com.mergedata.server;

import com.mergedata.model.entity.YQOperatorEntity;

import java.util.List;

public interface YQOperatorService {
    /*
     * 查询所有历史操作员数据
     */
    List<YQOperatorEntity> findAll();

    /*
     * 根据ID查询操作员
     */
    List<YQOperatorEntity> findByID(YQOperatorEntity operator);

    /*
     * 根据门诊/住院查询  门诊 0  住院 1
     */
    List<YQOperatorEntity> findByCategory(String category);


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
