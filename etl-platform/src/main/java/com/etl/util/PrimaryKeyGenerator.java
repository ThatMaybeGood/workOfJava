package com.etl.util;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;

public class PrimaryKeyGenerator {

    public static Long nextId() {
        return IdWorker.getId();
    }

    public static String nextIdStr() {
        return IdWorker.getIdStr();
    }
}
