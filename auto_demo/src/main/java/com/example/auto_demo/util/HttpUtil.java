package com.example.auto_demo.util;

import com.alibaba.excel.EasyExcel;
import com.alibaba.fastjson.JSON;
import com.example.auto_demo.model.ExportExcelDTO;
import com.google.common.io.Files;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.io.*;
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
     * @return
     */
    public byte[] postExport(String url, Map<String, Object> data, Map<String, String> headerMap) {
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

            log.info("调用两定平台入参：{}", JSON.toJSONString(data));
            log.info("调用两定平台header：{}", headerMap);
            log.info("调用两定平台url：{}", url);

            Request request = requestBuilder.build();

            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                return response.body().bytes();
            } else {
                log.error("调用两定平台异常，状态码: {}，消息: {}", response.code(), response.message());
            }

        } catch (Exception e) {
            log.error("调用两定平台异常: ", e);
            return null;
        }
        return null;
    }


//    public List<ExportExcelDTO> readExcelFile(byte[] excelBytes) {
//        List<ExportExcelDTO> excelDTOS = new ArrayList<>();
//
//        try {
//
//            // 获取响应体字节
//            log.info("获取到Excel文件，大小: {} 字节", excelBytes.length);
//            // 将文件临时保存（用于调试）
////            String tempFileName = "temp_excel_" + System.currentTimeMillis() + ".xlsx";
////            Files.write(Paths.get(tempFileName), excelBytes);
////            log.info("Excel文件已临时保存为: {}", tempFileName);
//
//            // 使用 EasyExcel 读取 Excel 内容
//            InputStream inputStream = new ByteArrayInputStream(excelBytes);
//            List<Map<Integer, String>> list = EasyExcel.read(inputStream)
//                    .sheet()  // 读取第一个sheet
//                    .headRowNumber(0)  // 从第0行开始读取数据
//                    .doReadSync();
//
//            log.info("成功读取Excel数据，共 {} 行", list.size());
//
//            // 打印前几行数据（用于调试）
//            if (!list.isEmpty()) {
//                for (int i = 0; i < Math.min(list.size(), 3); i++) {
//                    log.info("第 {} 行数据: {}", i, list.get(i));
//                }
//
//                for (int i = 1; i < list.size(); i++) {
//                    ExportExcelDTO exportExcelDTO = new ExportExcelDTO();
//                    exportExcelDTO.setPsnNo(list.get(i).get(0));
//                    exportExcelDTO.setPsnName(list.get(i).get(1));
//                    exportExcelDTO.setCertno(list.get(i).get(2));
//                    exportExcelDTO.setMdtrtId(list.get(i).get(3));
//                    exportExcelDTO.setSetlId(list.get(i).get(4));
//                    exportExcelDTO.setMedinsSetlId(list.get(i).get(5));
//                    exportExcelDTO.setMsgid(list.get(i).get(5));
//                    exportExcelDTO.setAdmDate(list.get(i).get(6));
//                    exportExcelDTO.setDisDate(list.get(i).get(7));
//                    exportExcelDTO.setBillDate(list.get(i).get(8));
//                    exportExcelDTO.setMedType(list.get(i).get(9));
//                    exportExcelDTO.setMedfeeSumamt(list.get(i).get(10));
//                    exportExcelDTO.setTranType(list.get(i).get(10).contains("-")?"2":"1");
//                    excelDTOS.add(exportExcelDTO);
//                }
//            }
//        } catch (Exception e) {
//            log.error("读取Excel文件异常: ", e);
//        }
//        return excelDTOS;
//    }

    public List<ExportExcelDTO> readExcelFile(byte[] excelBytes,String insutype) {
        if (excelBytes == null || excelBytes.length == 0) {
            log.warn("Excel文件为空");
            return Collections.emptyList();
        }

        log.info("Excel文件大小: {} 字节", excelBytes.length);

        try (InputStream inputStream = new ByteArrayInputStream(excelBytes)) {
            List<Map<Integer, String>> data = EasyExcel.read(inputStream)
                    .sheet()
                    .headRowNumber(0)
                    .doReadSync();

            log.info("读取到 {} 行数据", data.size());

            if (!data.isEmpty()) {
                log.debug("表头: {}", data.get(0));
                data.stream().limit(3).forEach(row -> log.debug("数据行: {}", row));
            }

            // 跳过表头，转换为DTO
            return data.stream()
                    .skip(1)
                    .map(row -> convertRowToDTO(row,insutype))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("读取Excel异常", e);
            return Collections.emptyList();
        }
    }

    private ExportExcelDTO convertRowToDTO(Map<Integer, String> row,String insutype) {
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
