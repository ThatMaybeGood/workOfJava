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
     * 按代码取中文名, 未知代码原样返回, null返回null
     */
    public static String nameOf(String code) {
        if (code == null) {
            return null;
        }
        for (MiReconOrgCategory c : values()) {
            if (c.getCode().equals(code.trim())) {
                return c.getChineseName();
            }
        }
        return code;
    }
}
