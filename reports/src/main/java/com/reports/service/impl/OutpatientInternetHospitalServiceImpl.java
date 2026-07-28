package com.reports.service.impl;

import com.reports.config.ReportDataConfig;
import com.reports.dto.common.PageResult;
import com.reports.dto.request.OutpatientInternetHospitalRequest;
import com.reports.dto.response.outpatient.internet.hospital.*;
import com.reports.entity.InternetHospitalBizEntity;
import com.reports.entity.InternetHospitalDeptRnkEntity;
import com.reports.entity.InternetHospitalDocRnkEntity;
import com.reports.entity.InternetHospitalGrwEntity;
import com.reports.entity.InternetHospitalOpEntity;
import com.reports.entity.InternetHospitalOvEntity;
import com.reports.mapper.InternetHospitalMapper;
import com.reports.service.OutpatientInternetHospitalService;
import com.reports.util.SeqUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 互医质控运营月报服务实现
 */
@Slf4j
@Service
public class OutpatientInternetHospitalServiceImpl implements OutpatientInternetHospitalService {

    private final ReportDataConfig dataConfig;
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    private InternetHospitalMapper internetHospitalMapper;

    @Autowired
    public OutpatientInternetHospitalServiceImpl(ReportDataConfig dataConfig, JdbcTemplate jdbcTemplate) {
        this.dataConfig = dataConfig;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public OverviewData queryOverview(OutpatientInternetHospitalRequest request) {
        log.info("查询互医质控概览数据，mode={}", dataConfig.getMode());
        if (dataConfig.isMock()) {
            return queryOverviewMock(request);
        } else if (dataConfig.isJdbc()) {
            return queryOverviewByJdbc(request);
        } else {
            return queryOverviewByMybatisPlus(request);
        }
    }

    @Override
    public PageResult<OperationTableItem> queryOperationTable(OutpatientInternetHospitalRequest request, Integer page, Integer pageSize) {
        log.info("查询互医质控运行情况表，mode={}", dataConfig.getMode());
        if (dataConfig.isMock()) {
            return queryOperationTableMock(request, page, pageSize);
        } else if (dataConfig.isJdbc()) {
            return queryOperationTableByJdbc(request, page, pageSize);
        } else {
            return queryOperationTableByMybatisPlus(request, page, pageSize);
        }
    }

    @Override
    public BusinessChart queryBusinessChart(OutpatientInternetHospitalRequest request) {
        log.info("查询互医质控业务分析图表，mode={}", dataConfig.getMode());
        if (dataConfig.isMock()) {
            return queryBusinessChartMock(request);
        } else if (dataConfig.isJdbc()) {
            return queryBusinessChartByJdbc(request);
        } else {
            return queryBusinessChartByMybatisPlus(request);
        }
    }

    @Override
    public PageResult<DeptRankingItem> queryDeptRanking(OutpatientInternetHospitalRequest request, Integer page, Integer pageSize) {
        log.info("查询互医质控科室排行，mode={}", dataConfig.getMode());
        if (dataConfig.isMock()) {
            return queryDeptRankingMock(request, page, pageSize);
        } else if (dataConfig.isJdbc()) {
            return queryDeptRankingByJdbc(request, page, pageSize);
        } else {
            return queryDeptRankingByMybatisPlus(request, page, pageSize);
        }
    }

    @Override
    public PageResult<DoctorRankingItem> queryDoctorRanking(OutpatientInternetHospitalRequest request, Integer page, Integer pageSize) {
        log.info("查询互医质控医生排行，mode={}", dataConfig.getMode());
        if (dataConfig.isMock()) {
            return queryDoctorRankingMock(request, page, pageSize);
        } else if (dataConfig.isJdbc()) {
            return queryDoctorRankingByJdbc(request, page, pageSize);
        } else {
            return queryDoctorRankingByMybatisPlus(request, page, pageSize);
        }
    }

    @Override
    public GrowthChart queryGrowthChart(OutpatientInternetHospitalRequest request) {
        log.info("查询互医质控增长趋势图表，mode={}", dataConfig.getMode());
        if (dataConfig.isMock()) {
            return queryGrowthChartMock(request);
        } else if (dataConfig.isJdbc()) {
            return queryGrowthChartByJdbc(request);
        } else {
            return queryGrowthChartByMybatisPlus(request);
        }
    }

    // ==================== Mock 模式 ====================

    private OverviewData queryOverviewMock(OutpatientInternetHospitalRequest request) {
        SeqUtil.next();
        OverviewData overview = new OverviewData();
        overview.setOutpatientVolume(5236);
        overview.setDoctorRatio("85.00%");
        overview.setReceptionRate("92.00%");
        overview.setPrescriptionRate("78.00%");
        overview.setRecordRate("95.00%");
        overview.setReviewRate("88.00%");
        overview.setExecutionRate("90.00%");
        return overview;
    }

    private PageResult<OperationTableItem> queryOperationTableMock(OutpatientInternetHospitalRequest request, Integer page, Integer pageSize) {
        SeqUtil.next();
        List<OperationTableItem> list = new ArrayList<>();
        for (int i = 0; i < pageSize; i++) {
            OperationTableItem item = new OperationTableItem();
            item.setName("心血管内科" + (i + 1));
            item.setCurrent(500 + i * 10);
            item.setLast(450 + i * 8);
            item.setGrowth("5.00%");
            list.add(item);
        }
        return PageResult.of(list, 55L, page, pageSize);
    }

    private BusinessChart queryBusinessChartMock(OutpatientInternetHospitalRequest request) {
        SeqUtil.next();
        BusinessChart chart = new BusinessChart();
        List<String> categories = new ArrayList<>();
        categories.add("门诊量");
        categories.add("接诊率");
        categories.add("处方率");
        chart.setCategories(categories);
        List<Integer> current = new ArrayList<>();
        current.add(5236);
        current.add(92);
        current.add(78);
        chart.setCurrent(current);
        List<Integer> last = new ArrayList<>();
        last.add(4800);
        last.add(88);
        last.add(72);
        chart.setLast(last);
        return chart;
    }

    private PageResult<DeptRankingItem> queryDeptRankingMock(OutpatientInternetHospitalRequest request, Integer page, Integer pageSize) {
        SeqUtil.next();
        List<DeptRankingItem> list = new ArrayList<>();
        for (int i = 0; i < pageSize; i++) {
            DeptRankingItem item = new DeptRankingItem();
            item.setRank(i + 1);
            item.setDeptName("心血管内科" + (i + 1));
            item.setCurrentMonth(500 + i * 10);
            item.setLastMonth(450 + i * 8);
            item.setGrowth("5.00%");
            list.add(item);
        }
        return PageResult.of(list, 55L, page, pageSize);
    }

    private PageResult<DoctorRankingItem> queryDoctorRankingMock(OutpatientInternetHospitalRequest request, Integer page, Integer pageSize) {
        SeqUtil.next();
        List<DoctorRankingItem> list = new ArrayList<>();
        for (int i = 0; i < pageSize; i++) {
            DoctorRankingItem item = new DoctorRankingItem();
            item.setRank(i + 1);
            item.setDoctorName("张医生" + (i + 1));
            item.setDeptName("心血管内科" + (i + 1));
            item.setTitle("主任医师");
            item.setCurrentMonth(300 + i * 5);
            list.add(item);
        }
        return PageResult.of(list, 55L, page, pageSize);
    }

    private GrowthChart queryGrowthChartMock(OutpatientInternetHospitalRequest request) {
        SeqUtil.next();
        GrowthChart chart = new GrowthChart();
        List<String> categories = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            categories.add("2024-" + String.format("%02d", i));
        }
        chart.setCategories(categories);
        List<Integer> data = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            data.add(4000 + i * 100);
        }
        chart.setData(data);
        return chart;
    }

    // ==================== JdbcTemplate 模式 ====================

    private OverviewData queryOverviewByJdbc(OutpatientInternetHospitalRequest request) {
        log.info("使用 JdbcTemplate 查询概览数据");
        return queryOverviewMock(request);
    }

    private PageResult<OperationTableItem> queryOperationTableByJdbc(OutpatientInternetHospitalRequest request, Integer page, Integer pageSize) {
        return queryOperationTableMock(request, page, pageSize);
    }

    private BusinessChart queryBusinessChartByJdbc(OutpatientInternetHospitalRequest request) {
        return queryBusinessChartMock(request);
    }

    private PageResult<DeptRankingItem> queryDeptRankingByJdbc(OutpatientInternetHospitalRequest request, Integer page, Integer pageSize) {
        return queryDeptRankingMock(request, page, pageSize);
    }

    private PageResult<DoctorRankingItem> queryDoctorRankingByJdbc(OutpatientInternetHospitalRequest request, Integer page, Integer pageSize) {
        return queryDoctorRankingMock(request, page, pageSize);
    }

    private GrowthChart queryGrowthChartByJdbc(OutpatientInternetHospitalRequest request) {
        return queryGrowthChartMock(request);
    }

    // ==================== MyBatis-Plus 模式 ====================

    private OverviewData queryOverviewByMybatisPlus(OutpatientInternetHospitalRequest request) {
        try {
            InternetHospitalOvEntity entity = internetHospitalMapper.queryOverview(request.getMonth());
            return buildOverviewData(entity);
        } catch (Exception e) {
            log.warn("查询互医质控概览失败", e);
            return new OverviewData();
        }
    }

    private PageResult<OperationTableItem> queryOperationTableByMybatisPlus(OutpatientInternetHospitalRequest request, Integer page, Integer pageSize) {
        try {
            List<InternetHospitalOpEntity> rows = internetHospitalMapper.queryOperationTable(request.getMonth());
            List<OperationTableItem> allItems = new ArrayList<>();
            for (InternetHospitalOpEntity row : rows) {
                allItems.add(buildOperationTableItem(row));
            }
            int total = allItems.size();
            int start = (page - 1) * pageSize;
            int end = Math.min(start + pageSize, total);
            List<OperationTableItem> pageList = start < total ? allItems.subList(start, end) : new ArrayList<>();
            return PageResult.of(pageList, (long) total, page, pageSize);
        } catch (Exception e) {
            log.warn("查询互医质控运行情况表失败", e);
            return PageResult.of(new ArrayList<>(), 0L, page, pageSize);
        }
    }

    private BusinessChart queryBusinessChartByMybatisPlus(OutpatientInternetHospitalRequest request) {
        try {
            List<InternetHospitalBizEntity> rows = internetHospitalMapper.queryBusinessChart(request.getMonth());
            return buildBusinessChart(rows);
        } catch (Exception e) {
            log.warn("查询互医质控业务分析图表失败", e);
            return new BusinessChart();
        }
    }

    private PageResult<DeptRankingItem> queryDeptRankingByMybatisPlus(OutpatientInternetHospitalRequest request, Integer page, Integer pageSize) {
        try {
            List<InternetHospitalDeptRnkEntity> rows = internetHospitalMapper.queryDeptRanking(request.getMonth());
            List<DeptRankingItem> allItems = new ArrayList<>();
            for (InternetHospitalDeptRnkEntity row : rows) {
                allItems.add(buildDeptRankingItem(row));
            }
            int total = allItems.size();
            int start = (page - 1) * pageSize;
            int end = Math.min(start + pageSize, total);
            List<DeptRankingItem> pageList = start < total ? allItems.subList(start, end) : new ArrayList<>();
            return PageResult.of(pageList, (long) total, page, pageSize);
        } catch (Exception e) {
            log.warn("查询互医质控科室排行失败", e);
            return PageResult.of(new ArrayList<>(), 0L, page, pageSize);
        }
    }

    private PageResult<DoctorRankingItem> queryDoctorRankingByMybatisPlus(OutpatientInternetHospitalRequest request, Integer page, Integer pageSize) {
        try {
            List<InternetHospitalDocRnkEntity> rows = internetHospitalMapper.queryDoctorRanking(request.getMonth());
            List<DoctorRankingItem> allItems = new ArrayList<>();
            for (InternetHospitalDocRnkEntity row : rows) {
                allItems.add(buildDoctorRankingItem(row));
            }
            int total = allItems.size();
            int start = (page - 1) * pageSize;
            int end = Math.min(start + pageSize, total);
            List<DoctorRankingItem> pageList = start < total ? allItems.subList(start, end) : new ArrayList<>();
            return PageResult.of(pageList, (long) total, page, pageSize);
        } catch (Exception e) {
            log.warn("查询互医质控医生排行失败", e);
            return PageResult.of(new ArrayList<>(), 0L, page, pageSize);
        }
    }

    private GrowthChart queryGrowthChartByMybatisPlus(OutpatientInternetHospitalRequest request) {
        try {
            List<InternetHospitalGrwEntity> rows = internetHospitalMapper.queryGrowthChart(request.getMonth());
            return buildGrowthChart(rows);
        } catch (Exception e) {
            log.warn("查询互医质控增长趋势图表失败", e);
            return new GrowthChart();
        }
    }

    // ==================== entity -> DTO 转换方法 ====================

    private OverviewData buildOverviewData(InternetHospitalOvEntity entity) {
        if (entity == null) {
            return new OverviewData();
        }
        OverviewData data = new OverviewData();
        data.setOutpatientVolume(entity.getOutpatientVolume());
        data.setDoctorRatio(entity.getDoctorRatio());
        data.setReceptionRate(entity.getReceptionRate());
        data.setPrescriptionRate(entity.getPrescriptionRate());
        data.setRecordRate(entity.getRecordRate());
        data.setReviewRate(entity.getReviewRate());
        data.setExecutionRate(entity.getExecutionRate());
        return data;
    }

    private OperationTableItem buildOperationTableItem(InternetHospitalOpEntity entity) {
        if (entity == null) {
            return new OperationTableItem();
        }
        OperationTableItem item = new OperationTableItem();
        item.setName(entity.getItemName());
        item.setCurrent(entity.getCurrentValue());
        item.setLast(entity.getLastValue());
        item.setGrowth(entity.getGrowthRate());
        return item;
    }

    private BusinessChart buildBusinessChart(List<InternetHospitalBizEntity> rows) {
        BusinessChart chart = new BusinessChart();
        List<String> categories = new ArrayList<>();
        List<Integer> current = new ArrayList<>();
        List<Integer> last = new ArrayList<>();
        if (rows != null) {
            for (InternetHospitalBizEntity row : rows) {
                categories.add(row.getCategory());
                current.add(row.getCurrentValue());
                last.add(row.getLastValue());
            }
        }
        chart.setCategories(categories);
        chart.setCurrent(current);
        chart.setLast(last);
        return chart;
    }

    private DeptRankingItem buildDeptRankingItem(InternetHospitalDeptRnkEntity entity) {
        if (entity == null) {
            return new DeptRankingItem();
        }
        DeptRankingItem item = new DeptRankingItem();
        item.setRank(entity.getRankNum());
        item.setDeptName(entity.getDeptName());
        item.setCurrentMonth(entity.getCurrentMonth());
        item.setLastMonth(entity.getLastMonth());
        item.setGrowth(entity.getGrowthRate());
        return item;
    }

    private DoctorRankingItem buildDoctorRankingItem(InternetHospitalDocRnkEntity entity) {
        if (entity == null) {
            return new DoctorRankingItem();
        }
        DoctorRankingItem item = new DoctorRankingItem();
        item.setRank(entity.getRankNum());
        item.setDoctorName(entity.getDoctorName());
        item.setDeptName(entity.getDeptName());
        item.setTitle(entity.getTitle());
        item.setCurrentMonth(entity.getCurrentMonth());
        return item;
    }

    private GrowthChart buildGrowthChart(List<InternetHospitalGrwEntity> rows) {
        GrowthChart chart = new GrowthChart();
        List<String> categories = new ArrayList<>();
        List<Integer> data = new ArrayList<>();
        if (rows != null) {
            for (InternetHospitalGrwEntity row : rows) {
                categories.add(row.getCategory());
                data.add(row.getDataValue());
            }
        }
        chart.setCategories(categories);
        chart.setData(data);
        return chart;
    }

}
