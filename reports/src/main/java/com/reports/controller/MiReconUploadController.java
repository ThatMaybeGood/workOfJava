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

import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 医保对账文件上传入口(multipart, 不走JSON网关)
 * <p>
 * 校验: 医保类型白名单(service层) + 扩展名/文件魔数(xls=OLE2, xlsx=ZIP) + 大小上限,
 * 防止非Excel文件/超大文件进入POI解析占满内存。
 */
@Slf4j
@RestController
@RequestMapping("/reports/mi-recon")
public class MiReconUploadController {

    /** 与 application.yml spring.servlet.multipart.max-file-size 保持一致 */
    private static final long MAX_FILE_SIZE = 20L * 1024 * 1024;
    private static final Set<String> ALLOW_EXT = new HashSet<>(Arrays.asList("xls", "xlsx"));

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
        validateFile(file);
        Map<String, Object> body = miReconService.upload(file, insurType, dataDate);
        return ApiResponse.success(body, "上传成功，共解析 " + body.get("rowCount") + " 条明细！");
    }

    /** 扩展名 + 大小 + 文件头魔数校验 */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("上传文件为空");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("文件超过20MB限制");
        }
        String name = file.getOriginalFilename() == null ? "" : file.getOriginalFilename();
        int dot = name.lastIndexOf('.');
        String ext = dot >= 0 ? name.substring(dot + 1).toLowerCase() : "";
        if (!ALLOW_EXT.contains(ext)) {
            throw new IllegalArgumentException("仅支持 xls/xlsx 文件");
        }
        byte[] head = new byte[4];
        try (InputStream in = file.getInputStream()) {
            if (in.read(head) < 4) {
                throw new IllegalArgumentException("文件内容不完整");
            }
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("文件读取失败");
        }
        // xls = OLE2复合文档魔数 D0 CF 11 E0; xlsx = ZIP魔数 50 4B 03 04
        boolean ole2 = (head[0] & 0xFF) == 0xD0 && (head[1] & 0xFF) == 0xCF
                && (head[2] & 0xFF) == 0x11 && (head[3] & 0xFF) == 0xE0;
        boolean zip = (head[0] & 0xFF) == 0x50 && (head[1] & 0xFF) == 0x4B
                && (head[2] & 0xFF) == 0x03 && (head[3] & 0xFF) == 0x04;
        if (!ole2 && !zip) {
            throw new IllegalArgumentException("文件内容与xls/xlsx类型不符");
        }
    }

}
