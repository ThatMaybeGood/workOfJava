package com.mergedata.util;

import com.mergedata.model.dto.external.HisOutpIncomeResponseDTO;
import com.mergedata.model.entity.YQOperatorEntity;
import com.mergedata.model.entity.YQHolidayEntity;
import com.mergedata.model.entity.YQCashRegRecordEntity;

/*
 * 定义枚举，用于标识不同类型的 API
 */
public enum ApiType {
    HIS_DATA("hisData", HisOutpIncomeResponseDTO.class),
    // 增加枚举值，例如 HIS_REPORT
    HIS_REPORT("hisReport", YQOperatorEntity.class),
    // 增加枚举值，例如 HOLIDAY_CALENDAR
    HOLIDAY_CALENDAR("holidayCalendar", YQHolidayEntity.class),

    // 增加新的枚举值，例如 HIS_OPERATOR
    HIS_OPERATOR("hisOperator", YQOperatorEntity.class),

    YQ_CASH_REG("yqCashReg", YQCashRegRecordEntity.class),
        // 未知类型，用于处理外部接口返回的新增枚举值
    UNKNOWN("unknown", Object.class);




    // 内部字段
    private final String code;
    private final Class<?> targetClass; // 增加目标类属性，便于后续转换

    ApiType(String code, Class<?> targetClass) {
        this.code = code;
        this.targetClass = targetClass;
    }

    public String getCode() {
        return code;
    }

    public Class<?> getTargetClass() {
        return targetClass;
    }

    // 【关键转换方法】: 将外部字符串转换为枚举实例
    public static ApiType fromCode(String code) {
        for (ApiType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        // 如果外部接口返回了新的未知类型，避免抛异常，返回 UNKNOWN
        return UNKNOWN;
    }
}
