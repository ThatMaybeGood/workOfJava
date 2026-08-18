package com.reports.etl.service.scheduler;

import com.reports.etl.entity.EtlTask;
import com.reports.etl.service.core.EtlEngine;
import com.reports.etl.service.core.EtlMetaDao;
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
    private final EtlMetaDao metaDao;
    private final TaskScheduler taskScheduler;

    private final Map<Long, CronTrigger> cronTriggers = new ConcurrentHashMap<>();
    private final Map<Long, ScheduledFuture<?>> scheduledFutures = new ConcurrentHashMap<>();
    private final Set<Long> runningTasks = ConcurrentHashMap.newKeySet();

    public EtlScheduler(EtlEngine etlEngine, EtlMetaDao metaDao, TaskScheduler taskScheduler) {
        this.etlEngine = etlEngine;
        this.metaDao = metaDao;
        this.taskScheduler = taskScheduler;
    }

    @PostConstruct
    public void init() {
        // 启动时加载启用的定时任务
        List<EtlTask> tasks = metaDao.listTasks();
        if (tasks != null) {
            for (EtlTask task : tasks) {
                if (task.getEnabled() != null && task.getEnabled() == 1 && task.getCron() != null && !task.getCron().isEmpty()) {
                    scheduleTask(task);
                }
            }
        }
        log.info("EtlScheduler 初始化完成，加载定时任务 {} 个", scheduledFutures.size());
    }

    public void scheduleTask(EtlTask task) {
        if (task == null || task.getCron() == null || task.getCron().isEmpty()) return;
        Long taskId = task.getId();
        // 取消旧的
        ScheduledFuture<?> old = scheduledFutures.remove(taskId);
        if (old != null) old.cancel(false);

        CronTrigger trigger = new CronTrigger(task.getCron());
        ScheduledFuture<?> future = taskScheduler.schedule(
                () -> executeSafely(taskId, "SCHEDULED"), trigger);
        cronTriggers.put(taskId, trigger);
        scheduledFutures.put(taskId, future);
        log.info("任务已调度: taskId={} cron={}", taskId, task.getCron());
    }

    public void unscheduleTask(Long taskId) {
        ScheduledFuture<?> future = scheduledFutures.remove(taskId);
        if (future != null) future.cancel(false);
        cronTriggers.remove(taskId);
        runningTasks.remove(taskId);
    }

    public boolean isRunning(Long taskId) {
        return runningTasks.contains(taskId);
    }

    /**
     * 手动触发一次任务（触发后真实执行完整链路）
     */
    public Map<String, Object> triggerManually(Long taskId, String triggerType) {
        if (runningTasks.contains(taskId)) {
            Map<String, Object> running = new LinkedHashMap<>();
            running.put("status", "RUNNING");
            running.put("message", "任务正在执行中，请勿重复触发");
            return running;
        }
        runningTasks.add(taskId);
        long start = System.currentTimeMillis();
        try {
            log.info("手动触发任务: taskId={} triggerType={}", taskId, triggerType);
            Map<String, Object> result = new LinkedHashMap<>();
            EtlTask task = metaDao.getTask(taskId);
            if (task == null) {
                result.put("status", "FAILED");
                result.put("message", "任务不存在");
                return result;
            }
            com.reports.etl.entity.EtlTaskLog taskLog = etlEngine.execute(taskId,
                    triggerType != null ? triggerType : "MANUAL");
            result.put("status", "SUCCESS".equals(taskLog.getStatus()) ? "SUCCESS" : "FAILED");
            result.put("message", "任务执行完成");
            result.put("logId", taskLog.getId());
            result.put("extractedRows", taskLog.getExtractedRows());
            result.put("writtenRows", taskLog.getWrittenRows());
            result.put("errorMsg", taskLog.getErrorMsg());
            result.put("durationMs", System.currentTimeMillis() - start);
            return result;
        } catch (Exception e) {
            log.error("手动触发任务失败 taskId={}", taskId, e);
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("status", "FAILED");
            result.put("message", e.getMessage());
            result.put("durationMs", System.currentTimeMillis() - start);
            return result;
        } finally {
            runningTasks.remove(taskId);
        }
    }

    private void executeSafely(Long taskId, String triggerType) {
        if (runningTasks.contains(taskId)) return;
        runningTasks.add(taskId);
        try {
            etlEngine.execute(taskId, triggerType);
        } catch (Exception e) {
            log.error("定时任务执行异常 taskId={}", taskId, e);
        } finally {
            runningTasks.remove(taskId);
        }
    }
}