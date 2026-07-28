package com.reports.service.impl;

import com.reports.config.ReportDataConfig;
import com.reports.dto.common.PageResult;
import com.reports.dto.request.OutpatientRoomUsageRequest;
import com.reports.dto.response.outpatient.room.usage.*;
import com.reports.service.OutpatientRoomUsageService;
import com.reports.mapper.RoomUsageMapper;
import com.reports.entity.RoomUsageOvEntity;
import com.reports.entity.RoomUsageDtlEntity;
import com.reports.util.SeqUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 诊室使用率分析服务实现
 */
@Slf4j
@Service
public class OutpatientRoomUsageServiceImpl implements OutpatientRoomUsageService {

    private final ReportDataConfig dataConfig;
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    private RoomUsageMapper roomUsageMapper;

    @Autowired
    public OutpatientRoomUsageServiceImpl(ReportDataConfig dataConfig, JdbcTemplate jdbcTemplate) {
        this.dataConfig = dataConfig;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public OverviewData queryOverview(OutpatientRoomUsageRequest request) {
        log.info("查询诊室使用率概览数据，mode={}", dataConfig.getMode());
        if (dataConfig.isMock()) {
            return queryOverviewMock(request);
        } else if (dataConfig.isJdbc()) {
            return queryOverviewByJdbc(request);
        } else {
            return queryOverviewByMybatisPlus(request);
        }
    }

    @Override
    public PageResult<TableItem> queryTable(OutpatientRoomUsageRequest request, Integer page, Integer pageSize) {
        log.info("查询诊室使用率表格数据，mode={}", dataConfig.getMode());
        if (dataConfig.isMock()) {
            return queryTableMock(request, page, pageSize);
        } else if (dataConfig.isJdbc()) {
            return queryTableByJdbc(request, page, pageSize);
        } else {
            return queryTableByMybatisPlus(request, page, pageSize);
        }
    }

    // ==================== Mock 模式 ====================

    private OverviewData queryOverviewMock(OutpatientRoomUsageRequest request) {
        SeqUtil.next();
        OverviewData overview = new OverviewData();
        overview.setAvgUsage("85.00%");
        overview.setAmUsage("90.00%");
        overview.setPmUsage("80.00%");
        overview.setHolidayUsage("70.00%");
        return overview;
    }

    private PageResult<TableItem> queryTableMock(OutpatientRoomUsageRequest request, Integer page, Integer pageSize) {
        SeqUtil.next();
        List<TableItem> list = new ArrayList<>();
        for (int i = 0; i < pageSize; i++) {
            TableItem item = new TableItem();
            item.setDeptName("心血管内科" + (i + 1));
            item.setAvgUsage("85.00%");
            item.setAmUsage("90.00%");
            item.setPmUsage("80.00%");
            item.setHolidayUsage("70.00%");
            list.add(item);
        }
        return PageResult.of(list, 55L, page, pageSize);
    }

    // ==================== JdbcTemplate 模式 ====================

    private OverviewData queryOverviewByJdbc(OutpatientRoomUsageRequest request) {
        log.info("使用 JdbcTemplate 查询概览数据");
        return queryOverviewMock(request);
    }

    private PageResult<TableItem> queryTableByJdbc(OutpatientRoomUsageRequest request, Integer page, Integer pageSize) {
        return queryTableMock(request, page, pageSize);
    }

    // ==================== MyBatis-Plus 模式 ====================

    private OverviewData queryOverviewByMybatisPlus(OutpatientRoomUsageRequest request) {
        try {
            RoomUsageOvEntity entity = roomUsageMapper.queryOverview(request.getStartDate(), request.getEndDate());
            return buildOverviewData(entity);
        } catch (Exception e) {
            log.warn("查询诊室使用率概览失败", e);
            return new OverviewData();
        }
    }

    private PageResult<TableItem> queryTableByMybatisPlus(OutpatientRoomUsageRequest request, Integer page, Integer pageSize) {
        try {
            List<RoomUsageDtlEntity> rows = roomUsageMapper.queryDeptDetail(request.getStartDate(), request.getEndDate(), request.getDeptName());
            List<TableItem> allItems = new ArrayList<>();
            for (RoomUsageDtlEntity row : rows) {
                allItems.add(buildTableItem(row));
            }
            int total = allItems.size();
            int start = (page - 1) * pageSize;
            int end = Math.min(start + pageSize, total);
            List<TableItem> pageList = start < total ? allItems.subList(start, end) : new ArrayList<>();
            return PageResult.of(pageList, (long) total, page, pageSize);
        } catch (Exception e) {
            log.warn("查询诊室使用率表格失败", e);
            return PageResult.of(new ArrayList<>(), 0L, page, pageSize);
        }
    }

    // ==================== 实体转换 ====================

    private OverviewData buildOverviewData(RoomUsageOvEntity entity) {
        if (entity == null) {
            return new OverviewData();
        }
        OverviewData overview = new OverviewData();
        overview.setAvgUsage(entity.getAvgUsage());
        overview.setAmUsage(entity.getAmUsage());
        overview.setPmUsage(entity.getPmUsage());
        overview.setHolidayUsage(entity.getHolidayUsage());
        return overview;
    }

    private TableItem buildTableItem(RoomUsageDtlEntity entity) {
        if (entity == null) {
            return new TableItem();
        }
        TableItem item = new TableItem();
        item.setDeptName(entity.getDeptName());
        item.setAvgUsage(entity.getAvgUsage());
        item.setAmUsage(entity.getAmUsage());
        item.setPmUsage(entity.getPmUsage());
        item.setHolidayUsage(entity.getHolidayUsage());
        return item;
    }

}
