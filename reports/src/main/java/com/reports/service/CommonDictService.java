package com.reports.service;

import com.reports.entity.CommonDictEntity;

import java.util.List;

/**
 * 通用字典服务
 */
public interface CommonDictService {

    /**
     * 按类型查询启用的字典项
     *
     * @param dictType 字典类型（为空查询全部）
     * @return 字典列表
     */
    List<CommonDictEntity> queryDictList(String dictType);

    /**
     * 新增字典项
     *
     * @param entity 字典项
     * @return 影响行数
     */
    int addDict(CommonDictEntity entity);

    /**
     * 删除字典项
     *
     * @param id 主键ID
     * @return 影响行数
     */
    int deleteDict(Long id);
}
