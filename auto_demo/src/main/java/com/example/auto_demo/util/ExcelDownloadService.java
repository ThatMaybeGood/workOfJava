package com.example.auto_demo.util;


import com.alibaba.excel.EasyExcel;
import com.alibaba.fastjson.JSON;
import com.example.auto_demo.config.AppConfig;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class ExcelDownloadService {

    @Resource
    private AppConfig appConfig;

    AppConfig config = new AppConfig();
    @PostConstruct
    public void init(){
        config = appConfig.getAppConfig() ;
    }


    public List<Map<Integer, String>> postExport(String url, Map<String, Object> data, Map<String, String> headerMap) {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                .build();

        try {
            // 构建表单请求体
            FormBody.Builder formBodyBuilder = new FormBody.Builder();
            for (Map.Entry<String, Object> entry : data.entrySet()) {
                formBodyBuilder.add(entry.getKey(), String.valueOf(entry.getValue()));
            }
            RequestBody formBody = formBodyBuilder.build();

            // 构建请求
            Request.Builder requestBuilder = new Request.Builder()
                    .url(url)
                    .post(formBody);

            // 添加必需的头信息
//            requestBuilder.addHeader("Accept", "application/json, text/plain, */*");
//            requestBuilder.addHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
//            requestBuilder.addHeader("Referer", "http://mas.cq.hsip.gov.cn/hds/N1703.html");
//            requestBuilder.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36");
//            requestBuilder.addHeader("X-XSRF-TOKEN", "530137d4-9687-48b7-9b20-3cf10ecb3f18");
//            requestBuilder.addHeader("Cookie", "XSRF-TOKEN=530137d4-9687-48b7-9b20-3cf10ecb3f18; SESSION=MzFmMzBkZmYtYzVkMi00ZjRlLWE3ODktNDkxZjQ2NDAyMjE1");
            // 添加必需的头信息

            requestBuilder.addHeader("Accept", "application/json, text/plain, */*");
            requestBuilder.addHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            requestBuilder.addHeader("Referer", config.getFrontUrl());
            requestBuilder.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36");
            requestBuilder.addHeader("X-XSRF-TOKEN", config.getToken());
            requestBuilder.addHeader("Cookie", "XSRF-TOKEN="+config.getToken()+"; SESSION="+config.getSession());

            // 添加自定义头
            for (Map.Entry<String, String> entry : headerMap.entrySet()) {
                requestBuilder.addHeader(entry.getKey(), entry.getValue());
            }

            log.info("调用两定平台入参：{}", JSON.toJSONString(data));
            log.info("调用两定平台header：{}", headerMap);
            log.info("调用两定平台url：{}", url);

            Request request = requestBuilder.build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    log.error("调用两定平台异常，状态码: {}，消息: {}", response.code(), response.message());
                    return new ArrayList<>();
                }

                // 检查响应类型
                String contentType = response.header("Content-Type", "");
                String contentDisposition = response.header("Content-Disposition", "");

                log.info("响应 Content-Type: {}", contentType);
                log.info("响应 Content-Disposition: {}", contentDisposition);

                // 获取响应体字节
                byte[] excelBytes = response.body().bytes();
                log.info("获取到Excel文件，大小: {} 字节", excelBytes.length);

                // 将文件临时保存（用于调试）
                try {
                    String tempFileName = "temp_excel_" + System.currentTimeMillis() + ".xlsx";
                    Files.write(Paths.get(tempFileName), excelBytes);
                    log.info("Excel文件已临时保存为: {}", tempFileName);
                } catch (Exception e) {
                    log.warn("无法保存临时文件: {}", e.getMessage());
                }

                // 使用 EasyExcel 读取 Excel 内容
                try (InputStream inputStream = new ByteArrayInputStream(excelBytes)) {
                    List<Map<Integer, String>> list = EasyExcel.read(inputStream)
                            .sheet()  // 读取第一个sheet
                            .headRowNumber(0)  // 从第0行开始读取数据
                            .doReadSync();

                    log.info("成功读取Excel数据，共 {} 行", list.size());

                    // 打印前几行数据（用于调试）
                    if (!list.isEmpty()) {
                        for (int i = 0; i < Math.min(list.size(), 3); i++) {
                            log.info("第 {} 行数据: {}", i, list.get(i));
                        }
                    }

                    return list;
                }

            } catch (IOException e) {
                log.error("调用两定平台IO异常: ", e);
                return new ArrayList<>();
            }

        } catch (Exception e) {
            log.error("调用两定平台异常: ", e);
            return new ArrayList<>();
        }
    }
    // 方法重载：支持指定sheet索引
    public List<Map<Integer, String>> postExport(String url, Map<String, Object> data,
                                                 Map<String, String> headerMap, int sheetNo) {
        OkHttpClient client = new OkHttpClient();

        FormBody.Builder formBodyBuilder = new FormBody.Builder();
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            formBodyBuilder.add(entry.getKey(), String.valueOf(entry.getValue()));
        }
        RequestBody formBody = formBodyBuilder.build();

        Request.Builder requestBuilder = new Request.Builder()
                .url(url)
                .post(formBody);

        // 添加头信息
        requestBuilder.addHeader("Accept", "application/json, text/plain, */*");
        requestBuilder.addHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        requestBuilder.addHeader("X-XSRF-TOKEN", "530137d4-9687-48b7-9b20-3cf10ecb3f18");
        requestBuilder.addHeader("Cookie", "XSRF-TOKEN=530137d4-9687-48b7-9b20-3cf10ecb3f18; SESSION=MzFmMzBkZmYtYzVkMi00ZjRlLWE3ODktNDkxZjQ2NDAyMjE1");

        for (Map.Entry<String, String> entry : headerMap.entrySet()) {
            requestBuilder.addHeader(entry.getKey(), entry.getValue());
        }

        Request request = requestBuilder.build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                log.error("调用失败，状态码: " + response.code());
                return new ArrayList<>();
            }

            byte[] excelBytes = response.body().bytes();

            try (InputStream inputStream = new ByteArrayInputStream(excelBytes)) {
                List<Map<Integer, String>> list = EasyExcel.read(inputStream)
                        .sheet(sheetNo)  // 指定sheet索引
                        .headRowNumber(0)
                        .doReadSync();

                log.info("成功读取Excel第" + sheetNo + "个sheet，共" + list.size() + "行");
                return list;
            }

        } catch (IOException e) {
            log.error("调用异常: ", e);
            return new ArrayList<>();
        }
    }

    // 方法重载：支持指定sheet名称
    public List<Map<Integer, String>> postExport(String url, Map<String, Object> data,
                                                 Map<String, String> headerMap, String sheetName) {
        OkHttpClient client = new OkHttpClient();

        FormBody.Builder formBodyBuilder = new FormBody.Builder();
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            formBodyBuilder.add(entry.getKey(), String.valueOf(entry.getValue()));
        }
        RequestBody formBody = formBodyBuilder.build();

        Request.Builder requestBuilder = new Request.Builder()
                .url(url)
                .post(formBody);

        // 添加头信息
        requestBuilder.addHeader("Accept", "application/json, text/plain, */*");
        requestBuilder.addHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        requestBuilder.addHeader("X-XSRF-TOKEN", "530137d4-9687-48b7-9b20-3cf10ecb3f18");
        requestBuilder.addHeader("Cookie", "XSRF-TOKEN=530137d4-9687-48b7-9b20-3cf10ecb3f18; SESSION=MzFmMzBkZmYtYzVkMi00ZjRlLWE3ODktNDkxZjQ2NDAyMjE1");

        for (Map.Entry<String, String> entry : headerMap.entrySet()) {
            requestBuilder.addHeader(entry.getKey(), entry.getValue());
        }

        Request request = requestBuilder.build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                log.error("调用失败，状态码: " + response.code());
                return new ArrayList<>();
            }

            byte[] excelBytes = response.body().bytes();

            try (InputStream inputStream = new ByteArrayInputStream(excelBytes)) {
                List<Map<Integer, String>> list = EasyExcel.read(inputStream)
                        .sheet(sheetName)  // 指定sheet名称
                        .headRowNumber(0)
                        .doReadSync();

                log.info("成功读取Excel sheet: " + sheetName + "，共" + list.size() + "行");
                return list;
            }

        } catch (IOException e) {
            log.error("调用异常: ", e);
            return new ArrayList<>();
        }
    }

    public boolean downloadExcelFile(String url, Map<String, Object> data, Map<String, String> headerMap, String savePath) {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build();

        try {
            // 构建表单请求体
            FormBody.Builder formBodyBuilder = new FormBody.Builder();
            for (Map.Entry<String, Object> entry : data.entrySet()) {
                formBodyBuilder.add(entry.getKey(), String.valueOf(entry.getValue()));
            }
            RequestBody formBody = formBodyBuilder.build();

            // 构建请求
            Request.Builder requestBuilder = new Request.Builder()
                    .url(url)
                    .post(formBody);

            // 添加头信息
            requestBuilder.addHeader("Accept", "application/json, text/plain, */*");
            requestBuilder.addHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            requestBuilder.addHeader("Referer", "http://mas.cq.hsip.gov.cn/hds/N1703.html");
            requestBuilder.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36");
            requestBuilder.addHeader("X-XSRF-TOKEN", "530137d4-9687-48b7-9b20-3cf10ecb3f18");
            requestBuilder.addHeader("Cookie", "XSRF-TOKEN=530137d4-9687-48b7-9b20-3cf10ecb3f18; SESSION=MzFmMzBkZmYtYzVkMi00ZjRlLWE3ODktNDkxZjQ2NDAyMjE1");

            for (Map.Entry<String, String> entry : headerMap.entrySet()) {
                requestBuilder.addHeader(entry.getKey(), entry.getValue());
            }

            Request request = requestBuilder.build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    log.error("调用失败，状态码: {}", response.code());
                    return false;
                }

                // 保存文件
                byte[] excelBytes = response.body().bytes();
                Files.write(Paths.get(savePath), excelBytes);
                log.info("Excel文件保存成功: {}, 大小: {} 字节", savePath, excelBytes.length);

                // 验证文件是否可以打开
                File file = new File(savePath);
                if (file.exists() && file.length() > 0) {
                    log.info("文件验证成功，可以手动打开查看");
                    return true;
                }
                return false;
            }
        } catch (Exception e) {
            log.error("下载Excel文件异常: ", e);
            return false;
        }
    }
}