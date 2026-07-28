package com.reports.service.impl;

import com.reports.config.ReportDataConfig;
import com.reports.dto.common.PageResult;
import com.reports.dto.request.OutpatientNoShowRequest;
import com.reports.dto.response.outpatient.no.show.*;
import com.reports.service.OutpatientNoShowService;
import com.reports.mapper.NoShowMapper;
import com.reports.entity.NoShowOvEntity;
import com.reports.entity.NoShowAgeEntity;
import com.reports.entity.NoShowOrgEntity;
import com.reports.entity.NoShowChnEntity;
import com.reports.entity.NoShowDtlEntity;
import com.reports.util.SeqUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 爽约退号分析服务实现
 */
@Slf4j
@Service
public class OutpatientNoShowServiceImpl implements OutpatientNoShowService {

    private final ReportDataConfig dataConfig;
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public OutpatientNoShowServiceImpl(ReportDataConfig dataConfig, JdbcTemplate jdbcTemplate) {
        this.dataConfig = dataConfig;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Autowired
    NoShowMapper noShowMapper;

    @Override
    public OverviewData queryOverview(OutpatientNoShowRequest request) {
        log.info("查询爽约退号概览数据，mode={}", dataConfig.getMode());
        if (dataConfig.isMock()) {
            return queryOverviewMock(request);
        } else if (dataConfig.isJdbc()) {
            return queryOverviewByJdbc(request);
        } else {
            return queryOverviewByMybatisPlus(request);
        }
    }

    @Override
    public AgeAnalysis queryAgeAnalysis(OutpatientNoShowRequest request) {
        log.info("查询爽约退号年龄分析，mode={}", dataConfig.getMode());
        if (dataConfig.isMock()) {
            return queryAgeAnalysisMock(request);
        } else if (dataConfig.isJdbc()) {
            return queryAgeAnalysisByJdbc(request);
        } else {
            return queryAgeAnalysisByMybatisPlus(request);
        }
    }

    @Override
    public List<AnalysisItem> queryRefundOrigin(OutpatientNoShowRequest request) {
        log.info("查询爽约退号来源分析，mode={}", dataConfig.getMode());
        if (dataConfig.isMock()) {
            return queryRefundOriginMock(request);
        } else if (dataConfig.isJdbc()) {
            return queryRefundOriginByJdbc(request);
        } else {
            return queryRefundOriginByMybatisPlus(request);
        }
    }

    @Override
    public List<AnalysisItem> queryRefundChannel(OutpatientNoShowRequest request) {
        log.info("查询爽约退号渠道分析，mode={}", dataConfig.getMode());
        if (dataConfig.isMock()) {
            return queryRefundChannelMock(request);
        } else if (dataConfig.isJdbc()) {
            return queryRefundChannelByJdbc(request);
        } else {
            return queryRefundChannelByMybatisPlus(request);
        }
    }

    @Override
    public PageResult<TableItem> queryTable(OutpatientNoShowRequest request, Integer page, Integer pageSize) {
        log.info("查询爽约退号明细表格，mode={}", dataConfig.getMode());
        if (dataConfig.isMock()) {
            return queryTableMock(request, page, pageSize);
        } else if (dataConfig.isJdbc()) {
            return queryTableByJdbc(request, page, pageSize);
        } else {
            return queryTableByMybatisPlus(request, page, pageSize);
        }
    }

    // ==================== Mock 模式 ====================

    private OverviewData queryOverviewMock(OutpatientNoShowRequest request) {
        SeqUtil.next();
        OverviewData overview = new OverviewData();
        overview.setRefundCount(128);
        overview.setRefundRate("2.60%");
        overview.setNoShowCount(256);
        overview.setNoShowRate("5.20%");
        return overview;
    }

    private AgeAnalysis queryAgeAnalysisMock(OutpatientNoShowRequest request) {
        SeqUtil.next();
        AgeAnalysis analysis = new AgeAnalysis();
        List<String> categories = new ArrayList<>();
        categories.add("0-18岁");
        categories.add("19-35岁");
        categories.add("36-50岁");
        categories.add("51-65岁");
        categories.add("65岁以上");
        analysis.setCategories(categories);
        List<Integer> data = new ArrayList<>();
        data.add(20);
        data.add(80);
        data.add(70);
        data.add(50);
        data.add(36);
        analysis.setData(data);
        return analysis;
    }

    private List<AnalysisItem> queryRefundOriginMock(OutpatientNoShowRequest request) {
        SeqUtil.next();
        List<AnalysisItem> list = new ArrayList<>();
        list.add(newAnalysisItem("微信预约", 80));
        list.add(newAnalysisItem("电话预约", 30));
        list.add(newAnalysisItem("现场挂号", 18));
        return list;
    }

    private List<AnalysisItem> queryRefundChannelMock(OutpatientNoShowRequest request) {
        SeqUtil.next();
        List<AnalysisItem> list = new ArrayList<>();
        list.add(newAnalysisItem("窗口退费", 60));
        list.add(newAnalysisItem("自助机退费", 40));
        list.add(newAnalysisItem("线上退费", 28));
        return list;
    }

    private PageResult<TableItem> queryTableMock(OutpatientNoShowRequest request, Integer page, Integer pageSize) {
        SeqUtil.next();
        List<TableItem> list = new ArrayList<>();
        for (int i = 0; i < pageSize; i++) {
            TableItem item = new TableItem();
            item.setDeptName("心血管内科" + (i + 1));
            item.setRefundCount(5 + i);
            item.setRefundRate("2.00%");
            item.setNoShowCount(10 + i);
            item.setNoShowRate("5.00%");
            list.add(item);
        }
        return PageResult.of(list, 55L, page, pageSize);
    }

    // ==================== JdbcTemplate 模式 ====================

    private OverviewData queryOverviewByJdbc(OutpatientNoShowRequest request) {
        log.info("使用 JdbcTemplate 查询概览数据");
        return queryOverviewMock(request);
    }

    private AgeAnalysis queryAgeAnalysisByJdbc(OutpatientNoShowRequest request) {
        return queryAgeAnalysisMock(request);
    }

    private List<AnalysisItem> queryRefundOriginByJdbc(OutpatientNoShowRequest request) {
        return queryRefundOriginMock(request);
    }

    private List<AnalysisItem> queryRefundChannelByJdbc(OutpatientNoShowRequest request) {
        return queryRefundChannelMock(request);
    }

    private PageResult<TableItem> queryTableByJdbc(OutpatientNoShowRequest request, Integer page, Integer pageSize) {
        return queryTableMock(request, page, pageSize);
    }

    // ==================== MyBatis-Plus 模式 ====================

    private OverviewData queryOverviewByMybatisPlus(OutpatientNoShowRequest request) {
        try {
            NoShowOvEntity entity = noShowMapper.queryOverview(request.getStartDate(), request.getEndDate());
            return buildOverviewData(entity);
        } catch (Exception e) {
            log.warn("查询爽约退号概览数据失败", e);
            return new OverviewData();
        }
    }

    private AgeAnalysis queryAgeAnalysisByMybatisPlus(OutpatientNoShowRequest request) {
        try {
            List<NoShowAgeEntity> list = noShowMapper.queryAgeAnalysis(request.getStartDate(), request.getEndDate());
            return buildAgeAnalysis(list);
        } catch (Exception e) {
            log.warn("查询爽约退号年龄分析失败", e);
            return new AgeAnalysis();
        }
    }

    private List<AnalysisItem> queryRefundOriginByMybatisPlus(OutpatientNoShowRequest request) {
        try {
            List<NoShowOrgEntity> list = noShowMapper.queryOriginAnalysis(request.getStartDate(), request.getEndDate(), null);
            List<AnalysisItem> result = new ArrayList<>();
            for (NoShowOrgEntity entity : list) {
                result.add(newAnalysisItem(entity.getItemName(), entity.getItemValue()));
            }
            return result;
        } catch (Exception e) {
            log.warn("查询爽约退号来源分析失败", e);
            return new ArrayList<>();
        }
    }

    private List<AnalysisItem> queryRefundChannelByMybatisPlus(OutpatientNoShowRequest request) {
        try {
            List<NoShowChnEntity> list = noShowMapper.queryChannelAnalysis(request.getStartDate(), request.getEndDate(), null);
            List<AnalysisItem> result = new ArrayList<>();
            for (NoShowChnEntity entity : list) {
                result.add(newAnalysisItem(entity.getItemName(), entity.getItemValue()));
            }
            return result;
        } catch (Exception e) {
            log.warn("查询爽约退号渠道分析失败", e);
            return new ArrayList<>();
        }
    }

    private PageResult<TableItem> queryTableByMybatisPlus(OutpatientNoShowRequest request, Integer page, Integer pageSize) {
        try {
            List<NoShowDtlEntity> list = noShowMapper.queryDeptDetail(request.getStartDate(), request.getEndDate(), null);
            List<TableItem> allItems = new ArrayList<>();
            for (NoShowDtlEntity entity : list) {
                allItems.add(buildTableItem(entity));
            }
            int total = allItems.size();
            int start = (page - 1) * pageSize;
            int end = Math.min(start + pageSize, total);
            List<TableItem> pageList = start < total ? allItems.subList(start, end) : new ArrayList<>();
            return PageResult.of(pageList, (long) total, page, pageSize);
        } catch (Exception e) {
            log.warn("查询爽约退号明细表格失败", e);
            return PageResult.of(new ArrayList<>(), 0L, page, pageSize);
        }
    }

    // ==================== Entity-DTO 转换方法 ====================

    private OverviewData buildOverviewData(NoShowOvEntity entity) {
        if (entity == null) return new OverviewData();
        OverviewData dto = new OverviewData();
        dto.setRefundCount(entity.getRefundCount());
        dto.setRefundRate(entity.getRefundRate());
        dto.setNoShowCount(entity.getNoShowCount());
        dto.setNoShowRate(entity.getNoShowRate());
        return dto;
    }

    private AgeAnalysis buildAgeAnalysis(List<NoShowAgeEntity> list) {
        AgeAnalysis analysis = new AgeAnalysis();
        List<String> categories = new ArrayList<>();
        List<Integer> data = new ArrayList<>();
        for (NoShowAgeEntity entity : list) {
            categories.add(entity.getAgeGroup());
            data.add(entity.getNoShowCount());
        }
        analysis.setCategories(categories);
        analysis.setData(data);
        return analysis;
    }

    private TableItem buildTableItem(NoShowDtlEntity entity) {
        if (entity == null) return null;
        TableItem item = new TableItem();
        item.setDeptName(entity.getDeptName());
        item.setRefundCount(entity.getRefundCount());
        item.setRefundRate(entity.getRefundRate());
        item.setNoShowCount(entity.getNoShowCount());
        item.setNoShowRate(entity.getNoShowRate());
        return item;
    }

    // ==================== 工具方法 ====================

    private AnalysisItem newAnalysisItem(String name, int value) {
        AnalysisItem item = new AnalysisItem();
        item.setName(name);
        item.setValue(value);
        return item;
    }

}
