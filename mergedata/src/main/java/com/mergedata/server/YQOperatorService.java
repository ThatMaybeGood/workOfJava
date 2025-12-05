package com.mergedata.server;

import com.mergedata.model.YQOperator;

import java.util.List;

public interface YQOperatorService {
    /*
     * 查询所有历史操作员数据
     */
    List<YQOperator> findAll();


    List<YQOperator> findByID(YQOperator operator);

    /*
     * 单条插入操作员数据
     */
    Boolean insert(YQOperator yqOperators);

    /*
     * 批量插入操作员数据
     */
    Boolean batchInsert(List<YQOperator> yqOperators);

    /*
     * 作废
     */
    Boolean update(YQOperator yqOperator);

}
