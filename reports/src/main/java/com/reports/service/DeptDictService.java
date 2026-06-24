package com.reports.service;

import com.reports.dto.request.DeptDictRequest;
import com.reports.dto.response.common.DeptDictItem;

import java.util.List;

/**
 * 科室字典服务接口
 */
public interface DeptDictService {

    /**
     * 查询科室字典列表
     *
     * @param request 查询参数
     * @return 科室字典项列表
     */
    List<DeptDictItem> queryDeptDict(DeptDictRequest request);
}
