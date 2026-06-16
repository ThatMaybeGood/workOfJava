package com.reports.service.impl;

import com.reports.config.ReportDataConfig;
import com.reports.dto.common.PageResult;
import com.reports.dto.request.OutpatientRevenueRequest;
import com.reports.dto.response.outpatient.revenue.*;
import com.reports.service.OutpatientRevenueService;
import com.reports.util.SeqUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 门诊收入分析服务实现
 */
@Slf4j
@Service
public class OutpatientRevenueServiceImpl implements OutpatientRevenueService {

    private final ReportDataConfig dataConfig;
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public OutpatientRevenueServiceImpl(ReportDataConfig dataConfig, JdbcTemplate jdbcTemplate) {
        this.dataConfig = dataConfig;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public OverviewData queryOverview(OutpatientRevenueRequest request) {
        log.info("查询门诊收入概览数据，mode={}", dataConfig.getMode());
        if (dataConfig.isMock()) {
            return queryOverviewMock(request);
        } else if (dataConfig.isJdbc()) {
            return queryOverviewByJdbc(request);
        } else {
            return queryOverviewByMybatisPlus(request);
        }
    }

    @Override
    public PageResult<DeptTableItem> queryDeptTable(OutpatientRevenueRequest request, Integer page, Integer pageSize) {
        log.info("查询门诊收入科室表格，mode={}", dataConfig.getMode());
        if (dataConfig.isMock()) {
            return queryDeptTableMock(request, page, pageSize);
        } else if (dataConfig.isJdbc()) {
            return queryDeptTableByJdbc(request, page, pageSize);
        } else {
            return queryDeptTableByMybatisPlus(request, page, pageSize);
        }
    }

    @Override
    public PageResult<DoctorTableItem> queryDoctorTable(OutpatientRevenueRequest request, Integer page, Integer pageSize) {
        log.info("查询门诊收入医生表格，mode={}", dataConfig.getMode());
        if (dataConfig.isMock()) {
            return queryDoctorTableMock(request, page, pageSize);
        } else if (dataConfig.isJdbc()) {
            return queryDoctorTableByJdbc(request, page, pageSize);
        } else {
            return queryDoctorTableByMybatisPlus(request, page, pageSize);
        }
    }

    // ==================== Mock 模式 ====================

    private OverviewData queryOverviewMock(OutpatientRevenueRequest request) {
        SeqUtil.next();
        OverviewData overview = new OverviewData();
        overview.setOutpatientRevenue(1256800.50);
        overview.setServiceRevenue(356200.00);
        return overview;
    }

    private PageResult<DeptTableItem> queryDeptTableMock(OutpatientRevenueRequest request, Integer page, Integer pageSize) {
        SeqUtil.next();
        List<DeptTableItem> list = new ArrayList<>();
        for (int i = 0; i < pageSize; i++) {
            DeptTableItem item = new DeptTableItem();
            item.setDeptName("心血管内科" + (i + 1));
            item.setOutpatientRevenue(String.valueOf(50000.00 + i * 1000));
            item.setServiceRevenue(String.valueOf(15000.00 + i * 300));
            list.add(item);
        }
        return PageResult.of(list, 55L, page, pageSize);
    }

    private PageResult<DoctorTableItem> queryDoctorTableMock(OutpatientRevenueRequest request, Integer page, Integer pageSize) {
        SeqUtil.next();
        List<DoctorTableItem> list = new ArrayList<>();
        for (int i = 0; i < pageSize; i++) {
            DoctorTableItem item = new DoctorTableItem();
            item.setDoctorName("张医生" + (i + 1));
            item.setDeptName("心血管内科" + (i + 1));
            item.setDoctorBenefit(String.valueOf(30000.00 + i * 600));
            item.setServiceRevenue(String.valueOf(10000.00 + i * 200));
            list.add(item);
        }
        return PageResult.of(list, 55L, page, pageSize);
    }

    // ==================== JdbcTemplate 模式 ====================

    private OverviewData queryOverviewByJdbc(OutpatientRevenueRequest request) {
        log.info("使用 JdbcTemplate 查询概览数据");
        return queryOverviewMock(request);
    }

    private PageResult<DeptTableItem> queryDeptTableByJdbc(OutpatientRevenueRequest request, Integer page, Integer pageSize) {
        return queryDeptTableMock(request, page, pageSize);
    }

    private PageResult<DoctorTableItem> queryDoctorTableByJdbc(OutpatientRevenueRequest request, Integer page, Integer pageSize) {
        return queryDoctorTableMock(request, page, pageSize);
    }

    // ==================== MyBatis-Plus 模式 ====================

    private OverviewData queryOverviewByMybatisPlus(OutpatientRevenueRequest request) {
        return queryOverviewMock(request);
    }

    private PageResult<DeptTableItem> queryDeptTableByMybatisPlus(OutpatientRevenueRequest request, Integer page, Integer pageSize) {
        return queryDeptTableMock(request, page, pageSize);
    }

    private PageResult<DoctorTableItem> queryDoctorTableByMybatisPlus(OutpatientRevenueRequest request, Integer page, Integer pageSize) {
        return queryDoctorTableMock(request, page, pageSize);
    }

}
