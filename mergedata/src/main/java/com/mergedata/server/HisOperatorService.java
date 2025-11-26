package com.mergedata.server;

import com.mergedata.dto.YQOperatorDTO;

import java.util.List;

public interface HisOperatorService {
    /*
     * 查询所有历史操作员数据
     */
    List<YQOperatorDTO> findData();

}
