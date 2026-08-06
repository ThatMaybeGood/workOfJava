package com.etl.service.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.etl.entity.EtlTaskConfig;
import com.etl.mapper.EtlTaskConfigMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class EtlTaskConfigService extends ServiceImpl<EtlTaskConfigMapper, EtlTaskConfig> {

    public EtlTaskConfig getByTaskCode(String taskCode) {
        return getOne(new QueryWrapper<EtlTaskConfig>()
                .eq("task_code", taskCode));
    }

    public List<EtlTaskConfig> listEnabled() {
        return list(new QueryWrapper<EtlTaskConfig>()
                .eq("enabled", "Y"));
    }

    public List<EtlTaskConfig> listEnabledWithCron() {
        return list(new QueryWrapper<EtlTaskConfig>()
                .eq("enabled", "Y")
                .isNotNull("cron_expr")
                .ne("cron_expr", ""));
    }
}
