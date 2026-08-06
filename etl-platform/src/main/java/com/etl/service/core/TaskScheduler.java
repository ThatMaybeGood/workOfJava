package com.etl.service.core;

import com.etl.entity.EtlTaskConfig;
import com.etl.job.EtlQuartzJob;
import com.etl.service.admin.EtlTaskConfigService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

@Slf4j
@Component("etlTaskScheduler")
public class TaskScheduler {

    @Autowired
    private Scheduler scheduler;

    @Autowired
    private EtlTaskConfigService taskConfigService;

    @PostConstruct
    public void init() {
        try {
            List<EtlTaskConfig> tasks = taskConfigService.listEnabledWithCron();
            for (EtlTaskConfig task : tasks) {
                scheduleTask(task);
            }
            log.info("初始化定时任务完成, 共 {} 个任务", tasks.size());
        } catch (Exception e) {
            log.warn("定时任务初始化失败(数据库可能未就绪): {}", e.getMessage());
            log.warn("请在数据源配置完成后重启应用以加载定时任务");
        }
    }

    public void scheduleTask(EtlTaskConfig task) throws SchedulerException {
        if (task.getCronExpr() == null || task.getCronExpr().trim().isEmpty()) {
            log.warn("任务 [{}] 没有配置Cron表达式, 跳过调度", task.getTaskCode());
            return;
        }

        JobKey jobKey = new JobKey(task.getTaskCode(), "ETL");
        TriggerKey triggerKey = new TriggerKey(task.getTaskCode() + "_trigger", "ETL");

        // 如果已存在，先删除
        if (scheduler.checkExists(jobKey)) {
            scheduler.deleteJob(jobKey);
        }

        JobDetail jobDetail = JobBuilder.newJob(EtlQuartzJob.class)
                .withIdentity(jobKey)
                .usingJobData("taskCode", task.getTaskCode())
                .build();

        CronTrigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(triggerKey)
                .withSchedule(CronScheduleBuilder.cronSchedule(task.getCronExpr()))
                .build();

        scheduler.scheduleJob(jobDetail, trigger);
        log.info("任务 [{}] 已加入Quartz调度, Cron: {}", task.getTaskCode(), task.getCronExpr());
    }

    public void pauseTask(String taskCode) throws SchedulerException {
        JobKey jobKey = new JobKey(taskCode, "ETL");
        scheduler.pauseJob(jobKey);
        log.info("任务 [{}] 已暂停", taskCode);
    }

    public void resumeTask(String taskCode) throws SchedulerException {
        JobKey jobKey = new JobKey(taskCode, "ETL");
        scheduler.resumeJob(jobKey);
        log.info("任务 [{}] 已恢复", taskCode);
    }

    public void removeTask(String taskCode) throws SchedulerException {
        JobKey jobKey = new JobKey(taskCode, "ETL");
        scheduler.deleteJob(jobKey);
        log.info("任务 [{}] 已从调度器中移除", taskCode);
    }

    public void reload() throws SchedulerException {
        log.info("开始重新加载所有定时任务...");
        List<EtlTaskConfig> tasks = taskConfigService.listEnabledWithCron();
        int count = 0;
        for (EtlTaskConfig task : tasks) {
            scheduleTask(task);
            count++;
        }
        log.info("重新加载定时任务完成, 共 {} 个任务", count);
    }

    public void triggerTask(String taskCode) throws SchedulerException {
        JobKey jobKey = new JobKey(taskCode, "ETL");
        scheduler.triggerJob(jobKey);
        log.info("任务 [{}] 手动触发", taskCode);
    }
}
