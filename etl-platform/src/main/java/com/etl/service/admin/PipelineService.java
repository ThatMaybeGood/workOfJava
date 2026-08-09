package com.etl.service.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.etl.entity.EtlPipeline;
import com.etl.mapper.EtlPipelineMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class PipelineService extends ServiceImpl<EtlPipelineMapper, EtlPipeline> {

    public EtlPipeline getByPipelineCode(String pipelineCode) {
        return getOne(new QueryWrapper<EtlPipeline>()
                .eq("pipeline_code", pipelineCode));
    }

    public List<EtlPipeline> listEnabled() {
        return list(new QueryWrapper<EtlPipeline>()
                .eq("enabled", "Y"));
    }

    public List<EtlPipeline> listEnabledWithCron() {
        return list(new QueryWrapper<EtlPipeline>()
                .eq("enabled", "Y")
                .isNotNull("cron_expr")
                .ne("cron_expr", ""));
    }
}
