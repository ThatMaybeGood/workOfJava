package com.reports.dto.response.outpatient.internet.hospital;

import lombok.Data;

/**
 * 互医质控运营月报 - 概览数据
 */
@Data
public class OverviewData {

    private Integer outpatientVolume;
    private String doctorRatio;
    private String receptionRate;
    private String prescriptionRate;
    private String recordRate;
    private String reviewRate;
    private String executionRate;

}
