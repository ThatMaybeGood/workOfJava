package com.example.auto_demo.util;

import com.alibaba.excel.EasyExcel;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.nio.file.Files;

@Slf4j
public class HttpUtil {

    public String post(String url, Map<String, Object> data ,Map<String, String> headerMap){

        OkHttpClient client = new OkHttpClient();

        FormBody.Builder  formBodybuilder = new FormBody.Builder();
        // 遍历 Map，动态添加 Header
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            formBodybuilder.add(entry.getKey(), String.valueOf(entry.getValue()));
        }

        RequestBody formBody = formBodybuilder.build();

        Request.Builder requestBuilder = new Request.Builder();

        // 遍历 Map，动态添加 Header
        for (Map.Entry<String, String> entry : headerMap.entrySet()) {
            requestBuilder.addHeader(entry.getKey(), entry.getValue());
        }

        log.info("调用两定平台入参：" + JSON.toJSONString(formBody));
        log.info("调用两定平台header：" + headerMap.toString());
        log.info("调用两定平台url：" + url);

        Request request = requestBuilder
                .url(url)
                .post(formBody)
                .build();

        // 发送请求
        Response response = null;
        try {
            response = client.newCall(request).execute();

            if (response.isSuccessful() && response.body() != null) {
                return response.body().string();
            }else{
                log.error("调用两定平台异常:" + response.code());
            }
        } catch (Exception e) {
            log.error("调用两定平台异常:" , e);
            return "";
        }
        return "";
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
            requestBuilder.addHeader("Accept", "application/json, text/plain, */*");
            requestBuilder.addHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            requestBuilder.addHeader("Referer", "http://mas.cq.hsip.gov.cn/hds/N1703.html");
            requestBuilder.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36");
            requestBuilder.addHeader("X-XSRF-TOKEN", "530137d4-9687-48b7-9b20-3cf10ecb3f18");
            requestBuilder.addHeader("Cookie", "XSRF-TOKEN=530137d4-9687-48b7-9b20-3cf10ecb3f18; SESSION=MzFmMzBkZmYtYzVkMi00ZjRlLWE3ODktNDkxZjQ2NDAyMjE1");

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


    public List<Map<Integer, String>> postAndParseExcel(String url, Map<String, Object> data, Map<String, String> headerMap) {
        OkHttpClient client = new OkHttpClient();

        // 构建请求体 (保持原样)
        FormBody.Builder formBodybuilder = new FormBody.Builder();
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            formBodybuilder.add(entry.getKey(), String.valueOf(entry.getValue()));
        }
        RequestBody formBody = formBodybuilder.build();

        Request.Builder requestBuilder = new Request.Builder();
        for (Map.Entry<String, String> entry : headerMap.entrySet()) {
            requestBuilder.addHeader(entry.getKey(), entry.getValue());
        }

        Request request = requestBuilder.url(url).post(formBody).build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                // 关键点 1：获取原始字节流
                InputStream inputStream = response.body().byteStream();

                // 关键点 2：不使用实体类，直接同步读取
                // .doReadSync() 会返回一个 List<Map<Integer, String>>
                List<Map<Integer, String>> list = EasyExcel.read(inputStream).sheet().doReadSync();

                return list;
            } else {
                log.error("接口响应失败, 状态码: " + response.code());
            }
        } catch (Exception e) {
            log.error("解析 Excel 过程发生异常: ", e);
        }
        return new ArrayList<>();
    }


    public  String sendPostRequest(String urlString,Map<String, Object> data ,Map<String, String> headerMap )  {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();

            String formParams = "";
            // 1. 设置请求方法和头信息
            connection.setRequestMethod("POST");
            //connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            for (Map.Entry<String, String> entry : headerMap.entrySet()) {
                connection.setRequestProperty(entry.getKey(), entry.getValue());
            }
            //connection.setRequestProperty("User-Agent", "Mozilla/5.0"); // 模拟浏览器 User-Agent
            connection.setDoOutput(true); // 允许写入请求体


            for (Map.Entry<String, Object> entry : data.entrySet()) {
                formParams += entry.getKey() + "=" + URLEncoder.encode(entry.getValue().toString(), "UTF-8") + "&";
            }
            formParams = formParams.substring(0, formParams.length() - 1);


            log.info("调用两定平台入参：" + formParams);
            log.info("调用两定平台header：" + headerMap.toString());
            log.info("调用两定平台url：" + url);

            // 2. 将表单参数写入请求体
            OutputStream os = connection.getOutputStream() ;
            byte[] input = formParams.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);

            // 3. 获取响应状态码
            int responseCode = connection.getResponseCode();
            System.out.println("响应状态码 : " + responseCode);

            // 4. 读取响应内容
            // 如果响应码是 2xx (成功)，则读取输入流，否则读取错误流
            InputStream inputStream = (responseCode >= 200 && responseCode < 300)
                    ? connection.getInputStream()
                    : connection.getErrorStream();

            BufferedReader br = new BufferedReader(
                    new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            return response.toString();

        }catch (Exception e){
            log.error("调两定异常:",e);
            return "";
        } finally {
            // 5. 断开连接
            connection.disconnect();
        }
    }
}
