package com.showexcel.service.impl;


import com.showexcel.constant.CashStatisticsConstant;
import com.showexcel.repository.CashStatisticsRepository;
import com.showexcel.response.*;
import com.showexcel.service.CashStatisticsNewService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CashStatisticsServiceNewImpl1 implements CashStatisticsNewService {

    @Autowired
    private CashStatisticsRepository cashStatisticsRepository;

    //获取当前日期时间，例如2025-10-29并将其格式化为字符串形式
    LocalDateTime now = LocalDateTime.now().minusDays(1);
    String currentDate = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));


    @Override
    public CashStatisticsResponse getAllStatisticsTableByDate(String date) {
        List<RowData> allData = cashStatisticsRepository.findByTableDateNew(date);

        // 构建表格响应对象
        CashStatisticsResponse response = buildBasicResponse(date, allData.size());

        // 构建分区数据
        List<TableSection> sections = buildTableSections(allData);
        response.setSections(sections);

        // 构建合并配置（根据需求决定是否启用）
        // response.setLayouts(buildTableLayouts());

        return response;
    }

    /**
     * 构建基础响应信息
     */
    private CashStatisticsResponse buildBasicResponse(String date, int dataSize) {
        CashStatisticsResponse response = new CashStatisticsResponse();
        response.setTitle("现金统计表（" + date + "）");
        response.setMetadata(new TableMetadata(
                dataSize + CashStatisticsConstant.CUSTOM_ROW_NAMES.length + 5,
                CashStatisticsConstant.TABLE_HEADERS.length,
                LocalDateTime.now(),
                LocalDate.parse(date)
        ));
        response.setHeaders(List.of(CashStatisticsConstant.TABLE_HEADERS));
        return response;
    }

    /**
     * 构建所有表格分区
     */
    private List<TableSection> buildTableSections(List<RowData> allData) {
        List<TableSection> sections = new ArrayList<>();

        // 预计算行数据，避免重复计算
        List<RowData> accountingRows = createCommonRows(allData,
                CashStatisticsConstant.ACCOUNTING_STATISTICS_TYPE,
                CashStatisticsConstant.ACCOUNTING_STATISTICS_NAME);

        List<RowData> reservationRows = createCommonRows(allData,
                CashStatisticsConstant.APPOINTMENT_STATISTICS_TYPE,
                CashStatisticsConstant.APPOINTMENT_STATISTICS_NAME);

        List<RowData> totalRows = createCommonRows(allData,
                CashStatisticsConstant.ALL_STATISTICS_TYPE,
                CashStatisticsConstant.ALL_STATISTICS_NAME);

        List<RowData> otherRows = createCommonRows(allData,
                CashStatisticsConstant.OTHER_STATISTICS_TYPE,
                "最后一行");

        // 构建分区
        sections.add(buildSection("会计室", SectionType.ACCOUNTING, accountingRows));
        sections.add(buildSection("预约中心", SectionType.RESERVATION, reservationRows));
        sections.add(buildSection("总计", SectionType.GRAND_TOTAL, totalRows));
        sections.add(buildSection("其他", SectionType.OTHER, otherRows));

        return sections;
    }

    /**
     * 构建单个分区
     */
    private TableSection buildSection(String name, SectionType type, List<RowData> rows) {
        return TableSection.builder()
                .name(name)
                .type(type)
                .rows(rows)
                .rowCount(rows.size()) // 直接使用已计算的行数
                .build();
    }

    /**
     * 优化后的通用行创建方法
     */
    private List<RowData> createCommonRows(List<RowData> allData, Integer tableType, String tableName) {
        // 过滤出指定类型的数据
        List<RowData> partRows = allData.stream()
                .filter(data -> data.getTableType().equals(tableType))
                .collect(Collectors.toList());

        List<RowData> rows = new ArrayList<>();

        // 根据表格类型处理不同的逻辑
        if (CashStatisticsConstant.APPOINTMENT_STATISTICS_TYPE.equals(tableType)) {
            // 预约中心需要添加分割行
            rows.add(createAppointmentDividerRow());
        }

        if (!CashStatisticsConstant.ALL_STATISTICS_TYPE.equals(tableType)) {
            // 非总计类型：添加数据行和分区合计
            addDataRowsWithIndex(rows, partRows);
            rows.add(calculateTotal(partRows, tableType, tableName, rows.size()));
        } else {
            // 总计类型：只添加总合计行
            rows.add(calculateTotal(allData, tableType, tableName, 0));
        }

        // 其他分区特殊处理
        if (CashStatisticsConstant.OTHER_STATISTICS_TYPE.equals(tableType)) {
            addOtherCustomRows(rows);
        }

        return rows;
    }

    /**
     * 创建预约中心分割行
     */
    private RowData createAppointmentDividerRow() {
        return new RowData(CashStatisticsConstant.APPOINTMENT_STATISTICS_TYPE,
                "预约中心分割行",
                RowType.SINGLE_SINGLE_CELL_MERGED_DATA,
                List.of(new LayoutCell(2, 0, 1, 14, "预约中心")));
    }

    /**
     * 添加带索引的数据行
     */
    private void addDataRowsWithIndex(List<RowData> rows, List<RowData> partRows) {
        int rowIndex = 1;
        for (RowData item : partRows) {
            item.setStyle(RowType.NO_MERGED_DATA);
            item.setIndex(rowIndex++);
            rows.add(item);
        }
    }

    /**
     * 添加其他分区的自定义行
     */
    private void addOtherCustomRows(List<RowData> rows) {
        // 添加自定义行
        for (String rowName : CashStatisticsConstant.CUSTOM_ROW_NAMES) {
            rows.add(new RowData(CashStatisticsConstant.OTHER_STATISTICS_TYPE,
                    rowName, RowType.SINGLE_SINGLE_CELL_MERGED_DATA,
                    List.of(new LayoutCell(rows.size(), 0, 1, 2, rowName))));
        }

        // 添加最后一行（审核、出纳）
        List<LayoutCell> specialCells = List.of(
                new LayoutCell(rows.size(), 0, 1, 2, "审核"),
                new LayoutCell(rows.size(), 0, 2, 1, "出纳")
        );
        rows.add(new RowData(CashStatisticsConstant.OTHER_STATISTICS_TYPE,
                "最后一行", RowType.SINGLE_MULTI_CELL_MERGED_DATA, specialCells));
    }

    // 原有的 calculateTotal 方法保持不变，但建议也进行优化
