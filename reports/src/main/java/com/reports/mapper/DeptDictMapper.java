package com.reports.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.reports.entity.DeptDictEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 科室字典 Mapper
 */
@Mapper
public interface DeptDictMapper extends BaseMapper<DeptDictEntity> {

    /**
     * 根据科室类型、科室代码、科室名称查询科室字典
     *
     * @param deptType 科室类型：0 门诊，1 住院，2 其他
     * @param deptCode 科室编码（可选，精确匹配）
     * @param deptName 科室名称（可选，模糊匹配）
     * @return 科室字典列表
     */
    List<DeptDictEntity> queryDeptDict(@Param("deptType") Integer deptType,
                                       @Param("deptCode") String deptCode,
                                       @Param("deptName") String deptName);
}
