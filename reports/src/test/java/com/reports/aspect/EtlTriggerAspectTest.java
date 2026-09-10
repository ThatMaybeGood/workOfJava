package com.reports.aspect;

import com.reports.annotation.EtlTask;
import com.reports.config.EtlProperties;
import com.reports.service.EtlClient;
import org.apache.ibatis.annotations.Param;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * EtlTriggerAspect 桩验证（不依赖数据库）
 * <p>
 * 用程序化 Spring 容器 + {@code @EnableAspectJAutoProxy} 启动 AOP（无 DB、无 MyBatis），
 * 通过普通 Bean 方法验证：空集合触发、Map 判空、非空不触发、白名单 vars 抽取、冷却期不重复触发；
 * 另反射验证判空方法对 null/数组/标量的覆盖。
 */
class EtlTriggerAspectTest {

    /** 覆写 doRunHttp 的计数 EtlClient */
    static class CountingEtlClient extends EtlClient {
        final AtomicInteger calls = new AtomicInteger(0);
        final CountDownLatch latch = new CountDownLatch(1);
        volatile String lastToken;
        volatile Map<String, Object> lastVars;

        CountingEtlClient(EtlProperties props) {
            super(props);
        }

        @Override
        protected void doRunHttp(String dsKey, String taskToken, Map<String, Object> vars, String traceId) {
            lastToken = taskToken;
            lastVars = vars;
            calls.incrementAndGet();
            latch.countDown();
        }
    }

    /** 普通 Bean 目标：方法上标注 @EtlTask，用于验证 AOP 拦截（mapper 代理差异留真实环境验证） */
    public static class Target {
        @EtlTask(taskToken = "T_EMPTY", params = {"date"})
        public List<String> emptyWithWhitelist(@Param("date") String date, @Param("extra") String extra) {
            return Collections.emptyList();
        }

        @EtlTask(taskToken = "T_NONEMPTY")
        public List<String> nonEmpty(@Param("date") String date) {
            return Collections.singletonList("x");
        }

        @EtlTask(taskToken = "T_MAPEMPTY")
        public Map<String, Object> emptyMap(@Param("a") String a) {
            return Collections.emptyMap();
        }

        @EtlTask(taskToken = "T_DATE_FMT", params = {"statDate"}, dateFormats = {"statDate=yyyy-MM-dd"})
        public List<String> emptyWithDateFmt(@Param("statDate") Date statDate) {
            return Collections.emptyList();
        }

        @EtlTask(taskToken = "T_DATE_RAW", params = {"statDate"})
        public List<String> emptyWithDateRaw(@Param("statDate") Date statDate) {
            return Collections.emptyList();
        }
    }

    @Configuration
    @EnableAspectJAutoProxy
    static class Cfg {
        static EtlProperties props;
        static CountingEtlClient client;

        @Bean
        EtlProperties props() {
            return props;
        }

        @Bean
        EtlClient etlClient() {
            return client;
        }

        @Bean
        EtlTriggerAspect etlTriggerAspect(EtlClient c) {
            return new EtlTriggerAspect(c);
        }

        @Bean
        Target target() {
            return new Target();
        }
    }

    private AnnotationConfigApplicationContext ctx;

    private CountingEtlClient start(CountingEtlClient client, EtlProperties props) {
        Cfg.props = props;
        Cfg.client = client;
        ctx = new AnnotationConfigApplicationContext(Cfg.class);
        return client;
    }

    private static EtlProperties props(long cooldownSeconds) {
        EtlProperties p = new EtlProperties();
        p.setEnabled(true);
        p.setRunUrl("http://localhost/etl");
        p.setApiToken("t");
        p.setCooldownSeconds(cooldownSeconds);
        return p;
    }

    @AfterEach
    void tearDown() {
        if (ctx != null) {
            ctx.close();
            ctx = null;
        }
    }

