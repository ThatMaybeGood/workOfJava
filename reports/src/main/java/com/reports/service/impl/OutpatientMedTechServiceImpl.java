package com.reports.service.impl;

import com.reports.config.ReportDataConfig;
import com.reports.dto.common.PageResult;
import com.reports.dto.request.OutpatientMedTechRequest;
import com.reports.dto.response.outpatient.med.tech.*;
import com.reports.service.OutpatientMedTechService;
import com.reports.util.SeqUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 医技统计服务实现
 */
@Slf4j
@Service
public class OutpatientMedTechServiceImpl implements OutpatientMedTechService {

    private final ReportDataConfig dataConfig;
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public OutpatientMedTechServiceImpl(ReportDataConfig dataConfig, JdbcTemplate jdbcTemplate) {
        this.dataConfig = dataConfig;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public OverviewData queryOverview(OutpatientMedTechRequest request) {
        log.info("查询医技统计概览数据，mode={}", dataConfig.getMode());
        if (dataConfig.isMock()) {
            return queryOverviewMock(request);
        } else if (dataConfig.isJdbc()) {
            return queryOverviewByJdbc(request);
        } else {
            return queryOverviewByMybatisPlus(request);
        }
    }

    @Override
    public PageResult<TableItem> queryTable(OutpatientMedTechRequest request, Integer page, Integer pageSize) {
        log.info("查询医技统计表格数据，mode={}", dataConfig.getMode());
        if (dataConfig.isMock()) {
            return queryTableMock(request, page, pageSize);
        } else if (dataConfig.isJdbc()) {
            return queryTableByJdbc(request, page, pageSize);
        } else {
            return queryTableByMybatisPlus(request, page, pageSize);
        }
    }

    // ==================== Mock 模式 ====================

    private OverviewData queryOverviewMock(OutpatientMedTechRequest request) {
        SeqUtil.next();
        OverviewData overview = new OverviewData();
        overview.setCheckCount(5236);
        overview.setOnTimeRate("92.00%");
        overview.setWaitTime("25分钟");
        overview.setAvgWaitLate("5分钟");
        overview.setAvgReportTime("30分钟");
        return overview;
    }

    private PageResult<TableItem> queryTableMock(OutpatientMedTechRequest request, Integer page, Integer pageSize) {
        SeqUtil.next();
        List<TableItem> list = new ArrayList<>();
        for (int i = 0; i < pageSize; i++) {
            TableItem item = new TableItem();
            item.setDeptName("放射科" + (i + 1));
            item.setCheckCount(500 + i * 10);
            item.setOnTimeRate("90.00%");
            item.setWaitTime(25.0 + i);
            item.setAvgWaitLate(5.0 + i * 0.5);
            item.setAvgReportTime(30.0 + i);
            list.add(item);
        }
        return PageResult.of(list, 55L, page, pageSize);
    }

    // ==================== JdbcTemplate 模式 ====================

    private OverviewData queryOverviewByJdbc(OutpatientMedTechRequest request) {
        log.info("使用 JdbcTemplate 查询概览数据");
        return queryOverviewMock(request);
    }

    private PageResult<TableItem> queryTableByJdbc(OutpatientMedTechRequest request, Integer page, Integer pageSize) {
        return queryTableMock(request, page, pageSize);
    }

    // ==================== MyBatis-Plus 模式 ====================

    private OverviewData queryOverviewByMybatisPlus(OutpatientMedTechRequest request) {
        return queryOverviewMock(request);
    }

    private PageResult<TableItem> queryTableByMybatisPlus(OutpatientMedTechRequest request, Integer page, Integer pageSize) {
        return queryTableMock(request, page, pageSize);
    }

}
