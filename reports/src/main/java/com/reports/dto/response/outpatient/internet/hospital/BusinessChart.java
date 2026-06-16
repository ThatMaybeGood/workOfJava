package com.reports.dto.response.outpatient.internet.hospital;

import lombok.Data;
import java.util.List;

/**
 * 互医质控运营月报 - 业务分析图表
 */
@Data
public class BusinessChart {

    private List<String> categories;
    private List<Integer> current;
    private List<Integer> last;

}
