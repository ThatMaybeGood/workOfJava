package com.reports.controller;

import com.reports.dto.common.ApiResponse;
import com.reports.service.MiReconService;
import com.reports.util.SeqUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * 医保对账文件上传入口(multipart, 不走JSON网关)
 */
@Slf4j
@RestController
@RequestMapping("/reports/mi-recon")
public class MiReconUploadController {

    private final MiReconService miReconService;

    @Autowired
    public MiReconUploadController(MiReconService miReconService) {
        this.miReconService = miReconService;
    }

    /**
     * 上传医保对账文件: file + insurType(310/390/3101/3901) + dataDate(yyyy-MM-dd, 默认当天)
     */
    @PostMapping("/upload")
    public ApiResponse<Map<String, Object>> upload(@RequestParam("file") MultipartFile file,
                                                   @RequestParam("insurType") String insurType,
                                                   @RequestParam(value = "dataDate", required = false) String dataDate) {
        SeqUtil.next();
        if (dataDate == null || dataDate.trim().isEmpty()) {
            dataDate = new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date());
        }
        log.info("[医保对账每日对比] 上传文件: name={} type={} dataDate={}",
                file == null ? null : file.getOriginalFilename(), insurType, dataDate);
        Map<String, Object> body = miReconService.upload(file, insurType, dataDate);
        return ApiResponse.success(body, "上传成功，共解析 " + body.get("rowCount") + " 条明细！");
    }
}
