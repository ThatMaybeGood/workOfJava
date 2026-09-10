package com.reports.etl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * ETL 接口批量灌数
 * <p>
 * 外层遍历日期范围，内层遍历任务表，逐个调用 {@code POST /api/etl/run}：
 * <pre>
 *   {"taskToken":"任务调用ID","vars":{"statDate":"2026-07-01"}}
 * </pre>
 * 每个任务每天调用一次，等上一个跑完再发下一个（接口对同一任务有并发防护）。
 * <p>
 * 用法：填好 RUN_URL / API_TOKEN / CASES 里的任务调用ID，改 RANGES 的日期范围，然后
 * {@code mvn test -Dtest=EtlBatchTriggerTest}。
 */
class EtlBatchTriggerTest {

    private static final Logger log = LoggerFactory.getLogger(EtlBatchTriggerTest.class);

    // ==================== 1. 接口与环境 ====================

    /** ETL 外部调用接口地址 */
    private static final String RUN_URL = "http://localhost:18090/api/etl/run";

    /** 渠道令牌，请求头 X-API-Token；内网按环境替换 */
    private static final String API_TOKEN = "0cb06f7a29064e62a5de34d484296456";

    /** 轮询间隔与超时（秒）：触发后按 pollUrl 查询，直到 SUCCESS / FAILED / CANCELLED */
    private static final int POLL_INTERVAL_SECONDS = 3;
    private static final int POLL_TIMEOUT_SECONDS = 300;

    // ==================== 2. 时间范围（闭区间，逐日展开） ====================

    /** 每个区间 {起始日, 结束日}，都是 yyyy-MM-dd；要补多段（如当期 + 同比）就往下加一行 */
    private static final String[][] RANGES = {
            {"2026-07-01", "2026-07-31"},
    };

    /**
     * 日期占位符：写在 Case 的 vars 值里，表示「这里填循环的当天日期」，发出前替换成 2026-07-01 这种。
     * 没写占位符的 Case 不按天展开，只调一次。
     */
    private static final String DATE = "{date}";

    // ==================== 3. 任务表（每个任务一行） ====================

    /**
     * 一个任务一行：任务调用ID + 变量表。变量按「键, 值, 键, 值…」成对写，键名必须是任务声明过的入参。
     * <pre>
     *   new Case("任务调用ID", vars("statDate", DATE))                           → {"statDate":"当天"}
     *   new Case("任务调用ID", vars("billType", "1", "statDate", DATE))          → {"billType":"1","statDate":"当天"}
     *   new Case("任务调用ID", vars("startDate", "2026-07-01"))                  → 不展开，只调一次
     * </pre>
     */
    private static final Case[] CASES = {
            new Case("填任务调用ID", vars("statDate", DATE)),
    };

    // ==================== 4. 主流程 ====================

    @Test
    void batch_call() throws IOException, InterruptedException {
        OkHttpClient httpClient = new OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .build();

        List<String> days = days();
        log.info("开始批量调用：{} 天 × {} 个任务 = {} 次", days.size(), CASES.length, days.size() * CASES.length);

        int ok = 0;
        int fail = 0;
        for (String day : days) {                 // 外层：日期范围
            for (Case c : CASES) {                // 内层：任务表
                if (call(httpClient, c.taskToken, fill(c.vars, day))) {
                    ok++;
                } else {
                    fail++;
                }
            }
        }
        log.info("批量调用结束：成功={}, 失败={}", ok, fail);
    }

    /** 展开 RANGES 得到要跑的所有日期 */
    private List<String> days() {
        List<String> days = new ArrayList<>();
        for (String[] range : RANGES) {
            LocalDate from = LocalDate.parse(range[0], DAY);
            LocalDate to = LocalDate.parse(range[1], DAY);
            if (to.isBefore(from)) {
                throw new IllegalArgumentException("日期区间非法：" + range[0] + " ~ " + range[1]);
            }
            for (LocalDate d = from; !d.isAfter(to); d = d.plusDays(1)) {
                days.add(d.format(DAY));
            }
        }
        return days;
    }

    /** 把 Case 里的日期占位符换成当天日期，其余变量原样保留 */
    private Map<String, Object> fill(Map<String, Object> template, String day) {
        Map<String, Object> vars = new LinkedHashMap<>();
        if (template == null) {
            return vars;
        }
        for (Map.Entry<String, Object> entry : template.entrySet()) {
            vars.put(entry.getKey(), DATE.equals(entry.getValue()) ? day : entry.getValue());
        }
        return vars;
    }