//    private RowData calculateTotal(List<RowData> data, Integer tableType, String name, int rowIndex) {
//        RowData total = new RowData();
//        total.setTableType(tableType);
//        total.setName(name);
//
//        // 使用 Stream reduce 或 forEach 优化累加逻辑
//        data.forEach(item -> accumulateRowData(total, item));
//
//        total.setStyle(RowType.SINGLE_SINGLE_CELL_MERGED_DATA);
//        total.setSpecialCells(List.of(new LayoutCell(rowIndex, 0, 1, 2, name)));
//
//        return total;
//    }

    /**
     * 累加行数据辅助方法
     */
    private void accumulateRowData(RowData total, RowData item) {
        total.setHisAdvancePayment(addBigDecimal(total.getHisAdvancePayment(), item.getHisAdvancePayment()));
        total.setHisMedicalIncome(addBigDecimal(total.getHisMedicalIncome(), item.getHisMedicalIncome()));
        total.setHisRegistrationIncome(addBigDecimal(total.getHisRegistrationIncome(), item.getHisRegistrationIncome()));
        // ... 其他字段的累加
    }

    /**
     * BigDecimal 安全加法
     */
    private BigDecimal addBigDecimal(BigDecimal a, BigDecimal b) {
        return (a != null ? a : BigDecimal.ZERO).add(b != null ? b : BigDecimal.ZERO);
    }



    //calculateTotal 计算传入的合计行数据
    private RowData calculateTotal(List<RowData> data, Integer tableType, String name, Integer index) {
        //计算传入的data的合计行数据
        RowData total = new RowData();

        total.setTableType(tableType);
        total.setName(name);
        // 遍历data中的每个RowData对象，累加其字段值
        for (RowData item : data) {
            total.setHisAdvancePayment(total.getHisAdvancePayment().add(item.getHisAdvancePayment()));
            total.setHisMedicalIncome(total.getHisMedicalIncome().add(item.getHisMedicalIncome()));
            total.setHisRegistrationIncome(total.getHisRegistrationIncome().add(item.getHisRegistrationIncome()));
            total.setReportAmount(total.getReportAmount().add(item.getReportAmount()));
            total.setPreviousTemporaryReceipt(total.getPreviousTemporaryReceipt().add(item.getPreviousTemporaryReceipt()));
            total.setActualReportAmount(total.getActualReportAmount().add(item.getActualReportAmount()));
            total.setCurrentTemporaryReceipt(total.getCurrentTemporaryReceipt().add(item.getCurrentTemporaryReceipt()));
            total.setActualCashAmount(total.getActualCashAmount().add(item.getActualCashAmount()));
            total.setRetainedDifference(total.getRetainedDifference().add(item.getRetainedDifference()));
            total.setRetainedCash(total.getRetainedCash().add(item.getRetainedCash()));
            total.setPettyCash(total.getPettyCash().add(item.getPettyCash()));
        }
        // 设置合计行的样式和特殊单元格配置
        total.setStyle(RowType.SINGLE_SINGLE_CELL_MERGED_DATA);
        total.setSpecialCells(List.of(new LayoutCell(index, 0, 1, 2, name)));

        return total;
    }


    // 创建通用处理
