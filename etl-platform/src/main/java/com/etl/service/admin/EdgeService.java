package com.etl.service.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.etl.entity.EtlPipelineEdge;
import com.etl.mapper.EtlPipelineEdgeMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class EdgeService extends ServiceImpl<EtlPipelineEdgeMapper, EtlPipelineEdge> {

    public List<EtlPipelineEdge> listByPipelineId(Long pipelineId) {
        return list(new QueryWrapper<EtlPipelineEdge>()
                .eq("pipeline_id", pipelineId));
    }

    public void deleteByPipelineId(Long pipelineId) {
        remove(new QueryWrapper<EtlPipelineEdge>()
                .eq("pipeline_id", pipelineId));
    }

    public void deleteByStepId(Long stepId) {
        remove(new QueryWrapper<EtlPipelineEdge>()
                .eq("from_step_id", stepId)
                .or()
                .eq("to_step_id", stepId));
    }
}
