package com.demo.integration.core.retry;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

/**
 * @author Mine
 * @version 1.0
 * 描述:
 * @date 2026/5/9 23:05
 */
@Component
@Slf4j
public class RetryExecutor {

    public <T> T execute(
            int retryCount,
            int interval,
            Supplier<T> supplier) {

        Exception lastException = null;

        for (int i = 1; i <= retryCount; i++) {

            try {

                return supplier.get();

            } catch (Exception e) {

                lastException = e;

                log.error("第{}次请求失败", i, e);

                try {

                    Thread.sleep(interval);

                } catch (InterruptedException ex) {

                    Thread.currentThread().interrupt();
                }
            }
        }

        throw new RuntimeException(
                "超过最大重试次数",
                lastException);
    }
}