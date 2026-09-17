package com.reports.service.impl;

import com.reports.config.ReportDataConfig;
import com.reports.dto.common.PageResult;
import com.reports.dto.request.OutpatientQualityControlRequest;
import com.reports.dto.request.QualityControlMaintainRequest;
import com.reports.dto.response.outpatient.quality.control.*;
import com.reports.entity.QualityControlDtlEntity;
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
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * 门诊管理质量控制服务实现
 */
@Slf4j
@Service
public class OutpatientQualityControlServiceImpl implements OutpatientQualityControlService {

    /** 数据维护弹窗支持的指标：编码 -> 名称 */
    private static final Map<String, String> MAINTAIN_INDICATORS = new LinkedHashMap<>();

    static {
        MAINTAIN_INDICATORS.put("emr_usage_rate", "门诊电子病历使用率");
        MAINTAIN_INDICATORS.put("standard_diagnosis_rate", "门诊标准诊断使用率");
        MAINTAIN_INDICATORS.put("on_time_rate", "门诊准时出诊率");
        MAINTAIN_INDICATORS.put("stop_rate", "门诊停诊率");
        MAINTAIN_INDICATORS.put("chemo_record_rate", "门诊化疗病历记录完整率");
    }

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
            return buildOverviewData(queryMonthlyItems(orCurrentMonth(request.getStartMonth()),
                    orCurrentMonth(request.getEndMonth())));
        } catch (Exception e) {
            log.warn("查询门诊质量控制概览失败", e);
            return new OverviewData();
        }
    }

    /** 月度明细；概览取值与分页查询共用，保证两处口径一致 */
    private List<TableItem> queryMonthlyItems(String startMonth, String endMonth) {
        return qualityControlMapper.queryMonthly(startMonth, endMonth);
    }

    private PageResult<TableItem> queryTableByMybatisPlus(OutpatientQualityControlRequest request, Integer page, Integer pageSize) {
        try {
            List<TableItem> allItems = queryMonthlyItems(orCurrentMonth(request.getStartMonth()),
                    orCurrentMonth(request.getEndMonth()));
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
            QualityControlDtlEntity row = qualityControlMapper.queryByMonth(request.getStatMonth());
            for (String code : MAINTAIN_INDICATORS.keySet()) {
                result.add(toMaintainItem(code, row));
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
        QualityControlDtlEntity entity = new QualityControlDtlEntity();
        entity.setStatMonth(request.getStatMonth());
        for (QcMaintainItem item : request.getList()) {
            fillMaintain(entity, item);
        }
        return qualityControlMapper.mergeMaintain(entity);
    }

    /** 该月某指标的分子分母 -> 维护弹窗的行；该月还没数据时行内数值为空 */
    static QcMaintainItem toMaintainItem(String code, QualityControlDtlEntity row) {
        QcMaintainItem item = new QcMaintainItem();
        item.setIndicatorCode(code);
        item.setIndicatorName(MAINTAIN_INDICATORS.get(code));
        if (row == null) {
            return item;
        }
        switch (code) {
            case "emr_usage_rate":
                item.setNumerator(row.getEmrUsageRateNum());
                item.setDenominator(row.getEmrUsageRateDen());
                break;
            case "standard_diagnosis_rate":
                item.setNumerator(row.getStandardDiagnosisRateNum());
                item.setDenominator(row.getStandardDiagnosisRateDen());
                break;
            case "on_time_rate":
                item.setNumerator(row.getOnTimeRateNum());
                item.setDenominator(row.getOnTimeRateDen());
                break;
            case "stop_rate":
                item.setNumerator(row.getStopRateNum());
                item.setDenominator(row.getStopRateDen());
                break;
            case "chemo_record_rate":
                item.setNumerator(row.getChemoRecordRateNum());
                item.setDenominator(row.getChemoRecordRateDen());
                break;
            default:
                break;
        }
        return item;
    }

    /** 维护弹窗提交的行 -> 待写入的分子分母；没填的指标不动 */
    static void fillMaintain(QualityControlDtlEntity entity, QcMaintainItem item) {
        if (item == null || item.getIndicatorCode() == null) {
            return;
        }
        switch (item.getIndicatorCode()) {
            case "emr_usage_rate":
                entity.setEmrUsageRateNum(item.getNumerator());
                entity.setEmrUsageRateDen(item.getDenominator());
                break;
            case "standard_diagnosis_rate":
                entity.setStandardDiagnosisRateNum(item.getNumerator());
                entity.setStandardDiagnosisRateDen(item.getDenominator());
                break;
            case "on_time_rate":
                entity.setOnTimeRateNum(item.getNumerator());
                entity.setOnTimeRateDen(item.getDenominator());
                break;
            case "stop_rate":
                entity.setStopRateNum(item.getNumerator());
                entity.setStopRateDen(item.getDenominator());
                break;
            case "chemo_record_rate":
                entity.setChemoRecordRateNum(item.getNumerator());
                entity.setChemoRecordRateDen(item.getDenominator());
                break;
            default:
                break;
        }
    }

    // ==================== entity -> DTO 转换方法 ====================

    /** 概览 = 区间内各月指标值的算术平均，无数据的月份跳过 */
    private OverviewData buildOverviewData(List<TableItem> items) {
        OverviewData data = new OverviewData();
        data.setEmrUsageRate(average(items, TableItem::getEmrUsageRate));
        data.setStandardDiagnosisRate(average(items, TableItem::getStandardDiagnosisRate));
        data.setOnTimeRate(average(items, TableItem::getOnTimeRate));
        data.setStopRate(average(items, TableItem::getStopRate));
        data.setChemoRecordRate(average(items, TableItem::getChemoRecordRate));
        data.setChemoAdverseRate(average(items, TableItem::getChemoAdverseRate));
        data.setChemoInfusionRate(average(items, TableItem::getChemoInfusionRate));
        data.setCriticalValueRate(average(items, TableItem::getCriticalValueRate));
        data.setBloodDrawErrorRate(average(items, TableItem::getBloodDrawErrorRate));
        data.setSurgeryComplicationRate(average(items, TableItem::getSurgeryComplicationRate));
        data.setAdverseEventRate(average(items, TableItem::getAdverseEventRate));
        return data;
    }

    /** 单列平均值；无有效值返回 null */
    static String average(List<TableItem> items, Function<TableItem, String> getter) {
        BigDecimal sum = BigDecimal.ZERO;
        int count = 0;
        for (TableItem item : items) {
            BigDecimal value = parseRate(getter.apply(item));
            if (value != null) {
                sum = sum.add(value);
                count++;
            }
        }
        if (count == 0) {
            return null;
        }
        return sum.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP).toPlainString() + "%";
    }

    /** 比率文案解析成数值；取不到数值返回 null */
    private static BigDecimal parseRate(String raw) {
        if (raw == null) {
            return null;
        }
        String text = raw.trim();
        if (text.endsWith("%")) {
            text = text.substring(0, text.length() - 1).trim();
        }
        if (text.isEmpty()) {
            return null;
        }
        try {
            return new BigDecimal(text);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // ==================== 日期转换方法 ====================

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

}
