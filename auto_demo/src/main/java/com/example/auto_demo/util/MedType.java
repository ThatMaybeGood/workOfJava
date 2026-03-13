package com.example.auto_demo.util;

import java.util.HashMap;
import java.util.Map;

public enum MedType {

    TYPE_01("11", "普通门诊"),
    TYPE_02("1102", "新冠门诊"),
    TYPE_03("1901", "造口袋门诊"),
    TYPE_04("108", "辅助生殖门诊"),
    TYPE_05("12", "门诊挂号"),
    TYPE_06("13", "急诊"),
    TYPE_07("14", "门诊慢特病"),
    TYPE_08("19", "意外伤害门诊"),

    TYPE_09("21", "普通住院"),
    TYPE_10("24", "急诊转住院"),
    TYPE_11("26", "单病种住院"),

    // 生育类
    TYPE_12("51", "生育门诊"),
    TYPE_13("52", "生育住院"),
    TYPE_14("53", "计划生育手术费"),

    TYPE_15("9903", "儿童两病门诊"),
    TYPE_16("9904", "儿童两病住院"),
    TYPE_17("9907", "耐多药结核住院"),
    TYPE_18("9925", "转入住院"),
    TYPE_19("9921", "新生儿随母住院"),
    TYPE_20("9914", "职工两病"),
    TYPE_21("9922", "家庭病床"),

    TYPE_22("41", "定点药店购药"),

    TYPE_23("910202", "国家谈判药门诊"),
    TYPE_24("910302", "住院双通道外购药");

    private final String code;
    private final String name;

    private static final Map<String, String> CODE_TO_NAME_MAP = new HashMap<>();

    static {
        for (MedType type : MedType.values()) {
            CODE_TO_NAME_MAP.put(type.code, type.name);
        }
    }

    MedType(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static String nameOf(String code) {
        if (code == null) {
            return "";
        }
        return CODE_TO_NAME_MAP.getOrDefault(code, "");
    }


    public static Map<String, String> getAllMapping() {
        return new HashMap<>(CODE_TO_NAME_MAP);
    }


    public static boolean isValidCode(String code) {
        return CODE_TO_NAME_MAP.containsKey(code);
    }
}