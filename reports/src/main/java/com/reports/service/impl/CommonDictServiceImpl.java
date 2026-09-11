package com.reports.service.impl;

import com.reports.entity.CommonDictEntity;
import com.reports.mapper.CommonDictMapper;
import com.reports.service.CommonDictService;
import com.reports.util.SeqUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 通用字典服务实现
 */
@Slf4j
@Service
public class CommonDictServiceImpl implements CommonDictService {

    @Autowired
    private CommonDictMapper commonDictMapper;

    @Override
    public List<CommonDictEntity> queryDictList(String dictType) {
        SeqUtil.next();
        return commonDictMapper.queryByType(dictType);
    }

    @Override
    public int addDict(CommonDictEntity entity) {
        SeqUtil.next();
        if (entity.getDictCode() == null || entity.getDictCode().isEmpty()) {
            entity.setDictCode(entity.getDictName());
        }
        return commonDictMapper.insertDict(entity);
    }

    @Override
    public int deleteDict(Long id) {
        SeqUtil.next();
        return commonDictMapper.deleteById(id);
    }
}
