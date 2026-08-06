package com.etl.service.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.etl.entity.EtlTaskProgress;
import com.etl.mapper.EtlTaskProgressMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EtlTaskProgressService extends ServiceImpl<EtlTaskProgressMapper, EtlTaskProgress> {

    public EtlTaskProgress getByExecutionId(String executionId) {
        return getOne(new QueryWrapper<EtlTaskProgress>()
                .eq("execution_id", executionId));
    }
}
