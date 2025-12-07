package com.mergedata.util;

import com.baomidou.mybatisplus.annotation.EnumValue;

public enum ValidStatusEnum {
    // 0: 无效
    INVALID(0),
    // 1: 有效
    VALID(1);

    // 存入数据库的值
    @EnumValue
    private final Integer code;

    ValidStatusEnum(Integer code) {
        this.code = code;
    }
}