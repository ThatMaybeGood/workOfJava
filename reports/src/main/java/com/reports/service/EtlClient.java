package com.reports.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.reports.config.EtlProperties;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ETL 任务触发客户端（公共、尽力而为的“补数”能力）
 * <p>
 * 设计要点：
 * <ul>
 *   <li>非强校验：任何失败只记日志，绝不抛给调用方，不影响报表查询；</li>
 *   <li>异步：HTTP 调用在线程池执行，绝不阻塞请求线程；</li>
 *   <li>去重/节流：同一 (taskToken + 参数) 在 cooldownSeconds 内只触发一次，防并发/重复风暴；</li>
 *   <li>独立线程池，饱和策略 = 丢弃 + 告警，绝不反噬主流程。</li>
 * </ul>
 */
@Slf4j
@Component
public class EtlClient {

    /**
     * JSON 媒体类型（OkHttp 依据它自动设置 Content-Type）
     */
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    /**
     * 响应体在日志中的最大展示长度（避免超长 body 刷爆日志）
     */
    private static final int MAX_BODY_LOG_LENGTH = 200;

    private final EtlProperties etlProperties;
    private final OkHttpClient httpClient;
    private final ExecutorService executor;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 最近一次触发时间（epochMilli）：key = taskToken + 规范化参数，用于冷却去重
     */
    private final ConcurrentHashMap<String, Long> lastTriggerAt = new ConcurrentHashMap<>();

