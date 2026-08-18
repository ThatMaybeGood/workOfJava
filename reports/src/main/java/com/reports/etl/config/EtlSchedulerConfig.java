package com.reports.etl.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * ETL 调度配置
 * 仅 ETL 模块使用，不影响现有报表功能。
 */
@Configuration
@EnableScheduling
public class EtlSchedulerConfig {

    @Bean("etlTaskScheduler")
    public ThreadPoolTaskScheduler etlTaskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(5);
        scheduler.setThreadNamePrefix("etl-sched-");
        scheduler.setWaitForTasksToCompleteOnShutdown(false);
        scheduler.initialize();
        return scheduler;
    }
}