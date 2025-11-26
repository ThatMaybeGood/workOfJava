package com.mergedata.util;

import com.mergedata.dto.HisIncomeDTO;
import com.mergedata.dto.YQOperatorDTO;
import com.mergedata.dto.HolidayCalendarDTO;
import com.mergedata.dto.YQCashRegRecordDTO;

/*
 * 定义一个枚举，用于标识不同类型的 API
 */
public enum ApiType {
    HIS_DATA("hisData", HisIncomeDTO.class),
    // 增加枚举值，例如 HIS_REPORT
    HIS_REPORT("hisReport", YQOperatorDTO.class),
    // 增加枚举值，例如 HOLIDAY_CALENDAR
    HOLIDAY_CALENDAR("holidayCalendar", HolidayCalendarDTO.class),

    // 增加新的枚举值，例如 HIS_OPERATOR
    HIS_OPERATOR("hisOperator", YQOperatorDTO.class),

    YQ_CASH_REG("yqCashReg", YQCashRegRecordDTO.class),
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