    /**
     * 构造注入配置，并创建 HTTP 客户端与独立触发线程池
     */
    @Autowired
    public EtlClient(EtlProperties etlProperties) {
        this.etlProperties = etlProperties;
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(etlProperties.getConnectTimeoutSeconds(), TimeUnit.SECONDS)
                .readTimeout(etlProperties.getReadTimeoutSeconds(), TimeUnit.SECONDS)
                .build();

        ThreadFactory threadFactory = new ThreadFactory() {
            private final AtomicInteger seq = new AtomicInteger(1);

            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r, "etl-trigger-" + seq.getAndIncrement());
                t.setDaemon(true);
                return t;
            }
        };
        this.executor = new ThreadPoolExecutor(
                2,                          // corePoolSize
                4,                          // maximumPoolSize
                60L, TimeUnit.SECONDS,      // 空闲线程回收时间
                new ArrayBlockingQueue<>(200), // 有界任务队列，防止堆积
                threadFactory,
                new EtlDiscardPolicy());    // 饱和策略：丢弃 + 告警，不阻塞调用方
    }

    /**
     * 确保 ETL 任务被触发（尽力而为，绝不抛异常给调用方）
     * <p>
     * 流程：短路检查（未启用 / 缺 runUrl / 缺 apiToken）→ 冷却去重 → 提交异步执行。
     *
     * @param dsKey      数据源标识（仅用于日志与告警上下文）
     * @param taskToken  ETL 任务标识
     * @param vars       任务参数（可为 null），参与去重 key 的生成
     * @return true = 本次已提交触发（或允许触发）；false = 未触发（未启用 / 冷却期内 / 内部异常）
     */
    public boolean ensure(String dsKey, String taskToken, Map<String, Object> vars) {
        try {
            // 1. 短路：未启用或缺少必填配置时直接跳过
            if (!etlProperties.isEnabled()
                    || !StringUtils.hasText(etlProperties.getRunUrl())
                    || !StringUtils.hasText(etlProperties.getApiToken())) {
                log.debug("ETL 触发未启用或配置缺失(runUrl/apiToken)，跳过，dsKey=[{}], taskToken=[{}]",
                        dsKey, taskToken);
                return false;
            }

            // 1.1 单任务停用名单：命中即跳过（只停某一个，其余不受影响）
            if (etlProperties.getDisabledTaskTokens() != null
                    && etlProperties.getDisabledTaskTokens().contains(taskToken)) {
                log.debug("ETL 任务已通过 disabled-task-tokens 停用，跳过，dsKey=[{}], taskToken=[{}]",
                        dsKey, taskToken);
                return false;
            }

            // 2. 冷却去重：同一 (taskToken + 参数) 在冷却期内只触发一次
            String key = buildTriggerKey(taskToken, vars);
            long now = System.currentTimeMillis();
            long cooldownMillis = etlProperties.getCooldownSeconds() * 1000L;
            // compute 对同一个 key 是原子的：并发下同 key 只会有一个线程把 allow 置为 true
            boolean[] allow = {false};
            lastTriggerAt.compute(key, (k, last) -> {
                if (last != null && (now - last) < cooldownMillis) {
                    return last; // 仍在冷却期内：保留旧时间戳，拒绝本次触发
                }
                allow[0] = true; // 首次触发或已过冷却期：放行
                return now;      // 记录本次触发时间
            });
            if (!allow[0]) {
                log.debug("ETL 同任务在冷却期内重复触发，已忽略，dsKey=[{}], taskToken=[{}], key=[{}]",
                        dsKey, taskToken, key);
                return false;
            }

            // 3. 提交到独立线程池异步执行（队列满时由 EtlDiscardPolicy 丢弃并告警，不阻塞调用方）
            executor.execute(new EtlTriggerTask(dsKey, taskToken, vars));
            return true;
        } catch (Exception e) {
            // 外层兜底：任何异常都不上抛，确保不影响报表查询主流程
            log.warn("ETL 触发调用异常（已吞掉，不影响主流程），dsKey=[{}], taskToken=[{}], msg=[{}]",
                    dsKey, taskToken, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 实际执行 HTTP 触发调用（protected 以便单测覆写为桩）
     * <p>
     * 组装 JSON：{"taskToken":"...","vars":{...}}，POST 到 runUrl，
     * header 携带 X-API-Token 与 Content-Type。2xx 记 info，其它状态码记 warn，
     * IO/运行时异常记 warn，全程不向外抛出。
     */
    protected void doRunHttp(String dsKey, String taskToken, Map<String, Object> vars) {
        try {
            // 组装请求体
            Map<String, Object> payload = new HashMap<>(4);
            payload.put("taskToken", taskToken);
            payload.put("vars", vars == null ? Collections.emptyMap() : vars);
            String json;
            try {
                json = objectMapper.writeValueAsString(payload);
            } catch (JsonProcessingException e) {
                log.warn("ETL 触发参数 JSON 序列化失败，dsKey=[{}], taskToken=[{}], msg=[{}]",
                        dsKey, taskToken, e.getMessage());
                return;
            }

            RequestBody requestBody = RequestBody.create(json, JSON);
            Request request = new Request.Builder()
                    .url(etlProperties.getRunUrl())
                    .header("X-API-Token", etlProperties.getApiToken())
                    .header("Content-Type", "application/json")
                    .post(requestBody)
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                int code = response.code();
                String respBody = (response.body() == null) ? "" : response.body().string();

                // 尽力从响应体解析 logId / status 用于日志（非 JSON 时忽略，不影响主流程）
                String logId = null;
                String respStatus = null;
                if (StringUtils.hasText(respBody)) {
                    try {
                        JsonNode root = objectMapper.readTree(respBody);
                        if (root != null) {
                            JsonNode node = root.get("logId");
                            if (node != null) {
                                logId = node.asText();
                            }
                            node = root.get("status");
                            if (node != null) {
                                respStatus = node.asText();
                            }
                        }
                    } catch (IOException ignored) {
                        // 响应体非 JSON，忽略解析失败
                    }
                }

                if (code >= 200 && code < 300) {
                    log.info("ETL 任务触发成功，dsKey=[{}], taskToken=[{}], httpStatus=[{}], logId=[{}], respStatus=[{}]",
                            dsKey, taskToken, code, logId, respStatus);
                    if (isExplicitFailure(respStatus)) {
                        log.warn("ETL 接口返回 2xx 但业务状态明确为失败（该任务未触发成功），"
                                        + "dsKey=[{}], taskToken=[{}], respStatus=[{}], respBody=[{}]",
                                dsKey, taskToken, respStatus, truncate(respBody));
                    }
                } else {
                    log.warn("ETL 接口返回非 2xx，任务可能未触发成功，dsKey=[{}], taskToken=[{}], "
                                    + "httpStatus=[{}], respBody=[{}]",
                            dsKey, taskToken, code, truncate(respBody));
                }
            }
        } catch (IOException e) {
            log.warn("ETL HTTP 调用失败，任务未触发成功，dsKey=[{}], taskToken=[{}], msg=[{}]",
                    dsKey, taskToken, e.getMessage(), e);
        } catch (RuntimeException e) {
            log.warn("ETL 触发执行发生运行时异常，任务未触发成功，dsKey=[{}], taskToken=[{}], msg=[{}]",
                    dsKey, taskToken, e.getMessage(), e);
        }
    }

    /**
     * 优雅关闭线程池：等待在途任务结束，超时后强制关闭
     */
    @PreDestroy
    public void destroy() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
                log.warn("ETL 触发线程池在 5s 内未完全结束，已强制关闭");
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 构建冷却去重 key：taskToken + "#" + 规范化参数（key 排序后按 k=v&… 拼接，仅含非 null）
     */
    private String buildTriggerKey(String taskToken, Map<String, Object> vars) {
        StringBuilder sb = new StringBuilder(taskToken).append('#');
        if (vars != null && !vars.isEmpty()) {
            List<String> segments = new ArrayList<>(vars.size());
            for (Map.Entry<String, Object> entry : vars.entrySet()) {
                if (entry.getKey() != null && entry.getValue() != null) {
                    segments.add(entry.getKey() + "=" + entry.getValue());
                }
            }
            Collections.sort(segments);
            sb.append(String.join("&", segments));
        }
        return sb.toString();
    }

    /**
     * 判断业务响应状态是否为“明确失败”，用于在 2xx 时额外告警
     */
    private static boolean isExplicitFailure(String respStatus) {
        if (!StringUtils.hasText(respStatus)) {
            return false;
        }
        String s = respStatus.trim();
        return "FAILED".equalsIgnoreCase(s) || "FAIL".equalsIgnoreCase(s)
                || "FAILURE".equalsIgnoreCase(s) || "ERROR".equalsIgnoreCase(s);
    }

    /**
     * 截断超长字符串，用于日志打印
     */
    private static String truncate(String s) {
        if (s == null) {
            return "";
        }
        return s.length() > MAX_BODY_LOG_LENGTH ? s.substring(0, MAX_BODY_LOG_LENGTH) : s;
    }

    /**
     * ETL 触发异步任务（携带上下文便于被丢弃时打印告警）
     */
    private class EtlTriggerTask implements Runnable {
        private final String dsKey;
        private final String taskToken;
        private final Map<String, Object> vars;

        EtlTriggerTask(String dsKey, String taskToken, Map<String, Object> vars) {
            this.dsKey = dsKey;
            this.taskToken = taskToken;
            this.vars = vars;
        }

        @Override
        public void run() {
            doRunHttp(dsKey, taskToken, vars);
        }
    }

    /**
     * 自定义饱和策略：队列满 / 线程池关闭时丢弃新任务并告警，绝不阻塞调用方
     */
    private class EtlDiscardPolicy extends ThreadPoolExecutor.DiscardPolicy {
        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            if (r instanceof EtlTriggerTask) {
                EtlTriggerTask task = (EtlTriggerTask) r;
                log.warn("ETL 触发线程池已满或已关闭，丢弃任务，dsKey=[{}], taskToken=[{}]",
                        task.dsKey, task.taskToken);
            } else {
                log.warn("ETL 触发线程池已满或已关闭，丢弃未知任务");
            }
            super.rejectedExecution(r, executor);
        }
    }
}
