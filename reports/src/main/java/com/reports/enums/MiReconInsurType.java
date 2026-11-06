package com.reports.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 医保对账类型
 */
@Getter
@AllArgsConstructor
public enum MiReconInsurType {

    /** 职工 */
    WORKER("310", "职工"),

    /** 居民 */
    RESIDENT("390", "居民"),

    /** 异地职工 */
    WORKER_NONLOCAL("3101", "异地职工"),

    /** 异地居民 */
    RESIDENT_NONLOCAL("3901", "异地居民"),

    /** 生育 */
    MATERNITY("515253", "生育");

    private final String code;

    private final String chineseName;

    /**
     * 按代码查找, 无效代码返回null
     */
    public static MiReconInsurType ofCode(String code) {
        if (code == null) {
            return null;
        }
        for (MiReconInsurType type : values()) {
            if (type.getCode().equals(code.trim())) {
                return type;
            }
        }
        return null;
    }
}
