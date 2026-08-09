package com.etl.job;

import com.etl.service.core.EtlEngine;
import com.etl.service.core.PipelineExecutor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class EtlQuartzJob implements Job {

    @Autowired
    private EtlEngine etlEngine;

    @Autowired
    private PipelineExecutor pipelineExecutor;

    @Override
    public void execute(JobExecutionContext context) {
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();
        String taskCode = dataMap.getString("taskCode");
        boolean isPipeline = dataMap.getBoolean("isPipeline");

        if (taskCode == null || taskCode.trim().isEmpty()) {
            log.error("任务Code为空, 跳过执行");
            return;
        }

        if (isPipeline) {
            log.info("Quartz定时触发管线: {}", taskCode);
            pipelineExecutor.debug(taskCode, 0, true);
        } else {
            log.info("Quartz定时任务触发: {}", taskCode);
            etlEngine.execute(taskCode, "SCHEDULED", "SYSTEM");
        }
    }
}
