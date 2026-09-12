package com.reports.service.impl;

import com.reports.config.ReportDataConfig;
import com.reports.dto.request.OutpatientLabStatsRequest;
import com.reports.dto.response.outpatient.lab.stats.OverviewData;
import com.reports.dto.response.outpatient.lab.stats.ReportRank;
import com.reports.dto.response.outpatient.lab.stats.TimeAnalysis;
import com.reports.entity.LabStatsOvEntity;
import com.reports.entity.LabStatsRnkEntity;
import com.reports.entity.LabStatsTmEntity;
import com.reports.mapper.LabStatsMapper;
import com.reports.service.OutpatientLabStatsService;
import com.reports.util.SeqUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

    // ==================== 分时段采血分析 ====================

    @Override
    public TimeAnalysis queryTimeAnalysis(OutpatientLabStatsRequest request) {
        log.info("查询检验分时段分析，mode={}", dataConfig.getMode());
        TimeAnalysis dto = new TimeAnalysis();
        if (!dataConfig.isMybatisPlus()) {
            return dto;
        }
        try {
            List<LabStatsTmEntity> rows =
                    labStatsMapper.queryTimeAnalysis(request.getStartDate(), request.getEndDate());
            List<String> categories = new ArrayList<String>();
            List<Integer> data = new ArrayList<Integer>();
            for (LabStatsTmEntity row : rows) {
                categories.add(row.getTimeSlot());
                data.add(row.getBloodCount() == null ? Integer.valueOf(0) : row.getBloodCount());
            }
            dto.setCategories(categories);
            dto.setData(data);
        } catch (Exception e) {
            log.warn("查询检验分时段分析失败", e);
        }
        return dto;
    }

    // ==================== 检验项目排行 ====================

    @Override
    public ReportRank queryReportRank(OutpatientLabStatsRequest request) {
        log.info("查询检验项目排行，mode={}", dataConfig.getMode());
        ReportRank dto = new ReportRank();
        if (!dataConfig.isMybatisPlus()) {
            return dto;
        }
        try {
            List<LabStatsRnkEntity> rows =
                    labStatsMapper.queryRanking(request.getStartDate(), request.getEndDate(), "LAB");
            // 查询返回的是区间内每天每项的明细，这里按项目汇总后取前 10 名，
            // 否则 400 天的数据会变成几千个类目，图表画不出来
            Map<String, Integer> sumByName = new LinkedHashMap<String, Integer>();
            for (LabStatsRnkEntity row : rows) {
                String name = row.getItemName();
                if (name == null) {
                    continue;
                }
                int v = row.getItemValue() == null ? 0 : row.getItemValue().intValue();
                Integer old = sumByName.get(name);
                sumByName.put(name, Integer.valueOf(old == null ? v : old.intValue() + v));
            }
            List<Map.Entry<String, Integer>> sorted =
                    new ArrayList<Map.Entry<String, Integer>>(sumByName.entrySet());
            Collections.sort(sorted, new Comparator<Map.Entry<String, Integer>>() {
                @Override
                public int compare(Map.Entry<String, Integer> a, Map.Entry<String, Integer> b) {
                    return b.getValue().compareTo(a.getValue());
                }
            });
            List<String> categories = new ArrayList<String>();
            List<Integer> data = new ArrayList<Integer>();
            int limit = Math.min(10, sorted.size());
            for (int i = 0; i < limit; i++) {
                categories.add(sorted.get(i).getKey());
                data.add(sorted.get(i).getValue());
            }
            dto.setCategories(categories);
            dto.setData(data);
        } catch (Exception e) {
            log.warn("查询检验项目排行失败", e);
        }
        return dto;
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
