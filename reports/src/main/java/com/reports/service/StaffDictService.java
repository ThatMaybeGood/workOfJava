package com.reports.service;

import com.reports.entity.StaffDictEntity;

import java.util.List;

/**
 * 人员字典服务
 */
public interface StaffDictService {

    /**
     * 查询在职人员
     *
     * @param deptCode  科室代码（可选）
     * @param staffName 人员姓名（支持模糊匹配，可选）
     * @return 人员列表
     */
    List<StaffDictEntity> queryStaffList(String deptCode, String staffName);
}
