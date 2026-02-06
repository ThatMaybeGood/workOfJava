package com.example.auto_demo.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

@Slf4j
public class HttpUtilBak {

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

    public String postExport(String url, Map<String, Object> data ,Map<String, String> headerMap){

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
                InputStream inputStream =response.body().byteStream();

                return null;
            }else{
                log.error("调用两定平台异常:" + response.code());
            }
        } catch (Exception e) {
            log.error("调用两定平台异常:" , e);
            return "";
        }
        return "";
    }



}
