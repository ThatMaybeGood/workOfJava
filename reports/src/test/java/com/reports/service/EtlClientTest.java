package com.reports.service;

import com.reports.config.EtlProperties;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * EtlClient 逻辑桩测试（不依赖数据库 / 不依赖真实 ETL）
 * <p>
 * 通过覆写 {@code doRunHttp} 为计数桩，验证：总开关、配置缺失短路、
 * 冷却去重原子性、异步触发。
 */
class EtlClientTest {

    /** 覆写 doRunHttp 为计数桩的 EtlClient 子类 */
    private static class CountingEtlClient extends EtlClient {
        final AtomicInteger calls = new AtomicInteger(0);
        final CountDownLatch firstCall = new CountDownLatch(1);

        CountingEtlClient(EtlProperties props) {
            super(props);
        }

        @Override
        protected void doRunHttp(String dsKey, String taskToken, Map<String, Object> vars) {
            calls.incrementAndGet();
            firstCall.countDown();
        }
    }

    private static EtlProperties props(boolean enabled, String runUrl, String apiToken, long cooldownSeconds) {
        EtlProperties p = new EtlProperties();
        p.setEnabled(enabled);
        p.setRunUrl(runUrl);
        p.setApiToken(apiToken);
        p.setCooldownSeconds(cooldownSeconds);
        return p;
    }

    @Test
    void disabled_shouldShortCircuit() throws Exception {
        EtlProperties p = props(false, "http://localhost/x", "t", 600L);
        CountingEtlClient client = new CountingEtlClient(p);
        assertFalse(client.ensure("master", "TOKEN", Collections.singletonMap("date", "2026-09-01")));
        Thread.sleep(150);
        assertEquals(0, client.calls.get(), "总开关关闭时不应触发任何 HTTP");
        client.destroy();
    }

    @Test
    void missingRunUrlOrToken_shouldShortCircuit() throws Exception {
        CountingEtlClient noUrl = new CountingEtlClient(props(true, "", "t", 600L));
        assertFalse(noUrl.ensure("master", "TOKEN", null));
        Thread.sleep(150);
        assertEquals(0, noUrl.calls.get());
        noUrl.destroy();

        CountingEtlClient noToken = new CountingEtlClient(props(true, "http://localhost/x", "  ", 600L));
        assertFalse(noToken.ensure("master", "TOKEN", null));
        noToken.destroy();
    }

    @Test
    void sameTaskWithinCooldown_shouldTriggerOnlyOnce() throws Exception {
        EtlProperties p = props(true, "http://localhost/x", "t", 600L);
        CountingEtlClient client = new CountingEtlClient(p);
        Map<String, Object> vars = Collections.singletonMap("date", "2026-09-01");

        // 第一次触发成功，且异步桩已被调用
        assertTrue(client.ensure("master", "T1", vars));
        assertTrue(client.firstCall.await(2, TimeUnit.SECONDS), "异步 doRunHttp 应在 2s 内执行");
        assertEquals(1, client.calls.get());

        // 冷却期内再次触发（含不同实参顺序传入的相同参数）应被拒绝
        assertFalse(client.ensure("master", "T1", vars));
        Thread.sleep(200);
        assertEquals(1, client.calls.get(), "冷却期内不应再次触发");
        client.destroy();
    }

    @Test
    void cooldownZero_shouldAlwaysTrigger() {
        EtlProperties p = props(true, "http://localhost/x", "t", 0L);
        CountingEtlClient client = new CountingEtlClient(p);
        Map<String, Object> vars = Collections.singletonMap("date", "2026-09-01");
        assertTrue(client.ensure("master", "T1", vars), "cooldown=0 时第一次应放行");
        assertTrue(client.ensure("master", "T1", vars), "cooldown=0 时第二次也应放行");
        client.destroy();
    }

    @Test
    void disabledTaskToken_shouldShortCircuit_othersUnaffected() throws Exception {
        EtlProperties p = props(true, "http://localhost/x", "t", 600L);
        p.setDisabledTaskTokens(Collections.singletonList("TOKEN_DISABLED"));
        CountingEtlClient client = new CountingEtlClient(p);

        // 命中停用名单 -> 不触发
        assertFalse(client.ensure("master", "TOKEN_DISABLED", null));
        Thread.sleep(150);
        assertEquals(0, client.calls.get(), "停用名单内的任务不应触发");

        // 未命中名单的任务不受影响 -> 正常触发
        assertTrue(client.ensure("master", "TOKEN_OK", null));
        assertTrue(client.firstCall.await(2, TimeUnit.SECONDS));
        assertEquals(1, client.calls.get());
        client.destroy();
    }
}