//    private List<RowData> createCommonRows(List<RowData> allData, Integer tableType, String tableName, Integer rowMergeIndex) {
//        int rowIndex = 0;
//        //从所有数据中剥离出指定类型的数据
//        List<RowData> partRows = allData.stream().filter(data -> data.getTableType().equals(tableType)).collect(Collectors.toList());
//
//        List<RowData> rows = new ArrayList<>();
//
//        if (tableType == 1) {
//            // 预约中心分割行
//            rows.add(new RowData(tableType, "预约中心分割行", RowType.SINGLE_SINGLE_CELL_MERGED_DATA, List.of(new LayoutCell(2, 0, 1, 14, "预约中心"))));
//        }
//
//        if (tableType == 2) {
//            // 通用合计行
//            rows.add(calculateTotal(allData, tableType, tableName, rowMergeIndex+partRows.size()));
//        }else{
//            // 1 和 0 通用数据行
//            for (RowData item : partRows) {
//                rowIndex++;
//                item.setStyle(RowType.NO_MERGED_DATA);
//                item.setIndex(rowIndex);
//                rows.add(item);
//            }
//            rows.add(calculateTotal(partRows, tableType, tableName, rowMergeIndex+partRows.size()));
//        }
//
//        if(tableType == 3) {
//                // 其他自定义行
//            for (String rowName : CashStatisticsConstant.CUSTOM_ROW_NAMES) {
//                rows.add(new RowData(tableType, rowName,RowType.SINGLE_SINGLE_CELL_MERGED_DATA, List.of(new LayoutCell(6, 0, 1, 2, rowName))));
//            }
//            List<LayoutCell> specialCells = new ArrayList<>();
//            specialCells.add(new LayoutCell(6, 0, 1, 2, "审核"));
//            specialCells.add(new LayoutCell(6, 0, 2, 1, "出纳"));
//            rows.add(new RowData(tableType, "最后一行",RowType.SINGLE_MULTI_CELL_MERGED_DATA, specialCells));
//        }
//        return rows;
//    }


}