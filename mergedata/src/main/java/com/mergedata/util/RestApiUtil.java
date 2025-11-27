package com.mergedata.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mergedata.entity.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.*;

import java.util.Collections;

/**
 * 通用 RestTemplate 调用工具类，负责封装请求、发起调用、处理HTTP错误和捕获业务异常。
 */
@Component
@Slf4j
public class RestApiUtil {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper; // 注入或创建 ObjectMapper 用于 String 解析

    /**
     * 【方法一：推荐】使用 ParameterizedTypeReference 发起通用的 POST 请求。
     * 适用于返回结构（ApiResponse<T> 中的 T）是复杂泛型（如 List<HisIncomeDTO>）的情况。
     *
     * @param url             API 服务的 URL
     * @param requestBody     请求体对象 (T)
     * @param responseTypeRef 期望的返回类型（包含泛型）
     * @param <Req>           请求体类型
     * @param <Res>           响应体类型
     * @return 响应体对象 (Res)
     * @throws BusinessException 如果调用失败（包括业务异常和HTTP/连接异常）
     */
    public <Req, Res> Res postForObject(
            String url,
            Req requestBody,
            ParameterizedTypeReference<Res> responseTypeRef) throws BusinessException {

        log.info("开始调用外部 API (TypeReference): {}", url);

        // 1. 封装 HttpEntity
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        HttpEntity<Req> requestEntity = new HttpEntity<>(requestBody, headers);

        try {
            // 2. 发起调用
            ResponseEntity<Res> responseEntity = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    responseTypeRef
            );

            // 3. 检查 HTTP 状态码
            if (responseEntity.getStatusCode().is2xxSuccessful() && responseEntity.getBody() != null) {
                return responseEntity.getBody();
            } else {
                // 如果 HTTP 状态码不是 2xx，但 RestTemplate 没抛出异常（极少发生，通常会被捕获）
                String msg = String.format("API 调用成功，但返回体为空或状态码异常。URL: %s, Status: %s",
                        url, responseEntity.getStatusCode());
                log.error(msg);
                throw new BusinessException(msg);
            }

        } catch (ResourceAccessException e) {
            log.error("调用失败，网络或连接问题（服务可能未启动或连接超时）: {}", url, e);
            throw new BusinessException("HIS接口连接失败: " + e.getMessage());
        } catch (HttpClientErrorException e) {
            log.error("调用失败，客户端错误 (状态码: {}). URL: {}", e.getStatusCode(), url, e);
            throw new BusinessException("HIS接口客户端错误: " + e.getStatusCode() + ". Response: " + e.getResponseBodyAsString().trim());
        }
        // 👇 新增或修改：明确捕获 Jackson 导致的序列化/反序列化失败
        catch (
                HttpMessageNotReadableException e) {
            log.error("调用失败，Jackson 反序列化/数据结构错误: {}", url, e);
            // 这里通常就是 BigDecimal 错误发生的地方
            throw new BusinessException("HIS接口数据解析失败 (Jackson): " + e.getLocalizedMessage());
        } catch (HttpServerErrorException e) {
            log.error("调用失败，服务器端错误 (状态码: {}). URL: {}", e.getStatusCode(), url, e);
            throw new BusinessException("HIS接口服务端错误: " + e.getStatusCode() + ". Response: " + e.getResponseBodyAsString().trim());
        } catch (RestClientException e) {
            log.error("调用失败，数据解析或通用 RestTemplate 错误: {}", url, e);
            throw new BusinessException("HIS接口通用错误: " + e.getMessage());
        } catch (BusinessException e) {
            // 捕获并重新抛出，以便上层服务能感知
            throw e;
        } catch (Exception e) {
            log.error("调用发生未知错误: {}", url, e);
            throw new BusinessException("HIS接口未知错误: " + e.getMessage());
        }
    }

    /**
     * 【方法二：可选】发起通用的 POST 请求，接收 String 响应体。
     * 适用于需要手动进行多次 JSON 解析（例如：先解析 Code，再根据 Code 解析不同的 Body 结构）的情况。
     *
     * @param url         API 服务的 URL
     * @param requestBody 请求体对象 (Req)
     * @param <Req>       请求体类型
     * @return 原始的 String 响应体
     * @throws BusinessException 如果调用失败
     */
    public <Req> String postForString(String url, Req requestBody) throws BusinessException {
        log.info("开始调用外部 API (String): {}", url);

        // 1. 封装 HttpEntity
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        HttpEntity<Req> requestEntity = new HttpEntity<>(requestBody, headers);

        try {
            // 2. 发起调用，接收 String
            ResponseEntity<String> responseEntity = restTemplate.exchange(
                    url, HttpMethod.POST, requestEntity, String.class
            );

            // 3. 检查 HTTP 状态码
            if (responseEntity.getStatusCode().is2xxSuccessful() && responseEntity.getBody() != null) {
                return responseEntity.getBody();
            } else {
                String msg = String.format("API 调用成功，但返回体为空或状态码异常。URL: %s, Status: %s",
                        url, responseEntity.getStatusCode());
                log.error(msg);
                throw new BusinessException(msg);
            }

        } catch (ResourceAccessException e) {
            log.error("调用失败，网络或连接问题（服务可能未启动或连接超时）: {}", url, e);
            throw new BusinessException("HIS接口连接失败: " + e.getMessage());
        } catch (HttpClientErrorException e) {
            log.error("调用失败，客户端错误 (状态码: {}). URL: {}", e.getStatusCode(), url, e);
            throw new BusinessException("HIS接口客户端错误: " + e.getStatusCode() + ". Response: " + e.getResponseBodyAsString().trim());
        } catch (HttpServerErrorException e) {
            log.error("调用失败，服务器端错误 (状态码: {}). URL: {}", e.getStatusCode(), url, e);
            throw new BusinessException("HIS接口服务端错误: " + e.getStatusCode() + ". Response: " + e.getResponseBodyAsString().trim());
        } catch (RestClientException e) {
            log.error("调用失败，数据解析或通用 RestTemplate 错误: {}", url, e);
            throw new BusinessException("HIS接口通用错误: " + e.getMessage());
        } catch (Exception e) {
            log.error("调用发生未知错误: {}", url, e);
            throw new BusinessException("HIS接口未知错误: " + e.getMessage());
        }
    }
}