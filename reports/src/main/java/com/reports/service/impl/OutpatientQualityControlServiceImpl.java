package com.reports.service.impl;

import com.reports.config.ReportDataConfig;
import com.reports.dto.common.PageResult;
import com.reports.dto.request.OutpatientQualityControlRequest;
import com.reports.dto.response.outpatient.quality.control.*;
import com.reports.service.OutpatientQualityControlService;
import com.reports.util.SeqUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 门诊管理质量控制服务实现
 */
@Slf4j
@Service
public class OutpatientQualityControlServiceImpl implements OutpatientQualityControlService {

    private final ReportDataConfig dataConfig;
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public OutpatientQualityControlServiceImpl(ReportDataConfig dataConfig, JdbcTemplate jdbcTemplate) {
        this.dataConfig = dataConfig;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public OverviewData queryOverview(OutpatientQualityControlRequest request) {
        log.info("查询门诊质量控制概览数据，mode={}", dataConfig.getMode());
        if (dataConfig.isMock()) {
            return queryOverviewMock(request);
        } else if (dataConfig.isJdbc()) {
            return queryOverviewByJdbc(request);
        } else {
            return queryOverviewByMybatisPlus(request);
        }
    }

    @Override
    public PageResult<TableItem> queryTable(OutpatientQualityControlRequest request, Integer page, Integer pageSize) {
        log.info("查询门诊质量控制表格数据，mode={}", dataConfig.getMode());
        if (dataConfig.isMock()) {
            return queryTableMock(request, page, pageSize);
        } else if (dataConfig.isJdbc()) {
            return queryTableByJdbc(request, page, pageSize);
        } else {
            return queryTableByMybatisPlus(request, page, pageSize);
        }
    }

    // ==================== Mock 模式 ====================

    private OverviewData queryOverviewMock(OutpatientQualityControlRequest request) {
        SeqUtil.next();
        OverviewData overview = new OverviewData();
        overview.setEmrUsageRate("95.00%");
        overview.setStandardDiagnosisRate("92.00%");
        overview.setOnTimeRate("88.00%");
        overview.setStopRate("2.00%");
        overview.setChemoRecordRate("98.00%");
        overview.setChemoAdverseRate("0.50%");
        overview.setChemoInfusionRate("99.00%");
        overview.setCriticalValueRate("100.00%");
        overview.setBloodDrawErrorRate("0.10%");
        overview.setSurgeryComplicationRate("1.00%");
        overview.setAdverseEventRate("0.20%");
        return overview;
    }

    private PageResult<TableItem> queryTableMock(OutpatientQualityControlRequest request, Integer page, Integer pageSize) {
        SeqUtil.next();
        List<TableItem> list = new ArrayList<>();
        for (int i = 0; i < pageSize; i++) {
            TableItem item = new TableItem();
            item.setMonth("2024-01");
            item.setEmrUsageRate("95.00%");
            item.setStandardDiagnosisRate("92.00%");
            item.setOnTimeRate("88.00%");
            item.setStopRate("2.00%");
            item.setChemoRecordRate("98.00%");
            item.setChemoAdverseRate("0.50%");
            item.setChemoInfusionRate("99.00%");
            item.setCriticalValueRate("100.00%");
            item.setBloodDrawErrorRate("0.10%");
            item.setSurgeryComplicationRate("1.00%");
            item.setAdverseEventRate("0.20%");
            list.add(item);
        }
        return PageResult.of(list, 55L, page, pageSize);
    }

    // ==================== JdbcTemplate 模式 ====================

    private OverviewData queryOverviewByJdbc(OutpatientQualityControlRequest request) {
        log.info("使用 JdbcTemplate 查询概览数据");
        return queryOverviewMock(request);
    }

    private PageResult<TableItem> queryTableByJdbc(OutpatientQualityControlRequest request, Integer page, Integer pageSize) {
        return queryTableMock(request, page, pageSize);
    }

    // ==================== MyBatis-Plus 模式 ====================

    private OverviewData queryOverviewByMybatisPlus(OutpatientQualityControlRequest request) {
        return queryOverviewMock(request);
    }

    private PageResult<TableItem> queryTableByMybatisPlus(OutpatientQualityControlRequest request, Integer page, Integer pageSize) {
        return queryTableMock(request, page, pageSize);
    }

}
