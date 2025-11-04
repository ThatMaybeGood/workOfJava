package com.showexcel;

import com.showexcel.dto.*;
import com.showexcel.model.HolidayCalendar;
import com.showexcel.service.impl.HolidayServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@SpringBootTest
class BatchUpdateTest {

    @Autowired
    private HolidayServiceImpl holidayService;

    @Test
    void testBatchUpdateWithExistingDate() throws Exception {
        // 使用Builder模式创建对象（推荐）
        CashStatisticsResponse response = CashStatisticsResponse.builder()
                .title("现金统计表（2025-11-02）")
                .metadata(TableMetadata.builder()
                        .totalRows(19)
                        .totalCols(14)
                        .generatedAt(LocalDateTime.now())
                        .reportDate(LocalDate.of(2025, 11, 2))
                        .build())
                .headers(Arrays.asList("序号", "名称", "预交金收入", "医疗收入", "挂号收入", ...))
    .sections(Arrays.asList(
                TableSection.builder()
                        .name("会计室")
                        .type(SectionType.ACCOUNTING)
                        .rows(Arrays.asList(
                                TableRow.builder()
                                        .id(21L)
                                        .index(0)
                                        .type(RowType.DATA)
                                        .name("cs")
                                        .data(RowData.builder()
                                                .hisAdvancePayment(new BigDecimal("20.0"))
                                                .hisMedicalIncome(BigDecimal.ZERO)
                                                .hisRegistrationIncome(new BigDecimal("22.0"))
                                                .build())
                                        .build()
                        ))
                        .build()
        ))
                .layout(TableLayout.builder()
                        .sectionHeaders(Arrays.asList(
                                LayoutCell.builder()
                                        .row(2)
                                        .col(0)
                                        .colSpan(14)
                                        .content("预约中心")
                                        .style(CellStyle.SECTION_HEADER)
                                        .build()
                        ))
                        .build())
                .build();

// 或者使用传统构造方式
        CashStatisticsResponse response2 = new CashStatisticsResponse();
        response2.setTitle("现金统计表");
// ... 设置其他属性
    }
}