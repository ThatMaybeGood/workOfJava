package com.reports.service.impl;

import com.reports.config.ReportDataConfig;
import com.reports.dto.common.PageResult;
import com.reports.dto.request.OutpatientAlertRequest;
import com.reports.dto.response.outpatient.alert.*;
import com.reports.entity.OutpatientAlertOvEntity;
import com.reports.mapper.OutpatientAlertMapper;
import com.reports.service.OutpatientAlertService;
import com.reports.util.SeqUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
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
    private OutpatientAlertMapper alertMapper;

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

    /**
     * 请求体日期（yyyy-MM-dd 字符串）转 java.sql.Date，格式非法时返回 null
     */
    private java.sql.Date parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        try {
            return java.sql.Date.valueOf(dateStr.trim());
        } catch (IllegalArgumentException e) {
            log.warn("日期格式非法: {}", dateStr);
            return null;
        }
    }

    private OverviewData queryOverviewByMybatisPlus(OutpatientAlertRequest request) {
        try {
            OutpatientAlertOvEntity entity = alertMapper.queryOverview(parseDate(request.getStartDate()), parseDate(request.getEndDate()));
            return buildOverviewData(entity);
        } catch (Exception e) {
            log.warn("查询概览数据失败", e);
            return new OverviewData();
        }
    }

    private PageResult<DeptTableItem> queryDeptTableByMybatisPlus(OutpatientAlertRequest request, Integer page, Integer pageSize) {
        try {
            List<OutpatientAlertOvEntity> rows = alertMapper.queryDeptDetail(parseDate(request.getStartDate()), parseDate(request.getEndDate()), request.getDeptCode(), request.getDeptName());
            List<DeptTableItem> allItems = new ArrayList<>();
            for (OutpatientAlertOvEntity row : rows) {
                allItems.add(buildDeptTableItem(row));
            }
            int total = allItems.size();
            int start = (page - 1) * pageSize;
            int end = Math.min(start + pageSize, total);
            List<DeptTableItem> pageList = start < total ? allItems.subList(start, end) : new ArrayList<>();
            return PageResult.of(pageList, (long) total, page, pageSize);
        } catch (Exception e) {
            log.warn("查询科室表格数据失败", e);
            return PageResult.of(new ArrayList<>(), 0L, page, pageSize);
        }
    }

    private PageResult<DoctorTableItem> queryDoctorTableByMybatisPlus(OutpatientAlertRequest request, Integer page, Integer pageSize) {
        try {
            List<OutpatientAlertOvEntity> rows = alertMapper.queryDoctorDetail(parseDate(request.getStartDate()), parseDate(request.getEndDate()), request.getDeptCode(), request.getDeptName());
            List<DoctorTableItem> allItems = new ArrayList<>();
            for (OutpatientAlertOvEntity row : rows) {
                allItems.add(buildDoctorTableItem(row));
            }
            int total = allItems.size();
            int start = (page - 1) * pageSize;
            int end = Math.min(start + pageSize, total);
            List<DoctorTableItem> pageList = start < total ? allItems.subList(start, end) : new ArrayList<>();
            return PageResult.of(pageList, (long) total, page, pageSize);
        } catch (Exception e) {
            log.warn("查询医生表格数据失败", e);
            return PageResult.of(new ArrayList<>(), 0L, page, pageSize);
        }
    }

    // ==================== 工具方法 ====================

    private OverviewData buildOverviewData(OutpatientAlertOvEntity entity) {
        if (entity == null) return new OverviewData();
        OverviewData dto = new OverviewData();
        dto.setRemainAlert(entity.getRemainAlert());
        dto.setAppointmentAlert(entity.getAppointmentAlert());
        dto.setEarlyLeave(entity.getEarlyLeave());
        return dto;
    }

    private DeptTableItem buildDeptTableItem(OutpatientAlertOvEntity entity) {
        if (entity == null) return new DeptTableItem();
        DeptTableItem item = new DeptTableItem();
        item.setDeptName(entity.getDeptName());
        item.setRemainAlert(entity.getRemainAlert());
        item.setAppointmentAlert(entity.getAppointmentAlert());
        item.setEarlyLeave(entity.getEarlyLeave());
        return item;
    }

    private DoctorTableItem buildDoctorTableItem(OutpatientAlertOvEntity entity) {
        if (entity == null) return new DoctorTableItem();
        DoctorTableItem item = new DoctorTableItem();
        item.setDoctorName(entity.getDoctorName());
        item.setDeptName(entity.getDeptName());
        item.setRemainAlert(entity.getRemainAlert());
        item.setAppointmentAlert(entity.getAppointmentAlert());
        item.setEarlyLeave(entity.getEarlyLeave());
        return item;
    }

    @Override
    public List<DetailItem> queryEarlyLeaveDetail(OutpatientAlertRequest request) {
        log.info("查询早退明细，mode={}", dataConfig.getMode());
        if (dataConfig.isMock()) {
            return queryEarlyLeaveDetailMock(request);
        }
        try {
            List<OutpatientAlertOvEntity> rows = alertMapper.queryEarlyLeaveDetail(
                    parseDate(request.getStartDate()), parseDate(request.getEndDate()),
                    request.getDeptCode(), request.getDeptName(),
                    request.getDoctorName(), request.getClinicPeriod());
            List<DetailItem> result = new ArrayList<>();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            for (OutpatientAlertOvEntity row : rows) {
                DetailItem item = new DetailItem();
                item.setDeptName(row.getDeptName());
                item.setDoctorName(row.getDoctorName());
                item.setStatDate(row.getStatDate() != null ? sdf.format(row.getStatDate()) : null);
                item.setClinicPeriod(row.getClinicPeriod());
                item.setHisLogoutTime(row.getHisLogoutTime());
                item.setRemainAlert(row.getRemainAlert());
                item.setAppointmentAlert(row.getAppointmentAlert());
                item.setEarlyLeave(row.getEarlyLeave());
                result.add(item);
            }
            return result;
        } catch (Exception e) {
            log.warn("查询早退明细失败", e);
            return new ArrayList<>();
        }
    }

    private List<DetailItem> queryEarlyLeaveDetailMock(OutpatientAlertRequest request) {
        List<DetailItem> list = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            DetailItem item = new DetailItem();
            item.setDeptName("心血管内科");
            item.setDoctorName("张医生" + (i + 1));
            item.setStatDate(request.getStartDate() != null ? request.getStartDate() : "2025-10-01");
            item.setClinicPeriod("08:00-11:30");
            item.setHisLogoutTime("11:25:00");
            item.setRemainAlert(2);
            item.setAppointmentAlert(1);
            item.setEarlyLeave(1);
            list.add(item);
        }
        return list;
    }

}
