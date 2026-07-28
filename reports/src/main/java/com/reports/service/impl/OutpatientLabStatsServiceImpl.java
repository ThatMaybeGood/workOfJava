package com.reports.service.impl;

import com.reports.config.ReportDataConfig;
import com.reports.dto.request.OutpatientLabStatsRequest;
import com.reports.dto.response.outpatient.lab.stats.OverviewData;
import com.reports.entity.LabStatsOvEntity;
import com.reports.mapper.LabStatsMapper;
import com.reports.service.OutpatientLabStatsService;
import com.reports.util.SeqUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * 检验统计服务实现
 */
@Slf4j
@Service
public class OutpatientLabStatsServiceImpl implements OutpatientLabStatsService {

    private final ReportDataConfig dataConfig;
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    private LabStatsMapper labStatsMapper;

    @Autowired
    public OutpatientLabStatsServiceImpl(ReportDataConfig dataConfig, JdbcTemplate jdbcTemplate) {
        this.dataConfig = dataConfig;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public OverviewData queryOverview(OutpatientLabStatsRequest request) {
        log.info("查询检验统计概览数据，mode={}", dataConfig.getMode());
        if (dataConfig.isMock()) {
            return queryOverviewMock(request);
        } else if (dataConfig.isJdbc()) {
            return queryOverviewByJdbc(request);
        } else {
            return queryOverviewByMybatisPlus(request);
        }
    }

    // ==================== Mock 模式 ====================

    private OverviewData queryOverviewMock(OutpatientLabStatsRequest request) {
        SeqUtil.next();
        OverviewData overview = new OverviewData();
        overview.setBloodCollection(3256);
        overview.setBloodEfficiency("95.00%");
        overview.setLabEfficiency("92.00%");
        return overview;
    }

    // ==================== JdbcTemplate 模式 ====================

    private OverviewData queryOverviewByJdbc(OutpatientLabStatsRequest request) {
        log.info("使用 JdbcTemplate 查询概览数据");
        return queryOverviewMock(request);
    }

    // ==================== MyBatis-Plus 模式 ====================

    private OverviewData queryOverviewByMybatisPlus(OutpatientLabStatsRequest request) {
        try {
            LabStatsOvEntity entity = labStatsMapper.queryOverview(request.getStartDate(), request.getEndDate());
            return buildOverviewData(entity);
        } catch (Exception e) {
            log.warn("查询检验统计概览失败", e);
            return new OverviewData();
        }
    }

    // ==================== 工具方法 ====================

    private OverviewData buildOverviewData(LabStatsOvEntity entity) {
        if (entity == null) return new OverviewData();
        OverviewData dto = new OverviewData();
        dto.setBloodCollection(entity.getBloodCollection());
        dto.setBloodEfficiency(entity.getBloodEfficiency());
        dto.setLabEfficiency(entity.getLabEfficiency());
        return dto;
    }

}
