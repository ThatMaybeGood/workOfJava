package com.reports.service.impl;

import com.reports.config.ReportDataConfig;
import com.reports.dto.request.TreatmentStatsRequest;
import com.reports.entity.TreatmentStatsDtlEntity;
import com.reports.entity.TreatmentStatsOvEntity;
import com.reports.entity.TreatmentStatsTrendEntity;
import com.reports.mapper.TreatmentStatsMapper;
import com.reports.service.TreatmentStatsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 治疗统计报表服务实现。
 *
 * <p>数据来自四张表：概览 {@code TR_TREAT_STAT_OV}、科室明细 {@code TR_TREAT_STAT_DTL}、
 * 每日趋势 {@code TR_TREAT_STAT_TREND}、治疗项目 {@code TR_TREAT_STAT_ITEM}。
 * 返回结构直接按页面读的字段拼 Map。
 */
@Slf4j
@Service
public class TreatmentStatsServiceImpl implements TreatmentStatsService {

    private static final int TOP_N = 10;

    private final ReportDataConfig dataConfig;
    private final TreatmentStatsMapper treatmentStatsMapper;

    @Autowired
    public TreatmentStatsServiceImpl(ReportDataConfig dataConfig,
                                     TreatmentStatsMapper treatmentStatsMapper) {
        this.dataConfig = dataConfig;
        this.treatmentStatsMapper = treatmentStatsMapper;
    }

    @Override
    public Map<String, Object> queryStats(TreatmentStatsRequest request, Integer page, Integer pageSize) {
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("overview", buildOverview(request));
        result.put("trend", buildTrend(request));
        result.put("topProjects", buildTopProjects(request));
        result.put("table", buildTable(request, page, pageSize));
        return result;
    }

    // ==================== 概览 ====================

    private Map<String, Object> buildOverview(TreatmentStatsRequest request) {
        Map<String, Object> overview = new LinkedHashMap<String, Object>();
        if (!dataConfig.isMybatisPlus()) {
            return overview;
        }
        try {
            TreatmentStatsOvEntity e =
                    treatmentStatsMapper.queryOverview(request.getStartDate(), request.getEndDate());
            if (e != null) {
                overview.put("patientCount", nvl(e.getPatientCount()));
                overview.put("treatmentCount", nvl(e.getTreatmentCount()));
                overview.put("treatmentAmount", e.getTreatmentAmount() == null
                        ? BigDecimal.ZERO : e.getTreatmentAmount());
                overview.put("avgAmount", e.getAvgAmount() == null
                        ? BigDecimal.ZERO : e.getAvgAmount());
            }
        } catch (Exception ex) {
            log.warn("查询治疗统计概览失败", ex);
        }
        return overview;
    }

    // ==================== 治疗量趋势 ====================

    private Map<String, Object> buildTrend(TreatmentStatsRequest request) {
        List<String> dates = new ArrayList<String>();
        List<Integer> data = new ArrayList<Integer>();
        if (dataConfig.isMybatisPlus()) {
            try {
                // queryTrend 已按 trend_date 分组升序返回
                List<TreatmentStatsTrendEntity> rows =
                        treatmentStatsMapper.queryTrend(request.getStartDate(), request.getEndDate());
                SimpleDateFormat sdf = new SimpleDateFormat("MM-dd");
                for (TreatmentStatsTrendEntity row : rows) {
                    dates.add(row.getTrendDate() == null ? "" : sdf.format(row.getTrendDate()));
                    data.add(row.getTrendValue() == null ? Integer.valueOf(0) : row.getTrendValue());
                }
            } catch (Exception ex) {
                log.warn("查询治疗统计趋势失败", ex);
            }
        }
        Map<String, Object> trend = new LinkedHashMap<String, Object>();
        trend.put("dates", dates);
        trend.put("data", data);
        return trend;
    }

    // ==================== TOP 治疗项目 ====================

    private List<Map<String, Object>> buildTopProjects(TreatmentStatsRequest request) {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        if (!dataConfig.isMybatisPlus()) {
            return list;
        }
        try {
            List<Map<String, Object>> rows =
                    treatmentStatsMapper.queryTopItems(request.getStartDate(), request.getEndDate());
            for (Map<String, Object> row : rows) {
                if (list.size() >= TOP_N) {
                    break;
                }
                Map<String, Object> item = new LinkedHashMap<String, Object>();
                // 别名是带引号的小写，Oracle 会原样保留列标签
                item.put("name", row.get("name"));
                item.put("value", row.get("value"));
                list.add(item);
            }
        } catch (Exception ex) {
            log.warn("查询 TOP 治疗项目失败", ex);
        }
        return list;
    }

    // ==================== 科室明细表 ====================

    private Map<String, Object> buildTable(TreatmentStatsRequest request, Integer pageNo, Integer pageSizeNo) {
        List<Map<String, Object>> all = new ArrayList<Map<String, Object>>();
        if (dataConfig.isMybatisPlus()) {
            try {
                // deptCode / deptName 传 null：页面这张表是全科室排名，不做科室过滤
                List<TreatmentStatsDtlEntity> rows =
                        treatmentStatsMapper.queryDeptDetail(request.getStartDate(), request.getEndDate(),
                                null, null);
                int rank = 1;
                for (TreatmentStatsDtlEntity r : rows) {
                    Map<String, Object> item = new LinkedHashMap<String, Object>();
                    item.put("rank", Integer.valueOf(rank++));
                    item.put("deptName", r.getDeptName());
                    item.put("patientCount", nvl(r.getPatientCount()));
                    item.put("treatmentCount", nvl(r.getTreatmentCount()));
                    item.put("treatmentAmount", r.getTreatmentAmount() == null
                            ? BigDecimal.ZERO : r.getTreatmentAmount());
                    item.put("avgAmount", r.getAvgAmount() == null
                            ? BigDecimal.ZERO : r.getAvgAmount());
                    all.add(item);
                }
            } catch (Exception ex) {
                log.warn("查询治疗统计科室明细失败", ex);
            }
        }
        return page(all, pageNo, pageSizeNo);
    }

    // ==================== 工具方法 ====================

    private static Map<String, Object> page(List<Map<String, Object>> all, Integer pageNo,
                                            Integer pageSizeNo) {
        int page = pageNo == null || pageNo.intValue() < 1 ? 1 : pageNo.intValue();
        int size = pageSizeNo == null || pageSizeNo.intValue() < 1 ? 10 : pageSizeNo.intValue();
        int total = all.size();
        int from = Math.min((page - 1) * size, total);
        int to = Math.min(from + size, total);
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("list", new ArrayList<Map<String, Object>>(all.subList(from, to)));
        result.put("total", Integer.valueOf(total));
        result.put("page", Integer.valueOf(page));
        result.put("pageSize", Integer.valueOf(size));
        return result;
    }

    private static int nvl(Integer v) {
        return v == null ? 0 : v.intValue();
    }
}
