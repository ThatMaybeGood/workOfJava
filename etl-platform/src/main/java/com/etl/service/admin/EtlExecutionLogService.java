package com.etl.service.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.etl.entity.EtlExecutionLog;
import com.etl.mapper.EtlExecutionLogMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class EtlExecutionLogService extends ServiceImpl<EtlExecutionLogMapper, EtlExecutionLog> {

    public List<EtlExecutionLog> listByTaskCode(String taskCode) {
        return list(new QueryWrapper<EtlExecutionLog>()
                .eq("task_code", taskCode)
                .orderByDesc("start_time"));
    }

    public EtlExecutionLog getByExecutionId(String executionId) {
        return getOne(new QueryWrapper<EtlExecutionLog>()
                .eq("execution_id", executionId));
    }

    public List<EtlExecutionLog> listRunning() {
        return list(new QueryWrapper<EtlExecutionLog>()
                .eq("status", "RUNNING")
                .orderByDesc("start_time"));
    }
}