    @Test
    void emptyCollection_shouldTriggerEtlOnce_withWhitelistVars() throws Exception {
        CountingEtlClient client = new CountingEtlClient(props(600L));
        start(client, props(600L));

        Target target = ctx.getBean(Target.class);
        List<String> result = target.emptyWithWhitelist("2026-09-01", "IGNORE_ME");
        assertTrue(result.isEmpty());

        assertTrue(client.latch.await(3, TimeUnit.SECONDS), "异步 doRunHttp 应在 3s 内执行");
        assertEquals(1, client.calls.get());
        assertEquals("T_EMPTY", client.lastToken);
        // 白名单只透传 date，extra 应被过滤
        assertEquals(Collections.singletonMap("date", "2026-09-01"), client.lastVars);

        // 冷却期内、同一条件再次调用同一任务 -> 不重复触发（冷却 key 含参数，条件相同才拦截）
        target.emptyWithWhitelist("2026-09-01", "IGNORE_ME");
        Thread.sleep(250);
        assertEquals(1, client.calls.get(), "冷却期内、同条件下不应重复触发");
    }

    @Test
    void nonEmptyResult_shouldNotTrigger() throws Exception {
        CountingEtlClient client = new CountingEtlClient(props(600L));
        start(client, props(600L));

        Target target = ctx.getBean(Target.class);
        List<String> result = target.nonEmpty("2026-09-01");
        assertEquals(Collections.singletonList("x"), result);
        Thread.sleep(300);
        assertEquals(0, client.calls.get(), "查询结果非空时不应触发 ETL");
    }

    @Test
    void emptyMap_shouldAlsoTrigger() throws Exception {
        CountingEtlClient client = new CountingEtlClient(props(600L));
        start(client, props(600L));

        Target target = ctx.getBean(Target.class);
        Map<String, Object> result = target.emptyMap("v");
        assertTrue(result.isEmpty());
        assertTrue(client.latch.await(3, TimeUnit.SECONDS));
        assertEquals(1, client.calls.get());
        assertEquals("T_MAPEMPTY", client.lastToken);
    }

    @Test
    void dateParam_withDateFormats_shouldFormatToString() throws Exception {
        CountingEtlClient client = new CountingEtlClient(props(600L));
        start(client, props(600L));
        Target target = ctx.getBean(Target.class);

        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.set(2026, Calendar.SEPTEMBER, 1); // 2026-09-01
        Date d = cal.getTime();

        List<String> result = target.emptyWithDateFmt(d);
        assertTrue(result.isEmpty());
        assertTrue(client.latch.await(3, TimeUnit.SECONDS));
        assertEquals(Collections.singletonMap("statDate", "2026-09-01"), client.lastVars,
                "dateFormats 命中时应把 Date 格式化为 yyyy-MM-dd 字符串");
    }

    @Test
    void dateParam_withoutDateFormats_shouldPassThroughRaw() throws Exception {
        CountingEtlClient client = new CountingEtlClient(props(600L));
        start(client, props(600L));
        Target target = ctx.getBean(Target.class);

        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.set(2026, Calendar.SEPTEMBER, 1);
        Date d = cal.getTime();

        List<String> result = target.emptyWithDateRaw(d);
        assertTrue(result.isEmpty());
        assertTrue(client.latch.await(3, TimeUnit.SECONDS));
        assertEquals(d, client.lastVars.get("statDate"), "未声明 dateFormats 时 Date 应原样透传");
    }

    @Test
    void isEmptyResult_shouldCoverCollectionMapOptionalArrayButNotNullOrScalar() throws Exception {
        // 仅验证判空私有逻辑，ensure 不会被真正调用（传入 null client 即可，不触发）
        EtlTriggerAspect aspect = new EtlTriggerAspect(null);
        Method m = EtlTriggerAspect.class.getDeclaredMethod("isEmptyResult", Object.class);
        m.setAccessible(true);

        assertTrue((Boolean) m.invoke(aspect, Collections.emptyList()));
        assertTrue((Boolean) m.invoke(aspect, Collections.emptyMap()));
        assertTrue((Boolean) m.invoke(aspect, Optional.empty()));
        assertTrue((Boolean) m.invoke(aspect, new int[0]));

        assertFalse((Boolean) m.invoke(aspect, Collections.singletonList("x")));
        Map<String, Object> nonEmptyMap = new HashMap<>();
        nonEmptyMap.put("k", "v");
        assertFalse((Boolean) m.invoke(aspect, nonEmptyMap));
        assertFalse((Boolean) m.invoke(aspect, Optional.of("v")));
        assertFalse((Boolean) m.invoke(aspect, new Object[]{1}));
        assertFalse((Boolean) m.invoke(aspect, (Object) null), "null 结果不触发（只有集合/Map/Optional/数组判空）");
        assertFalse((Boolean) m.invoke(aspect, "plainString"), "标量不触发");
    }
}
