package com.reports.service.impl;

import com.reports.config.ReportDataConfig;
import com.reports.dto.common.PageResult;
import com.reports.dto.request.OutpatientAlertRequest;
import com.reports.dto.response.outpatient.alert.*;
import com.reports.service.OutpatientAlertService;
import com.reports.util.SeqUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 门诊预警统计服务实现
 */
@Slf4j
@Service
public class OutpatientAlertServiceImpl implements OutpatientAlertService {

    private final ReportDataConfig dataConfig;
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public OutpatientAlertServiceImpl(ReportDataConfig dataConfig, JdbcTemplate jdbcTemplate) {
        this.dataConfig = dataConfig;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public OverviewData queryOverview(OutpatientAlertRequest request) {
        log.info("查询门诊预警概览数据，mode={}", dataConfig.getMode());
        if (dataConfig.isMock()) {
            return queryOverviewMock(request);
        } else if (dataConfig.isJdbc()) {
            return queryOverviewByJdbc(request);
        } else {
            return queryOverviewByMybatisPlus(request);
        }
    }

    @Override
    public PageResult<DeptTableItem> queryDeptTable(OutpatientAlertRequest request, Integer page, Integer pageSize) {
        log.info("查询门诊预警科室表格，mode={}", dataConfig.getMode());
        if (dataConfig.isMock()) {
            return queryDeptTableMock(request, page, pageSize);
        } else if (dataConfig.isJdbc()) {
            return queryDeptTableByJdbc(request, page, pageSize);
        } else {
            return queryDeptTableByMybatisPlus(request, page, pageSize);
        }
    }

    @Override
    public PageResult<DoctorTableItem> queryDoctorTable(OutpatientAlertRequest request, Integer page, Integer pageSize) {
        log.info("查询门诊预警医生表格，mode={}", dataConfig.getMode());
        if (dataConfig.isMock()) {
            return queryDoctorTableMock(request, page, pageSize);
        } else if (dataConfig.isJdbc()) {
            return queryDoctorTableByJdbc(request, page, pageSize);
        } else {
            return queryDoctorTableByMybatisPlus(request, page, pageSize);
        }
    }

    // ==================== Mock 模式 ====================

    private OverviewData queryOverviewMock(OutpatientAlertRequest request) {
        SeqUtil.next();
        OverviewData overview = new OverviewData();
        overview.setRemainAlert(12);
        overview.setAppointmentAlert(8);
        overview.setEarlyLeave(5);
        return overview;
    }

    private PageResult<DeptTableItem> queryDeptTableMock(OutpatientAlertRequest request, Integer page, Integer pageSize) {
        SeqUtil.next();
        List<DeptTableItem> list = new ArrayList<>();
        for (int i = 0; i < pageSize; i++) {
            DeptTableItem item = new DeptTableItem();
            item.setDeptName("心血管内科" + (i + 1));
            item.setRemainAlert(12 + i);
            item.setAppointmentAlert(8 + i);
            item.setEarlyLeave(5 + i);
            list.add(item);
        }
        return PageResult.of(list, 55L, page, pageSize);
    }

    private PageResult<DoctorTableItem> queryDoctorTableMock(OutpatientAlertRequest request, Integer page, Integer pageSize) {
        SeqUtil.next();
        List<DoctorTableItem> list = new ArrayList<>();
        for (int i = 0; i < pageSize; i++) {
            DoctorTableItem item = new DoctorTableItem();
            item.setDoctorName("张医生" + (i + 1));
            item.setDeptName("心血管内科" + (i + 1));
            item.setRemainAlert(10 + i);
            item.setAppointmentAlert(6 + i);
            item.setEarlyLeave(4 + i);
            list.add(item);
        }
        return PageResult.of(list, 55L, page, pageSize);
    }

    // ==================== JdbcTemplate 模式 ====================

    private OverviewData queryOverviewByJdbc(OutpatientAlertRequest request) {
        log.info("使用 JdbcTemplate 查询概览数据");
        return queryOverviewMock(request);
    }

    private PageResult<DeptTableItem> queryDeptTableByJdbc(OutpatientAlertRequest request, Integer page, Integer pageSize) {
        log.info("使用 JdbcTemplate 查询科室表格");
        return queryDeptTableMock(request, page, pageSize);
    }

    private PageResult<DoctorTableItem> queryDoctorTableByJdbc(OutpatientAlertRequest request, Integer page, Integer pageSize) {
        return queryDoctorTableMock(request, page, pageSize);
    }

    // ==================== MyBatis-Plus 模式 ====================

    private OverviewData queryOverviewByMybatisPlus(OutpatientAlertRequest request) {
        return queryOverviewMock(request);
    }

    private PageResult<DeptTableItem> queryDeptTableByMybatisPlus(OutpatientAlertRequest request, Integer page, Integer pageSize) {
        return queryDeptTableMock(request, page, pageSize);
    }

    private PageResult<DoctorTableItem> queryDoctorTableByMybatisPlus(OutpatientAlertRequest request, Integer page, Integer pageSize) {
        return queryDoctorTableMock(request, page, pageSize);
    }

}
