package com.showexcel.dto;

/**
 * @author Mine
 * @version 1.0
 * 描述:
 * @date 2025/11/4 17:55
 */

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 现金统计表主响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CashStatisticsResponse {
    private String title;
    private TableMetadata metadata;
    private List<String> headers;
    private List<TableSection> sections;
    private TableLayout layout;
}