package com.reports.dto.response.outpatient.internet.hospital;

import lombok.Data;
import java.util.List;

/**
 * 互医质控运营月报 - 增长趋势图表
 */
@Data
public class GrowthChart {

    private List<String> categories;
    private List<Integer> data;

}
