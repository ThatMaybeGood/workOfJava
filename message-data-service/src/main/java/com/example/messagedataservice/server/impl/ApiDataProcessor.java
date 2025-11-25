package com.example.messagedataservice.server.impl;

import com.example.messagedataservice.config.ApiProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class ApiDataProcessor {

    private final WebClient webClient;
    private final ApiProperties apiProperties; // 注入配置属性
    private final ObjectMapper objectMapper;

    // 构造函数注入所有依赖
    public ApiDataProcessor(WebClient webClient, ApiProperties apiProperties, ObjectMapper objectMapper) {
        this.webClient = webClient;
        this.apiProperties = apiProperties;
        this.objectMapper = objectMapper;
    }

    /**
     * 【核心方法】并行调用多个外部接口并组装结果
     * @return 包含所有聚合数据的 CombinedResult DTO
     */
//    public Void aggregateAllData() {
//
//        // 1. 定义三个并行的异步调用 Mono
//        Mono<HisData> hisDataMono = callApi("hisData", HisData.class);
//        Mono<HolidayCalendar> calendarMono = callApi("holidayCalendar", HolidayCalendar.class);
//        Mono<YQCashRegRecord> cashRegMono = callApi("yqCashReg", YQCashRegRecord.class);
//
//        // 2. 使用 Mono.zip() 组合所有 Mono，并行发起请求
//        Mono<CombinedResult> combinedMono = Mono.zip(hisDataMono, calendarMono, cashRegMono)
//                // 3. 将三个结果的元组 (Tuple) 转换为 CombinedResult DTO
//                .map(t3 -> {
//                    CombinedResult result = new CombinedResult();
//                    // t3.getT1(), t3.getT2(), t3.getT3() 对应三个 Mono 的结果
//                    result.setHisData(t3.getT1());
//                    result.setCalendarData(t3.getT2());
//                    result.setCashRegRecord(t3.getT3());
//                    return result;
//                })
//                // 4. 处理容错：如果任何一个 Mono 失败，返回一个包含错误信息的默认结果
//                .onErrorResume(e -> {
//                    System.err.println("聚合调用失败: " + e.getMessage());
//                    return Mono.just(new CombinedResult("FAILED: 部分或全部接口调用失败"));
//                });
//
//        // 5. 阻塞等待最终结果 (适用于 Spring WebMVC)
////        return combinedMono.block();
//          return null; // 实际应用中，此处应返回 combinedMono.block(); 以阻塞等待结果
//    }

    // ----------------------------------------------------------------------
    // 辅助泛型方法：动态构建 URL 并执行 API 调用
    // ----------------------------------------------------------------------

    /**
     * 根据 API Code 动态查找 URL，并执行 WebClient GET 请求
     * @param apiCode 在 application.yml 中配置的 key (例如: "hisData")
     * @param responseType 外部接口返回的目标 DTO 类
     * @return 包含目标 DTO 的 Mono
     */
    private <T> Mono<T> callApi(String apiCode, Class<T> responseType) {
        String baseUrl = apiProperties.getBaseUrls().get(apiCode);
        String endpointPath = apiProperties.getEndpointPath();

        if (baseUrl == null) {
            // 如果配置缺失，立即返回错误 Mono
            return Mono.error(new RuntimeException("配置中缺少 API Code: " + apiCode + " 的基地址"));
        }

        // 构建完整的 URL (例如: http://his-service.com/api/v1/data)
        String fullUrl = baseUrl + endpointPath;

        return webClient.get()
                .uri(fullUrl)
                .retrieve()
                // 统一的错误处理：将 HTTP 4xx/5xx 状态码转换为异常
                .onStatus(status -> status.isError(),
                        response -> response.bodyToMono(String.class)
                                .flatMap(body -> Mono.error(new RuntimeException(
                                        "API 调用失败[" + apiCode + "]: Status " + response.statusCode()))))
                .bodyToMono(responseType);
    }
}