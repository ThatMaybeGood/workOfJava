package com.reports.service.impl;

import com.reports.dto.response.outpatient.quality.control.QcMaintainItem;
import com.reports.dto.response.outpatient.quality.control.TableItem;
import com.reports.entity.QualityControlDtlEntity;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * 门诊质控的区间平均、维护弹窗预填与回写三段纯逻辑，不依赖数据库。
 */
class OutpatientQualityControlServiceImplTest {

    private static TableItem item(String month, String value) {
        TableItem tableItem = new TableItem();
        tableItem.setMonth(month);
        tableItem.setEmrUsageRate(value);
        return tableItem;
    }

    private static QcMaintainItem maintainItem(String code, String numerator, String denominator) {
        QcMaintainItem maintain = new QcMaintainItem();
        maintain.setIndicatorCode(code);
        maintain.setNumerator(numerator == null ? null : new BigDecimal(numerator));
        maintain.setDenominator(denominator == null ? null : new BigDecimal(denominator));
        return maintain;
    }

    @Test
    void averageSkipsEmptyMonthsAndRoundsToTwoDecimals() {
        List<TableItem> items = Arrays.asList(
                item("2025-08", "60.00%"),
                item("2025-09", null),
                item("2025-10", "63.00%"));

        assertEquals("61.50%", OutpatientQualityControlServiceImpl
                .average(items, TableItem::getEmrUsageRate));
    }

    @Test
    void averageOfSingleMonthIsItself() {
        assertEquals("62.00%", OutpatientQualityControlServiceImpl
                .average(Collections.singletonList(item("2025-09", "62.00%")), TableItem::getEmrUsageRate));
    }

    @Test
    void averageReturnsNullWhenNothingUsable() {
        assertNull(OutpatientQualityControlServiceImpl
                .average(Arrays.asList(item("2025-08", null), item("2025-09", "  ")),
                        TableItem::getEmrUsageRate));
        assertNull(OutpatientQualityControlServiceImpl
                .average(Collections.emptyList(), TableItem::getEmrUsageRate));
    }

    /** 该月还没有数据时，维护弹窗也要列出全部指标，只是数值为空 */
    @Test
    void maintainItemsAreListedEvenWhenMonthHasNoRow() {
        QcMaintainItem maintain = OutpatientQualityControlServiceImpl
                .toMaintainItem("standard_diagnosis_rate", null);

        assertEquals("standard_diagnosis_rate", maintain.getIndicatorCode());
        assertEquals("门诊标准诊断使用率", maintain.getIndicatorName());
        assertNull(maintain.getNumerator());
        assertNull(maintain.getDenominator());
    }

    @Test
    void maintainItemReadsTheMatchingNumDenColumns() {
        QualityControlDtlEntity row = new QualityControlDtlEntity();
        row.setStandardDiagnosisRateNum(new BigDecimal("830"));
        row.setStandardDiagnosisRateDen(new BigDecimal("1000"));
        row.setOnTimeRateNum(new BigDecimal("900"));

        QcMaintainItem maintain = OutpatientQualityControlServiceImpl
                .toMaintainItem("standard_diagnosis_rate", row);

        assertEquals(0, new BigDecimal("830").compareTo(maintain.getNumerator()));
        assertEquals(0, new BigDecimal("1000").compareTo(maintain.getDenominator()));
    }

    @Test
    void fillMaintainWritesOnlyTheSubmittedIndicators() {
        QualityControlDtlEntity entity = new QualityControlDtlEntity();
        entity.setStatMonth("2025-09");
        OutpatientQualityControlServiceImpl.fillMaintain(entity,
                maintainItem("stop_rate", "12", "1000"));

        assertEquals(0, new BigDecimal("12").compareTo(entity.getStopRateNum()));
        assertEquals(0, new BigDecimal("1000").compareTo(entity.getStopRateDen()));
        assertNull(entity.getEmrUsageRateNum());
        assertNull(entity.getOnTimeRateNum());
    }

    @Test
    void fillMaintainIgnoresUnknownIndicator() {
        QualityControlDtlEntity entity = new QualityControlDtlEntity();
        OutpatientQualityControlServiceImpl.fillMaintain(entity, maintainItem("not_an_indicator", "1", "2"));
        OutpatientQualityControlServiceImpl.fillMaintain(entity, null);

        assertNull(entity.getEmrUsageRateNum());
        assertNull(entity.getStopRateNum());
    }
}