    /**
     * 调一次接口：组报文 → POST → 轮询到终态。
     *
     * @return true = 执行成功
     */
    private boolean call(OkHttpClient httpClient, String taskToken, Map<String, Object> vars)
            throws IOException, InterruptedException {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("taskToken", taskToken);
        if (!vars.isEmpty()) {
            payload.put("vars", vars);
        }
        byte[] body = MAPPER.writeValueAsBytes(payload);
        log.info("请求报文：POST {}, X-API-Token={}, body={}", RUN_URL, API_TOKEN,
                new String(body, java.nio.charset.StandardCharsets.UTF_8));

        Request request = new Request.Builder()
                .url(RUN_URL)
                .header("X-API-Token", API_TOKEN)
                .header("Content-Type", "application/json")
                .post(RequestBody.create(body, JSON))
                .build();

        String pollUrl;
        Long logId;
        try (Response response = httpClient.newCall(request).execute()) {
            String respBody = (response.body() == null) ? "" : response.body().string();
            JsonNode root = MAPPER.readTree(respBody);
            JsonNode result = (root == null) ? null : root.get("result");
            boolean accepted = result != null && result.get("success") != null && result.get("success").asBoolean();
            if (!accepted) {
                log.warn("调用被拒绝：body={}, resp={}",
                        new String(body, java.nio.charset.StandardCharsets.UTF_8), respBody);
                return false;
            }
            JsonNode resBody = root.get("body");
            logId = (resBody != null && resBody.get("logId") != null) ? resBody.get("logId").asLong() : null;
            pollUrl = text(resBody, "pollUrl");
            log.info("触发成功：logId={}, status={}, pollUrl={}", logId, text(resBody, "status"), pollUrl);
        }

        if (pollUrl == null || pollUrl.trim().isEmpty()) {
            log.warn("响应未返回 pollUrl，跳过轮询：logId={}", logId);
            return false;
        }
        String url = URI.create(RUN_URL).resolve(pollUrl.trim()).toString();

        long deadline = System.currentTimeMillis() + POLL_TIMEOUT_SECONDS * 1000L;
        while (System.currentTimeMillis() < deadline) {
            Thread.sleep(POLL_INTERVAL_SECONDS * 1000L);
            Request poll = new Request.Builder().url(url).header("X-API-Token", API_TOKEN).get().build();
            try (Response response = httpClient.newCall(poll).execute()) {
                String respBody = (response.body() == null) ? "" : response.body().string();
                JsonNode root = MAPPER.readTree(respBody);
                JsonNode resBody = (root == null) ? null : root.get("body");
                String status = text(resBody, "status");
                if (status == null || !TERMINAL.contains(status.toUpperCase())) {
                    continue;
                }
                if ("SUCCESS".equalsIgnoreCase(status)) {
                    log.info("执行成功：logId={}", logId);
                    return true;
                }
                log.warn("执行未成功：logId={}, status={}, errorMsg={}", logId, status, text(resBody, "errorMsg"));
                return false;
            } catch (IOException e) {
                log.warn("轮询失败（继续重试）：logId={}, msg={}", logId, e.getMessage());
            }
        }
        log.warn("轮询超时 {}s：logId={}, pollUrl={}", POLL_TIMEOUT_SECONDS, logId, pollUrl);
        return false;
    }

    private String text(JsonNode node, String field) {
        if (node == null || !node.isObject()) {
            return null;
        }
        JsonNode value = node.get(field);
        return (value == null || value.isNull()) ? null : value.asText();
    }

    private static final Set<String> TERMINAL = new HashSet<>(Arrays.asList("SUCCESS", "FAILED", "CANCELLED"));
    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final ObjectMapper MAPPER = new ObjectMapper();

    /** 一个任务：任务调用ID + 变量表（日期用 {@link #DATE} 占位） */
    private static final class Case {
        private final String taskToken;
        private final Map<String, Object> vars;

        private Case(String taskToken, Map<String, Object> vars) {
            this.taskToken = taskToken;
            this.vars = vars;
        }
    }

    /** 变量表：按「键, 值, 键, 值…」成对传 */
    private static Map<String, Object> vars(Object... keyValues) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (int i = 0; i < keyValues.length; i += 2) {
            map.put(String.valueOf(keyValues[i]), keyValues[i + 1]);
        }
        return map;
    }
}
