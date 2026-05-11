package com.demo.integration.core.trace;

/**
 * @author Mine
 * @version 1.0
 * 描述:
 * @date 2026/5/9 23:05
 */
public class TraceIdGenerator {

    private static final SnowflakeIdWorker WORKER =
            new SnowflakeIdWorker(1, 1);

    public static String generate(
            String prefix,
            int length) {

        long id = WORKER.nextId();

        String num = String.valueOf(id);

        if (num.length() > length) {

            num = num.substring(
                    num.length() - length);
        }

        return prefix + num;
    }
}