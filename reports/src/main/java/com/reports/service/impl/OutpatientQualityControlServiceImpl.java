package com.reports.service.impl;

import com.reports.config.ReportDataConfig;
import com.reports.dto.common.PageResult;
import com.reports.dto.request.OutpatientQualityControlRequest;
import com.reports.dto.request.QualityControlMaintainRequest;
import com.reports.dto.response.outpatient.quality.control.*;
import com.reports.entity.QcMaintainEntity;
import com.reports.entity.QualityControlDtlEntity;
import com.reports.entity.QualityControlOvEntity;
import com.reports.mapper.QcMaintainMapper;
import com.reports.mapper.QualityControlMapper;
import com.reports.service.OutpatientQualityControlService;
import com.reports.util.SeqUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
    private QcMaintainMapper qcMaintainMapper;

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
            OverviewData data = buildOverviewData(entity);
            // 人工登记值覆盖ETL值
            Map<String, QcMaintainEntity> maintainMap = maintainMap(orCurrentMonth(request.getStartMonth()),
                    orCurrentMonth(request.getEndMonth()));
            for (String month : monthsBetween(orCurrentMonth(request.getStartMonth()),
                    orCurrentMonth(request.getEndMonth()))) {
                applyMaintain(data, month, maintainMap);
            }
            return data;
        } catch (Exception e) {
            log.warn("查询门诊质量控制概览失败", e);
            return new OverviewData();
        }
    }

    private PageResult<TableItem> queryTableByMybatisPlus(OutpatientQualityControlRequest request, Integer page, Integer pageSize) {
        try {
            List<QualityControlDtlEntity> rows = qualityControlMapper
                    .queryMonthlyDetail(orCurrentMonth(request.getStartMonth()),
                            orCurrentMonth(request.getEndMonth()));
            List<TableItem> allItems = new ArrayList<>();
            // 人工登记值覆盖ETL值
            Map<String, QcMaintainEntity> maintainMap = maintainMap(orCurrentMonth(request.getStartMonth()),
                    orCurrentMonth(request.getEndMonth()));
            for (QualityControlDtlEntity row : rows) {
                TableItem item = buildTableItem(row);
                applyMaintain(item, row.getStatMonth(), maintainMap);
                allItems.add(item);
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

    // ==================== 数据维护 ====================

    @Override
    public List<QcMaintainItem> queryMaintainList(QualityControlMaintainRequest request) {
        log.info("查询门诊质控维护明细，statMonth={}，mode={}", request.getStatMonth(), dataConfig.getMode());
        List<QcMaintainItem> result = new ArrayList<>();
        if (!dataConfig.isMybatisPlus()) {
            return result;
        }
        try {
            // 同月同指标多来源并存，SQL已按人工登记优先+最新排序，取首条
            List<String> seen = new ArrayList<>();
            for (QcMaintainEntity row : qcMaintainMapper.queryByMonth(request.getStatMonth())) {
                if (seen.contains(row.getIndicatorCode())) {
                    continue;
                }
                seen.add(row.getIndicatorCode());
                QcMaintainItem item = new QcMaintainItem();
                item.setIndicatorCode(row.getIndicatorCode());
                item.setIndicatorName(row.getIndicatorName());
                item.setNumerator(row.getNumerator());
                item.setDenominator(row.getDenominator());
                result.add(item);
            }
        } catch (Exception e) {
            log.warn("查询门诊质控维护明细失败", e);
        }
        return result;
    }

    @Override
    public int saveMaintain(QualityControlMaintainRequest request) {
        log.info("保存门诊质控维护明细，statMonth={}，mode={}", request.getStatMonth(), dataConfig.getMode());
        if (!dataConfig.isMybatisPlus() || request.getList() == null) {
            return 0;
        }
        // 追加式保存：同月同指标每次维护新增一行(人工登记)，保留历史，查询时人工登记优先
        int affected = 0;
        for (QcMaintainItem item : request.getList()) {
            QcMaintainEntity entity = new QcMaintainEntity();
            entity.setStatMonth(request.getStatMonth());
            entity.setIndicatorCode(item.getIndicatorCode());
            entity.setIndicatorName(item.getIndicatorName());
            entity.setNumerator(item.getNumerator());
            entity.setDenominator(item.getDenominator());
            entity.setRate(calcRate(item.getNumerator(), item.getDenominator()));
            entity.setSourceType("人工登记");
            affected += qcMaintainMapper.insert(entity);
        }
        return affected;
    }

    // ==================== 维护值覆盖 ====================

    /** 维护值映射：key = 月份|指标编码；SQL已按人工登记优先+最新排序，取首条即可 */
    private Map<String, QcMaintainEntity> maintainMap(String startMonth, String endMonth) {
        Map<String, QcMaintainEntity> map = new LinkedHashMap<>();
        try {
            for (QcMaintainEntity r : qcMaintainMapper.queryByRange(startMonth, endMonth)) {
                map.putIfAbsent(r.getStatMonth() + "|" + r.getIndicatorCode(), r);
            }
        } catch (Exception e) {
            log.warn("查询门诊质控维护值失败", e);
        }
        return map;
    }

    private static void applyMaintain(TableItem item, String month, Map<String, QcMaintainEntity> maintainMap) {
        setRate(rateOf(maintainMap, month, "emr_usage_rate"), item::setEmrUsageRate);
        setRate(rateOf(maintainMap, month, "standard_diagnosis_rate"), item::setStandardDiagnosisRate);
        setRate(rateOf(maintainMap, month, "on_time_rate"), item::setOnTimeRate);
        setRate(rateOf(maintainMap, month, "stop_rate"), item::setStopRate);
        setRate(rateOf(maintainMap, month, "chemo_record_rate"), item::setChemoRecordRate);
    }

    private static void applyMaintain(OverviewData data, String month, Map<String, QcMaintainEntity> maintainMap) {
        setRate(rateOf(maintainMap, month, "emr_usage_rate"), data::setEmrUsageRate);
        setRate(rateOf(maintainMap, month, "standard_diagnosis_rate"), data::setStandardDiagnosisRate);
        setRate(rateOf(maintainMap, month, "on_time_rate"), data::setOnTimeRate);
        setRate(rateOf(maintainMap, month, "stop_rate"), data::setStopRate);
        setRate(rateOf(maintainMap, month, "chemo_record_rate"), data::setChemoRecordRate);
    }

    private static String rateOf(Map<String, QcMaintainEntity> maintainMap, String month, String indicatorCode) {
        QcMaintainEntity r = maintainMap.get(month + "|" + indicatorCode);
        return r == null || r.getRate() == null ? null : r.getRate().setScale(2, RoundingMode.HALF_UP).toPlainString() + "%";
    }

    private static void setRate(String rate, java.util.function.Consumer<String> setter) {
        if (rate != null) {
            setter.accept(rate);
        }
    }

    /** 比率=分子/分母*100，分母为空或0时返回null */
    private static BigDecimal calcRate(BigDecimal numerator, BigDecimal denominator) {
        if (numerator == null || denominator == null
                || denominator.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        return numerator.multiply(new BigDecimal("100")).divide(denominator, 4, RoundingMode.HALF_UP);
    }

    /** 起止月份之间的月份列表（含端点） */
    private static List<String> monthsBetween(String startMonth, String endMonth) {
        List<String> months = new ArrayList<>();
        try {
            Calendar cal = Calendar.getInstance();
            cal.setTime(new SimpleDateFormat("yyyy-MM").parse(startMonth));
            Calendar end = Calendar.getInstance();
            end.setTime(new SimpleDateFormat("yyyy-MM").parse(endMonth));
            while (!cal.after(end)) {
                months.add(new SimpleDateFormat("yyyy-MM").format(cal.getTime()));
                cal.add(Calendar.MONTH, 1);
            }
        } catch (Exception e) {
            log.warn("生成月份区间失败", e);
        }
        return months;
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
        String m = orCurrentMonth(month);
        try {
            return new SimpleDateFormat("yyyy-MM-dd").parse(m + "-01");
        } catch (Exception e) {
            log.warn("解析月份起始日期失败: {}", month, e);
            return null;
        }
    }

    /**
     * 前端没传月份时兜底成当月。
     * 原来直接用 null 拼字符串会变成 "null-01"，SimpleDateFormat 抛
     * Unparseable date，概览和明细表就全空了。
     */
    private static String orCurrentMonth(String month) {
        if (month == null || month.trim().isEmpty()) {
            return new SimpleDateFormat("yyyy-MM").format(new Date());
        }
        return month.trim();
    }

    private Date parseMonthEnd(String month) {
        try {
            Date firstDay = new SimpleDateFormat("yyyy-MM-dd").parse(orCurrentMonth(month) + "-01");
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
