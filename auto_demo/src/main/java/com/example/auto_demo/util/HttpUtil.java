package com.example.auto_demo.util;

import com.alibaba.excel.EasyExcel;
import com.alibaba.fastjson.JSON;
import com.example.auto_demo.model.ExportExcelDTO;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class HttpUtil {

    public String post(String url, Map<String, Object> data, Map<String, String> headerMap) {

        OkHttpClient client = new OkHttpClient();

        FormBody.Builder formBodybuilder = new FormBody.Builder();
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
            } else {
                log.error("调用两定平台异常:" + response.code());
            }
        } catch (Exception e) {
            log.error("调用两定平台异常:", e);
            return "";
        }
        return "";
    }


    /*
     * 请求两定平台导出接口，
     * @param url 导出接口URL
     * @param data 请求参数
     * @param headerMap 自定义请求头
     * @param traceId 追踪号
     * @return
     */
    public byte[] postExport(String url, Map<String, Object> data, Map<String, String> headerMap, String traceId) {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(120, java.util.concurrent.TimeUnit.SECONDS)  //下载时间
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

            // 添加自定义头
            for (Map.Entry<String, String> entry : headerMap.entrySet()) {
                requestBuilder.addHeader(entry.getKey(), entry.getValue());
            }

            log.info("{} 调用两定平台导出接口入参：{}", traceId, JSON.toJSONString(data));
            log.info("{} 调用两定平台导出接口header：{}", traceId, headerMap);
            log.info("{} 调用两定平台导出接口url：{}", traceId, url);

            Request request = requestBuilder.build();

            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                return response.body().bytes();
            } else {
                log.error("{} 调用两定平台异常，状态码: {}，消息: {}", traceId, response.code(), response.message());
            }

        } catch (Exception e) {
            log.error("{} 调用两定平台异常: ", traceId, e);
            return null;
        }
        return null;
    }

    /**
     * 保存Excel文件
     *
     * @param excelBytes Excel文件字节数组
     * @param baseFileName 基础文件名（不包含扩展名）
     * @param extension 文件扩展名（包含点号，如 ".xlsx"）
     */
    public static void saveExcelFile(byte[] excelBytes, String baseFileName,String extension,String traceId) {
        try {
            // 3. 处理文件名重复
            String fileName = baseFileName + extension;
            Path filePath = Paths.get(fileName);

            int sequence = 1;
            while (Files.exists(filePath)) {
                fileName = baseFileName + "(" + sequence + ")" + extension;
                filePath = Paths.get(fileName);
                sequence++;

                // 安全限制，防止无限循环
                if (sequence > 20) {
                    log.error("{} 文件名重复超过20次，不再保存文件", traceId);
                    break;
                }
            }
            // 4. 保存文件
            Files.write(filePath, excelBytes);

            log.info("{} Excel文件已保存为: {}", traceId, filePath.toAbsolutePath().toString());


        } catch (Exception e) {
            log.error("{} 保存Excel文件失败: {}", traceId, e.getMessage(), e);
        }
    }

    public  List<ExportExcelDTO> readExcelFile(byte[] excelBytes, String insutype,String traceId) {

        try (InputStream inputStream = new ByteArrayInputStream(excelBytes)) {
            List<Map<Integer, String>> data = EasyExcel.read(inputStream)
                    .sheet()
                    .headRowNumber(0)
                    .doReadSync();

            log.info("{} 读取到 {} 行数据", traceId, data.size());

            if (!data.isEmpty()) {
                log.debug("{} 表头: {}", traceId, data.get(0));
                data.stream().limit(3).forEach(row -> log.debug("数据行: {}", row));
            }

            // 跳过表头，转换为DTO
            return data.stream()
                    .skip(1)
                    .map(row -> convertRowToDTO(row, insutype))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("{} 读取Excel异常: {}", traceId, e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    private  ExportExcelDTO convertRowToDTO(Map<Integer, String> row, String insutype) {
        if (row == null) return null;

        ExportExcelDTO dto = new ExportExcelDTO();
        dto.setPsnNo(getValue(row, 0));
        dto.setPsnName(getValue(row, 1));
        dto.setCertno(getValue(row, 2));
        dto.setMdtrtId(getValue(row, 3));
        dto.setSetlId(getValue(row, 4));
        dto.setMedinsSetlId(getValue(row, 5)); // 同列
        dto.setMsgid(getValue(row, 5));
        dto.setAdmDate(getValue(row, 6));
        dto.setDisDate(getValue(row, 7));
        dto.setBillDate(getValue(row, 8));
        dto.setMedType(getValue(row, 9));

        String amount = getValue(row, 10);
        dto.setMedfeeSumamt(amount);
        dto.setTranType(amount.contains("-") ? "2" : "1");
        dto.setInsutype(insutype);

        return dto;
    }

    private String getValue(Map<Integer, String> row, int col) {
        String value = row.get(col);
        return value == null ? "" : value.trim();
    }


}
