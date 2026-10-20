package com.reports.service.impl;

import com.reports.config.ReportDataConfig;
import com.reports.dto.common.PageResult;
import com.reports.dto.request.OutpatientNoShowRequest;
import com.reports.dto.response.outpatient.no.show.*;
import com.reports.service.OutpatientNoShowService;
import com.reports.mapper.NoShowMapper;
import com.reports.util.SeqUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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

    /** 全度量缓存: 同一区间10min只算一次, 概览/科室表/属地/渠道/年龄五处共用(源表直查较重) */
    private static final long MEASURE_TTL = 10 * 60 * 1000L;
    private static final int MEASURE_CACHE_MAX = 20;
    private final Map<String, long[]> measureCacheAt = new ConcurrentHashMap<>();
    private final Map<String, List<Map<String, Object>>> measureCache = new ConcurrentHashMap<>();

    private List<Map<String, Object>> queryMeasuresCached(OutpatientNoShowRequest request) {
        java.sql.Date start = sqlDay(request.getStartDate());
        java.sql.Date end = sqlDay(request.getEndDate());
        String key = start.getTime() + "-" + end.getTime();
        long now = System.currentTimeMillis();
        long[] at = measureCacheAt.get(key);
        if (at != null && now - at[0] <= MEASURE_TTL) {
            return measureCache.get(key);
        }
        List<Map<String, Object>> rows = noShowMapper.queryMeasureRange(start, end);
        if (measureCache.size() >= MEASURE_CACHE_MAX) {
            measureCache.clear();
            measureCacheAt.clear();
        }
        measureCacheAt.put(key, new long[]{now});
        measureCache.put(key, rows);
        return rows;
    }

    private OverviewData queryOverviewByMybatisPlus(OutpatientNoShowRequest request) {
        try {
            List<Map<String, Object>> rows = queryMeasuresCached(request);
            double total = sumOf(rows, "totalGuahao");
            double refund = sumOf(rows, "tuiHaoShu");
            double noShow = sumOf(rows, "shuangYueShu");
            OverviewData dto = new OverviewData();
            dto.setRefundCount((int) refund);
            dto.setRefundRate(pct(refund, total));
            dto.setNoShowCount((int) noShow);
            dto.setNoShowRate(pct(noShow, total));
            return dto;
        } catch (Exception e) {
            log.warn("查询爽约退号概览数据失败", e);
            return new OverviewData();
        }
    }

    private AgeAnalysis queryAgeAnalysisByMybatisPlus(OutpatientNoShowRequest request) {
        try {
            List<Map<String, Object>> rows = queryMeasuresCached(request);
            // 与原有行为一致: 年龄图只展示爽约侧(refundCount列原本就未上图)
            AgeAnalysis analysis = new AgeAnalysis();
            List<String> categories = new ArrayList<>();
            List<Integer> data = new ArrayList<>();
            for (int i = 0; i < AGE_GROUPS.length; i++) {
                categories.add(AGE_GROUPS[i]);
                data.add((int) sumOf(rows, AGE_KEYS[i]));
            }
            analysis.setCategories(categories);
            analysis.setData(data);
            return analysis;
        } catch (Exception e) {
            log.warn("查询爽约退号年龄分析失败", e);
            return new AgeAnalysis();
        }
    }

    private List<AnalysisItem> queryRefundOriginByMybatisPlus(OutpatientNoShowRequest request) {
        try {
            List<Map<String, Object>> rows = queryMeasuresCached(request);
            // 退号属地(原有行为只出退号侧); 与原SQL一致: 合计为0的属地不出项
            List<AnalysisItem> result = new ArrayList<>();
            for (int i = 0; i < ORIGIN_NAMES.length; i++) {
                int v = (int) sumOf(rows, REFUND_ORIGIN_KEYS[i]);
                if (v > 0) {
                    result.add(newAnalysisItem(ORIGIN_NAMES[i], v));
                }
            }
            result.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));
            return result;
        } catch (Exception e) {
            log.warn("查询爽约退号来源分析失败", e);
            return new ArrayList<>();
        }
    }

    private List<AnalysisItem> queryRefundChannelByMybatisPlus(OutpatientNoShowRequest request) {
        try {
            List<Map<String, Object>> rows = queryMeasuresCached(request);
            List<AnalysisItem> result = new ArrayList<>();
            int window = (int) sumOf(rows, "tuiHaoChuangKou");
            int mini = (int) sumOf(rows, "tuiHaoZiZhuJi");
            if (window > 0) {
                result.add(newAnalysisItem("窗口", window));
            }
            if (mini > 0) {
                result.add(newAnalysisItem("自助机", mini));
            }
            result.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));
            return result;
        } catch (Exception e) {
            log.warn("查询爽约退号渠道分析失败", e);
            return new ArrayList<>();
        }
    }

    private PageResult<TableItem> queryTableByMybatisPlus(OutpatientNoShowRequest request, Integer page, Integer pageSize) {
        try {
            List<Map<String, Object>> rows = queryMeasuresCached(request);
            Map<String, String> nameByCode = deptNameMap();
            // 按科室累计: deptCode为NULL(爽约情况一)不入科室表, 无字典的科室同INNER JOIN丢弃
            Map<String, double[]> acc = new LinkedHashMap<>();
            Map<String, String> codeName = new LinkedHashMap<>();
            String deptCodeFilter = request.getDeptCode() == null ? "" : request.getDeptCode().trim();
            String deptNameFilter = request.getDeptName() == null ? "" : request.getDeptName().trim();
            for (Map<String, Object> row : rows) {
                Object dc = row.get("deptCode");
                if (dc == null) {
                    continue;
                }
                String code = String.valueOf(dc);
                String name = nameByCode.get(code);
                if (name == null) {
                    continue;
                }
                if (!deptCodeFilter.isEmpty() && !code.equals(deptCodeFilter)) {
                    continue;
                }
                if (!deptNameFilter.isEmpty() && !name.contains(deptNameFilter)) {
                    continue;
                }
                codeName.put(code, name);
                double[] a = acc.computeIfAbsent(code, k -> new double[REFUND_ORIGIN_KEYS.length * 2 + 5]);
                a[0] += toDouble(row.get("tuiHaoShu"));
                a[1] += toDouble(row.get("shuangYueShu"));
                a[2] += toDouble(row.get("totalGuahao"));
                a[3] += toDouble(row.get("tuiHaoChuangKou"));
                a[4] += toDouble(row.get("tuiHaoZiZhuJi"));
                for (int i = 0; i < REFUND_ORIGIN_KEYS.length; i++) {
                    a[5 + i] += toDouble(row.get(REFUND_ORIGIN_KEYS[i]));
                    a[10 + i] += toDouble(row.get(NO_SHOW_ORIGIN_KEYS[i]));
                }
            }
            List<TableItem> allItems = new ArrayList<>();
            for (Map.Entry<String, double[]> entry : acc.entrySet()) {
                allItems.add(buildTableItem(codeName.get(entry.getKey()), entry.getValue()));
            }
            allItems.sort((x, y) -> Integer.compare(
                    y.getRefundCount() == null ? 0 : y.getRefundCount(),
                    x.getRefundCount() == null ? 0 : x.getRefundCount()));
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

    // ==================== 度量行聚合工具 ====================

    private static final String[] ORIGIN_NAMES = {"重庆", "四川", "贵州", "云南", "其他"};
    private static final String[] REFUND_ORIGIN_KEYS =
            {"tuiHaoChongqing", "tuiHaoSichuan", "tuiHaoGuizhou", "tuiHaoYunnan", "tuiHaoQiTa"};
    private static final String[] NO_SHOW_ORIGIN_KEYS =
            {"shuangYueChongqing", "shuangYueSichuan", "shuangYueGuizhou", "shuangYueYunnan", "shuangYueQiTa"};
    private static final String[] AGE_GROUPS = {"0-14", "15-19", "20-29", "30-39", "40-49", "50-59", "60-69", "70-79", "80-89", "90+"};
    private static final String[] AGE_KEYS =
            {"shuangYueAge0_14", "shuangYueAge15_19", "shuangYueAge20_29", "shuangYueAge30_39", "shuangYueAge40_49",
             "shuangYueAge50_59", "shuangYueAge60_69", "shuangYueAge70_79", "shuangYueAge80_89", "shuangYueAge90Up"};

    private double sumOf(List<Map<String, Object>> rows, String key) {
        double s = 0;
        for (Map<String, Object> row : rows) {
            s += toDouble(row.get(key));
        }
        return s;
    }

    private double toDouble(Object v) {
        return v == null ? 0.0 : ((Number) v).doubleValue();
    }

    /** 百分比: 与原SQL ROUND(x/NULLIF(sum,0)*100,2)||'%' 一致(分母0给0%) */
    private static String pct(double num, double den) {
        if (den <= 0) {
            return "0%";
        }
        return BigDecimal.valueOf(num / den * 100).setScale(2, RoundingMode.HALF_UP)
                .stripTrailingZeros().toPlainString() + "%";
    }

    private Map<String, String> deptNameMap() {
        Map<String, String> map = new HashMap<>();
        for (Map<String, Object> row : noShowMapper.queryDeptDict()) {
            Object code = row.get("deptCode");
            Object name = row.get("deptName");
            if (code != null) {
                map.put(String.valueOf(code), name == null ? "" : String.valueOf(name));
            }
        }
        return map;
    }

    private TableItem buildTableItem(String deptName, double[] a) {
        TableItem item = new TableItem();
        item.setDeptName(deptName);
        item.setRefundCount((int) a[0]);
        item.setRefundRate(pct(a[0], a[2]));
        item.setNoShowCount((int) a[1]);
        item.setNoShowRate(pct(a[1], a[2]));
        // 页面的科室表读的是嵌套结构（row.refundOrigin.chongqing 等），
        // 不填这三个 Map 的话前端会抛 TypeError 导致整张表渲染不出来
        item.setRefundOrigin(originMap((int) a[5], (int) a[6], (int) a[7], (int) a[8], (int) a[9], (int) a[0]));
        item.setNoShowOrigin(originMap((int) a[10], (int) a[11], (int) a[12], (int) a[13], (int) a[14], (int) a[1]));
        Map<String, Integer> channel = new LinkedHashMap<String, Integer>();
        channel.put("window", (int) a[3]);
        channel.put("miniprogram", (int) a[4]);
        item.setRefundChannel(channel);
        return item;
    }

    /** 归属地明细：值形如 "357 (30%)"，与页面其它地方的展示口径一致。 */
    private Map<String, String> originMap(Integer chongqing, Integer sichuan, Integer guizhou,
                                          Integer yunnan, Integer other, int total) {
        Map<String, String> map = new LinkedHashMap<String, String>();
        map.put("chongqing", withPercent(chongqing, total));
        map.put("sichuan", withPercent(sichuan, total));
        map.put("guizhou", withPercent(guizhou, total));
        map.put("yunnan", withPercent(yunnan, total));
        map.put("other", withPercent(other, total));
        return map;
    }

    private static String withPercent(Integer value, int total) {
        int v = nvlInt(value);
        int pct = total <= 0 ? 0 : (int) Math.round(v * 100.0d / total);
        return v + " (" + pct + "%)";
    }

    private static int nvlInt(Integer v) {
        return v == null ? 0 : v.intValue();
    }

    // ==================== 工具方法 ====================

    private AnalysisItem newAnalysisItem(String name, int value) {
        AnalysisItem item = new AnalysisItem();
        item.setName(name);
        item.setValue(value);
        return item;
    }


    /** java.util.Date→java.sql.Date：绑定按DATE类型发送， 避免TIMESTAMP绑定触发的列隐式转换致索引失效 */
    private static java.sql.Date sqlDay(java.util.Date d) {
        return d == null ? null : new java.sql.Date(d.getTime());
    }
}
