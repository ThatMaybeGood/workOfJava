package com.reports.task;

import com.reports.mapper.OutpatientFinanceMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

/**
 * 人次日汇总维护：滚动重算最近7天 + 补齐缺失历史日期（幂等，先删后插）。
 * 触发: 启动60s后首跑(补缺口) + 每日凌晨3点(cron可配, 须在ETL抽数完成后)。
 * 当天人次由查询侧 UNION 实时算，不入本表。
 */
@Slf4j
@Component
public class VisitDaySummaryTask {

    /** 滚动重算天数：覆盖 ETL 补数/重抽的滞后窗口 */
    private static final int ROLLBACK_DAYS = 7;

    private final OutpatientFinanceMapper financeMapper;
    private final boolean taskEnabled;

    public VisitDaySummaryTask(OutpatientFinanceMapper financeMapper,
                               @Value("${reports.visit-summary.task-enabled:true}") boolean taskEnabled) {
        this.financeMapper = financeMapper;
        this.taskEnabled = taskEnabled;
    }

    /** 启动60s后跑一次补缺口（fixedDelay取最大值即只跑一次； 例行调度见 refreshDaily） */
    @Scheduled(initialDelay = 60_000, fixedDelay = Long.MAX_VALUE)
    public void refreshOnBoot() {
        refresh();
    }

    /** 每日凌晨3点例行（ETL完成后滚动重算近7天） */
    @Scheduled(cron = "${reports.visit-summary.cron:0 0 3 * * ?}")
    public void refreshDaily() {
        refresh();
    }

    private void refresh() {
        if (!taskEnabled) {
            log.info("人次日汇总自动维护未开启(reports.visit-summary.task-enabled=false), 跳过");
            return;
        }
        try {
            LocalDate yesterday = LocalDate.now().minusDays(1);
            for (int i = 0; i < ROLLBACK_DAYS; i++) {
                refreshDay(java.sql.Date.valueOf(yesterday.minusDays(i)));
            }
            List<Date> missing = financeMapper.selectMissingSummaryDates();
            for (Date day : missing) {
                refreshDay(day);
            }
            log.info("人次日汇总刷新完成: 滚动{}天, 补齐{}天", ROLLBACK_DAYS, missing.size());
        } catch (Exception e) {
            log.error("人次日汇总刷新失败(下次调度自动重试)", e);
        }
    }

    private void refreshDay(Date day) {
        financeMapper.deleteVisitDay(day);
        financeMapper.insertVisitDay(day);
    }
}
