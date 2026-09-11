package com.reports.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.reports.entity.CommonDictEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 通用字典 Mapper
 */
@Mapper
public interface CommonDictMapper extends BaseMapper<CommonDictEntity> {

    /**
     * 按类型查询启用的字典项
     *
     * @param dictType 字典类型（为空查询全部）
     * @return 字典列表
     */
    List<CommonDictEntity> queryByType(@Param("dictType") String dictType);

    /**
     * 新增字典项（主键取 seq_tr_reports）
     *
     * @param entity 字典项
     * @return 影响行数
     */
    int insertDict(CommonDictEntity entity);
}
