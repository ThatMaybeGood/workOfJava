package com.reports.service;

import com.reports.dto.request.PreHospOperRequest;
import com.reports.entity.PreHospOperEntity;

import java.util.List;

/**
 * 院前操作员维护服务
 */
public interface PreHospOperService {

    /** 维护列表(姓名经HIS users回显) */
    List<PreHospOperEntity> queryList();

    /** 候选人员搜索(模糊, 排除已维护, 1起分页) */
    List<PreHospOperEntity> searchStaff(String keyword, int page, int size);

    /** 候选人员总数(同搜索条件) */
    int countStaff(String keyword);

    /** 添加操作员(已维护的重复添加返回0) */
    int addOper(PreHospOperRequest request);

    /** 删除操作员 */
    int removeOper(Long id);

    /** 设置是否有效 */
    int setValid(Long id, Integer validFlag);
}
