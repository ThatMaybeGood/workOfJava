package com.reports.dto.response.cash.mi;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 医保对账上传记录行
 */
@Data
public class MiReconFileItem {

    private Long id;

    /** 原始文件名 */
    private String fileName;

    /** 医保类型: 310职工 390居民 3101异地职工 3901异地居民 */
    private String insurType;

    /** 医保类型名称 */
    private String insurTypeName;

    /** 数据日期 */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date dataDate;

    /** 明细行数 */
    private Integer rowCount;

    /** 上传时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
}
