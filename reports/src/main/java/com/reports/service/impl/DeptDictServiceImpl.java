package com.reports.service.impl;

import com.reports.dto.request.DeptDictRequest;
import com.reports.dto.response.common.DeptDictItem;
import com.reports.entity.DeptDictEntity;
import com.reports.mapper.DeptDictMapper;
import com.reports.service.DeptDictService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 科室字典服务实现
 */
@Slf4j
@Service
public class DeptDictServiceImpl implements DeptDictService {

    private final DeptDictMapper deptDictMapper;

    @Autowired
    public DeptDictServiceImpl(DeptDictMapper deptDictMapper) {
        this.deptDictMapper = deptDictMapper;
    }

    @Override
    public List<DeptDictItem> queryDeptDict(DeptDictRequest request) {
        log.info("查询科室字典，deptType={}, deptCode={}, deptName={}",
                request.getDeptType(), request.getDeptCode(), request.getDeptName());

        // 默认门诊
        Integer deptType = request.getDeptType() != null ? request.getDeptType() : 0;

        List<DeptDictEntity> entityList = deptDictMapper.queryDeptDict(
                deptType,
                request.getDeptCode(),
                request.getDeptName()
        );

        return entityList.stream()
                .map(this::convertToItem)
                .collect(Collectors.toList());
    }

    private DeptDictItem convertToItem(DeptDictEntity entity) {
        DeptDictItem item = new DeptDictItem();
        item.setDeptCode(entity.getDeptCode());
        item.setDeptName(entity.getDeptName());
        item.setDeptType(mapOutpOrInp(entity.getOutpOrInp()));
        item.setParentCode("");
        item.setLevel(2);
        item.setSortNo(entity.getSerialNo());
        item.setStatus(1);
        return item;
    }

    private Integer mapOutpOrInp(Integer outpOrInp) {
        if (outpOrInp == null) {
            return 2;
        }
        if (outpOrInp == 0) {
            return 0;
        }
        if (outpOrInp == 1) {
            return 1;
        }
        return 2;
    }
}
