package com.etl.service.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.etl.entity.EtlPipelineStep;
import com.etl.mapper.EtlPipelineStepMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class StepService extends ServiceImpl<EtlPipelineStepMapper, EtlPipelineStep> {

    public List<EtlPipelineStep> listByPipelineId(Long pipelineId) {
        return list(new QueryWrapper<EtlPipelineStep>()
                .eq("pipeline_id", pipelineId)
                .orderByAsc("order_index"));
    }

    public void deleteByPipelineId(Long pipelineId) {
        remove(new QueryWrapper<EtlPipelineStep>()
                .eq("pipeline_id", pipelineId));
    }
}
