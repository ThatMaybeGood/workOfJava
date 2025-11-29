package com.mergedata.server;

import com.mergedata.model.YQOperator;

import java.util.List;

public interface YQOperatorService {
    /*
     * 查询所有历史操作员数据
     */
    List<YQOperator> findData();
    /*
     * 批量插入操作员数据
     */
    Boolean batchInsert(List<YQOperator> yqOperators);

}
