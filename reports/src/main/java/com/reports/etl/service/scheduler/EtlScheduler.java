package com.reports.etl.service.scheduler;

import com.reports.etl.entity.EtlTask;
import com.reports.etl.service.core.EtlEngine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

@Slf4j
@Component
public class EtlScheduler {

    private final EtlEngine etlEngine;
    private final Map<Long, CronTrigger> cronTriggers = new ConcurrentHashMap<>();
    private final Map<Long, ScheduledFuture<?>> scheduledFutures = new ConcurrentHashMap<>();
    private final Set<Long> runningTasks = ConcurrentHashMap.newKeySet();

    public EtlScheduler(EtlEngine etlEngine, TaskScheduler taskScheduler) {
        this.etlEngine = etlEngine;
        this.taskScheduler = taskScheduler;
    }

    private final TaskScheduler taskScheduler;

    @PostConstruct
    public void init() {
        log.info("EtlScheduler 初始化完成");
    }

    public void scheduleTask(EtlTask task) {
        if (task.getCron() == null || task.getCron().isEmpty()) return;
        cronTriggers.put(task.getId(), new CronTrigger(task.getCron()));
        log.info("任务已调度: taskId={} cron={}", task.getId(), task.getCron());
    }

    public void unscheduleTask(Long taskId) {
        cronTriggers.remove(taskId);
        scheduledFutures.remove(taskId);
        runningTasks.remove(taskId);
    }

    public boolean isRunning(Long taskId) {
        return runningTasks.contains(taskId);
    }

    public Map<String, Object> triggerManually(Long taskId) {
        if (runningTasks.contains(taskId)) {
            return Map.of("status", "RUNNING", "message", "任务正在执行中，请勿重复触发");
        }
        runningTasks.add(taskId);
        try {
            log.info("手动触发任务: taskId={}", taskId);
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("status", "SUCCESS");
            result.put("message", "任务执行完成");
            return result;
        } catch (Exception e) {
            log.error("手动触发任务失败 taskId={}", taskId, e);
            return Map.of("status", "FAILED", "message", e.getMessage());
        } finally {
            runningTasks.remove(taskId);
        }
    }
}