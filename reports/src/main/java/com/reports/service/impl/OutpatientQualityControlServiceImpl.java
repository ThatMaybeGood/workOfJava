package com.reports.service.impl;

import com.reports.config.ReportDataConfig;
import com.reports.dto.common.PageResult;
import com.reports.dto.request.OutpatientQualityControlRequest;
import com.reports.dto.response.outpatient.quality.control.*;
import com.reports.entity.QualityControlDtlEntity;
import com.reports.entity.QualityControlOvEntity;
import com.reports.mapper.QualityControlMapper;
import com.reports.service.OutpatientQualityControlService;
import com.reports.util.SeqUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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
    private QualityControlMapper qualityControlMapper;

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
        try {
            Date startDate = parseMonthStart(request.getStartMonth());
            Date endDate = parseMonthEnd(request.getEndMonth());
            QualityControlOvEntity entity = qualityControlMapper.queryOverview(startDate, endDate);
            return buildOverviewData(entity);
        } catch (Exception e) {
            log.warn("查询门诊质量控制概览失败", e);
            return new OverviewData();
        }
    }

    private PageResult<TableItem> queryTableByMybatisPlus(OutpatientQualityControlRequest request, Integer page, Integer pageSize) {
        try {
            List<QualityControlDtlEntity> rows = qualityControlMapper.queryMonthlyDetail(request.getStartMonth(), request.getEndMonth());
            List<TableItem> allItems = new ArrayList<>();
            for (QualityControlDtlEntity row : rows) {
                allItems.add(buildTableItem(row));
            }
            int total = allItems.size();
            int start = (page - 1) * pageSize;
            int end = Math.min(start + pageSize, total);
            List<TableItem> pageList = start < total ? allItems.subList(start, end) : new ArrayList<>();
            return PageResult.of(pageList, (long) total, page, pageSize);
        } catch (Exception e) {
            log.warn("查询门诊质量控制表格失败", e);
            return PageResult.of(new ArrayList<>(), 0L, page, pageSize);
        }
    }

    // ==================== entity -> DTO 转换方法 ====================

    private OverviewData buildOverviewData(QualityControlOvEntity entity) {
        if (entity == null) {
            return new OverviewData();
        }
        OverviewData data = new OverviewData();
        data.setEmrUsageRate(entity.getEmrUsageRate());
        data.setStandardDiagnosisRate(entity.getStandardDiagnosisRate());
        data.setOnTimeRate(entity.getOnTimeRate());
        data.setStopRate(entity.getStopRate());
        data.setChemoRecordRate(entity.getChemoRecordRate());
        data.setChemoAdverseRate(entity.getChemoAdverseRate());
        data.setChemoInfusionRate(entity.getChemoInfusionRate());
        data.setCriticalValueRate(entity.getCriticalValueRate());
        data.setBloodDrawErrorRate(entity.getBloodDrawErrorRate());
        data.setSurgeryComplicationRate(entity.getSurgeryComplicationRate());
        data.setAdverseEventRate(entity.getAdverseEventRate());
        return data;
    }

    private TableItem buildTableItem(QualityControlDtlEntity entity) {
        if (entity == null) {
            return new TableItem();
        }
        TableItem item = new TableItem();
        item.setMonth(entity.getStatMonth());
        item.setEmrUsageRate(entity.getEmrUsageRate());
        item.setStandardDiagnosisRate(entity.getStandardDiagnosisRate());
        item.setOnTimeRate(entity.getOnTimeRate());
        item.setStopRate(entity.getStopRate());
        item.setChemoRecordRate(entity.getChemoRecordRate());
        item.setChemoAdverseRate(entity.getChemoAdverseRate());
        item.setChemoInfusionRate(entity.getChemoInfusionRate());
        item.setCriticalValueRate(entity.getCriticalValueRate());
        item.setBloodDrawErrorRate(entity.getBloodDrawErrorRate());
        item.setSurgeryComplicationRate(entity.getSurgeryComplicationRate());
        item.setAdverseEventRate(entity.getAdverseEventRate());
        return item;
    }

    // ==================== 日期转换方法 ====================

    private Date parseMonthStart(String month) {
        try {
            return new SimpleDateFormat("yyyy-MM-dd").parse(month + "-01");
        } catch (Exception e) {
            log.warn("解析月份起始日期失败: {}", month, e);
            return null;
        }
    }

    private Date parseMonthEnd(String month) {
        try {
            Date firstDay = new SimpleDateFormat("yyyy-MM-dd").parse(month + "-01");
            Calendar cal = Calendar.getInstance();
            cal.setTime(firstDay);
            cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
            return cal.getTime();
        } catch (Exception e) {
            log.warn("解析月份结束日期失败: {}", month, e);
            return null;
        }
    }

}
