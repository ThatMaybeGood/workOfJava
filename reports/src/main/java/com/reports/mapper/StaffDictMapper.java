package com.reports.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.reports.entity.StaffDictEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 人员字典 Mapper
 */
@Mapper
public interface StaffDictMapper extends BaseMapper<StaffDictEntity> {

    /**
     * 查询在职人员
     *
     * @param deptCode  科室代码（可选）
     * @param staffName 人员姓名（支持模糊匹配，可选）
     * @return 人员列表
     */
    List<StaffDictEntity> queryStaffList(@Param("deptCode") String deptCode,
                                         @Param("staffName") String staffName);
}
