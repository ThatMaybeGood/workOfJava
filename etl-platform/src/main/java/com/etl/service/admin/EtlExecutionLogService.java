package com.etl.service.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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

    /** 全量执行日志，按开始时间倒序，limit<=0 返回全部（走 Oracle 方言分页插件） */
    public List<EtlExecutionLog> listAll(int limit) {
        QueryWrapper<EtlExecutionLog> wrapper = new QueryWrapper<EtlExecutionLog>()
                .orderByDesc("start_time");
        if (limit > 0) {
            return page(new Page<>(1, limit), wrapper).getRecords();
        }
        return list(wrapper);
    }

    public long countByStatus(String status) {
        return count(new QueryWrapper<EtlExecutionLog>().eq("status", status));
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
