package com.etl.service.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.etl.entity.EtlStepColumnMapping;
import com.etl.mapper.EtlStepColumnMappingMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class StepColumnMappingService extends ServiceImpl<EtlStepColumnMappingMapper, EtlStepColumnMapping> {

    public List<EtlStepColumnMapping> listByStepId(Long stepId) {
        return list(new QueryWrapper<EtlStepColumnMapping>()
                .eq("step_id", stepId)
                .eq("enabled", "Y")
                .orderByAsc("mapping_order"));
    }

    public List<EtlStepColumnMapping> listPrimaryKeys(Long stepId) {
        return list(new QueryWrapper<EtlStepColumnMapping>()
                .eq("step_id", stepId)
                .eq("is_primary_key", "Y")
                .eq("enabled", "Y"));
    }
}
