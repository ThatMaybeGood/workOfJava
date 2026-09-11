package com.reports.service.impl;

import com.reports.entity.StaffDictEntity;
import com.reports.mapper.StaffDictMapper;
import com.reports.service.StaffDictService;
import com.reports.util.SeqUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 人员字典服务实现
 */
@Slf4j
@Service
public class StaffDictServiceImpl implements StaffDictService {

    @Autowired
    private StaffDictMapper staffDictMapper;

    @Override
    public List<StaffDictEntity> queryStaffList(String deptCode, String staffName) {
        SeqUtil.next();
        return staffDictMapper.queryStaffList(deptCode, staffName);
    }
}
