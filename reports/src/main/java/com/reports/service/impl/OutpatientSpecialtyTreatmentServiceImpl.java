package com.reports.service.impl;

import com.reports.config.ReportDataConfig;
import com.reports.dto.common.PageResult;
import com.reports.dto.request.OutpatientSpecialtyTreatmentRequest;
import com.reports.dto.response.outpatient.specialty.treatment.*;
import com.reports.service.OutpatientSpecialtyTreatmentService;
import com.reports.util.SeqUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 专科治疗量统计服务实现
 */
@Slf4j
@Service
public class OutpatientSpecialtyTreatmentServiceImpl implements OutpatientSpecialtyTreatmentService {

    private final ReportDataConfig dataConfig;
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public OutpatientSpecialtyTreatmentServiceImpl(ReportDataConfig dataConfig, JdbcTemplate jdbcTemplate) {
        this.dataConfig = dataConfig;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public OverviewData queryOverview(OutpatientSpecialtyTreatmentRequest request) {
        log.info("查询专科治疗量概览数据，mode={}", dataConfig.getMode());
        if (dataConfig.isMock()) {
            return queryOverviewMock(request);
        } else if (dataConfig.isJdbc()) {
            return queryOverviewByJdbc(request);
        } else {
            return queryOverviewByMybatisPlus(request);
        }
    }

    @Override
    public PageResult<TableItem> queryTable(OutpatientSpecialtyTreatmentRequest request, Integer page, Integer pageSize) {
        log.info("查询专科治疗量表格数据，mode={}", dataConfig.getMode());
        if (dataConfig.isMock()) {
            return queryTableMock(request, page, pageSize);
        } else if (dataConfig.isJdbc()) {
            return queryTableByJdbc(request, page, pageSize);
        } else {
            return queryTableByMybatisPlus(request, page, pageSize);
        }
    }

    // ==================== Mock 模式 ====================

    private OverviewData queryOverviewMock(OutpatientSpecialtyTreatmentRequest request) {
        SeqUtil.next();
        OverviewData overview = new OverviewData();
        overview.setTreatmentCount(3256);
        overview.setTreatmentAmount(856200.50);
        overview.setPatientCount(1200);
        return overview;
    }

    private PageResult<TableItem> queryTableMock(OutpatientSpecialtyTreatmentRequest request, Integer page, Integer pageSize) {
        SeqUtil.next();
        List<TableItem> list = new ArrayList<>();
        for (int i = 0; i < pageSize; i++) {
            TableItem item = new TableItem();
            item.setDeptName("心血管内科" + (i + 1));
            item.setTreatmentCount(100 + i * 5);
            item.setTreatmentAmount(25000.00 + i * 500);
            item.setPatientCount(40 + i * 2);
            list.add(item);
        }
        return PageResult.of(list, 55L, page, pageSize);
    }

    // ==================== JdbcTemplate 模式 ====================

    private OverviewData queryOverviewByJdbc(OutpatientSpecialtyTreatmentRequest request) {
        log.info("使用 JdbcTemplate 查询概览数据");
        return queryOverviewMock(request);
    }

    private PageResult<TableItem> queryTableByJdbc(OutpatientSpecialtyTreatmentRequest request, Integer page, Integer pageSize) {
        return queryTableMock(request, page, pageSize);
    }

    // ==================== MyBatis-Plus 模式 ====================

    private OverviewData queryOverviewByMybatisPlus(OutpatientSpecialtyTreatmentRequest request) {
        return queryOverviewMock(request);
    }

    private PageResult<TableItem> queryTableByMybatisPlus(OutpatientSpecialtyTreatmentRequest request, Integer page, Integer pageSize) {
        return queryTableMock(request, page, pageSize);
    }

}
