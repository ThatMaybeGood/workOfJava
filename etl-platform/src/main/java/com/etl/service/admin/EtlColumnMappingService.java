package com.etl.service.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.etl.entity.EtlColumnMapping;
import com.etl.mapper.EtlColumnMappingMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class EtlColumnMappingService extends ServiceImpl<EtlColumnMappingMapper, EtlColumnMapping> {

    public List<EtlColumnMapping> listByTaskCode(String taskCode) {
        return list(new QueryWrapper<EtlColumnMapping>()
                .eq("task_code", taskCode)
                .eq("enabled", "Y")
                .orderByAsc("mapping_order"));
    }

    public List<EtlColumnMapping> listPrimaryKeys(String taskCode) {
        return list(new QueryWrapper<EtlColumnMapping>()
                .eq("task_code", taskCode)
                .eq("is_primary_key", "Y")
                .eq("enabled", "Y"));
    }
}
