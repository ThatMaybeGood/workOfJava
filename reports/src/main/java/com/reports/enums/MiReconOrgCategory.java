package com.reports.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 医疗机构对账数据类别(HIS原始行)
 */
@Getter
@AllArgsConstructor
public enum MiReconOrgCategory {

    /** 账平 */
    BALANCED("1", "账平"),

    /** 医保负金额(对应HIS正交易) */
    MI_NEGATIVE("2", "医保负金额"),

    /** 医保正金额 */
    MI_POSITIVE("3", "医保正金额");

    private final String code;

    private final String chineseName;

    /**
     * 按代码或中文名取中文名(SQL直出中文时原样返回, 未知值原样返回, null返回null)
     */
    public static String nameOf(String value) {
        if (value == null) {
            return null;
        }
        String v = value.trim();
        for (MiReconOrgCategory c : values()) {
            if (c.getCode().equals(v) || c.getChineseName().equals(v)) {
                return c.getChineseName();
            }
        }
        return v;
    }

    /**
     * 排序序号: 代码或中文名匹配返回ordinal, 未匹配返回null
     */
    public static Integer ordinalOf(String value) {
        if (value == null) {
            return null;
        }
        String v = value.trim();
        for (MiReconOrgCategory c : values()) {
            if (c.getCode().equals(v) || c.getChineseName().equals(v)) {
                return c.ordinal();
            }
        }
        return null;
    }
}
