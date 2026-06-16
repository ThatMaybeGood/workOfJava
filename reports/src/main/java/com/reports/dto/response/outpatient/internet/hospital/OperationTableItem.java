package com.reports.dto.response.outpatient.internet.hospital;

import lombok.Data;

/**
 * 互医质控运营月报 - 运行情况表行数据
 */
@Data
public class OperationTableItem {

    private String name;
    private Integer current;
    private Integer last;
    private String growth;

}
