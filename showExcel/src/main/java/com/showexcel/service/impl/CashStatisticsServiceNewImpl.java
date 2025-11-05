package com.showexcel.service.impl;


import com.showexcel.constant.CashStatisticsConstant;
import com.showexcel.dto.CashStatisticsTableDTO;
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
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CashStatisticsServiceNewImpl implements CashStatisticsNewService {

    @Autowired
    private CashStatisticsRepository cashStatisticsRepository;

    //获取当前日期时间，例如2025-10-29并将其格式化为字符串形式
    LocalDateTime now = LocalDateTime.now().minusDays(1);
    String currentDate = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));


    @Override
    public CashStatisticsResponse getAllStatisticsTableByDate(String date) {

        int rowMergeIndex = 0;

        List<RowData> allData = cashStatisticsRepository.findByTableDateNew(date);

        // 构建表格响应对象
        CashStatisticsResponse response = new CashStatisticsResponse();

        // 设置标题
        response.setTitle("现金统计表（" + date + "）");

        // 设置元数据
        response.setMetadata(new TableMetadata(allData.size() + CashStatisticsConstant.CUSTOM_ROW_NAMES.length + 5, CashStatisticsConstant.TABLE_HEADERS.length, LocalDateTime.now(), LocalDate.parse(date)));

        // 设置表头
        response.setHeaders(List.of(CashStatisticsConstant.TABLE_HEADERS));

        // 构建分区数据
        List<TableSection> sections = new ArrayList<>();

        // 会计室分区
        TableSection accountingSection = TableSection.builder()
                .name("会计室")
                .type(SectionType.ACCOUNTING)
                .rows(createCommonRows(allData, CashStatisticsConstant.ACCOUNTING_STATISTICS_TYPE, CashStatisticsConstant.ACCOUNTING_STATISTICS_NAME,rowMergeIndex))
                .rowCount(createCommonRows(allData, CashStatisticsConstant.ACCOUNTING_STATISTICS_TYPE, CashStatisticsConstant.ACCOUNTING_STATISTICS_NAME,rowMergeIndex).size())
                .build();
        sections.add(accountingSection);

        // 预约中心分区
        TableSection reservationSection = TableSection.builder()
                .name("预约中心")
                .type(SectionType.RESERVATION)
                .rows(createCommonRows(allData, CashStatisticsConstant.APPOINTMENT_STATISTICS_TYPE, CashStatisticsConstant.APPOINTMENT_STATISTICS_NAME,rowMergeIndex))
                .rowCount(createCommonRows(allData, CashStatisticsConstant.APPOINTMENT_STATISTICS_TYPE, CashStatisticsConstant.APPOINTMENT_STATISTICS_NAME,rowMergeIndex).size())
                .build();
        sections.add(reservationSection);

        // 总计分区
        TableSection totalSection = TableSection.builder()
                .name("总计")
                .type(SectionType.GRAND_TOTAL)
                .rows(createCommonRows(allData, CashStatisticsConstant.ALL_STATISTICS_TYPE, CashStatisticsConstant.ALL_STATISTICS_NAME,rowMergeIndex))
                .rowCount(createCommonRows(allData, CashStatisticsConstant.ALL_STATISTICS_TYPE, CashStatisticsConstant.ALL_STATISTICS_NAME,rowMergeIndex).size())
                .build();
        sections.add(totalSection);

        // 其他分区行先生成，后再合并
        TableSection otherSection = TableSection.builder()
                .name("其他")
                .type(SectionType.OTHER)
                .rows(createCommonRows(allData, CashStatisticsConstant.OTHER_STATISTICS_TYPE, "最后一行",rowMergeIndex))
                .rowCount(createCommonRows(allData, CashStatisticsConstant.OTHER_STATISTICS_TYPE, "最后一行",rowMergeIndex).size())
                .build();
        sections.add(otherSection);

        response.setSections(sections);


        // 构建合并配置
        List<TableLayout> layouts = new ArrayList<>();

        //1、合并会计室和预约中心的合计行
        TableLayout layoutSingleCell = TableLayout.builder()
                .style(RowType.SINGLE_SINGLE_CELL_MERGED_DATA)
                .specialCells(createSingleCells())
                .build();
        layouts.add(layoutSingleCell);

        TableLayout layoutMultiCell = TableLayout.builder()
                .style(RowType.SINGLE_MULTI_CELL_MERGED_DATA)
                .specialCells(createMultiCells(20))
                .build();
        layouts.add(layoutMultiCell);

//        response.setLayouts(layouts);

        return response;
    }


    private List<LayoutCell> createSingleCells() {
        List<LayoutCell> specialCells = new ArrayList<>();

        specialCells.add(new LayoutCell(2, 0, 1, 14, "预约中心"));
        specialCells.add(new LayoutCell(1, 0, 1, 2, "会计室合计"));
        specialCells.add(new LayoutCell(5, 0, 1, 2, "预约合计"));
        specialCells.add(new LayoutCell(6, 0, 1, 2, "总合计"));

        for (String name : CashStatisticsConstant.CUSTOM_ROW_NAMES) {
            specialCells.add(new LayoutCell(6, 0, 2, 1, name));
        }

        return specialCells;
    }

    private List<LayoutCell> createMultiCells(int lastRowIndex) {
        List<LayoutCell> specialCells = new ArrayList<>();

        specialCells.add(new LayoutCell(6, 0, 1, 2, "审核"));
        specialCells.add(new LayoutCell(6, 0, 1, 2, "出纳"));
        return specialCells;
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

//
//    private List<RowData> createOtherRows() {
//        List<RowData> rows = new ArrayList<>();
//        for (String rowName : CashStatisticsConstant.CUSTOM_ROW_NAMES) {
//            rows.add(new RowData(CashStatisticsConstant.OTHER_STATISTICS_TYPE, rowName,
//                    RowType.SINGLE_SINGLE_CELL_MERGED_DATA, List.of(new LayoutCell(6, 0, 1, 2, rowName))));
//        }
//        List<LayoutCell> specialCells = new ArrayList<>();
//        specialCells.add(new LayoutCell(6, 0, 1, 2, "审核"));
//        specialCells.add(new LayoutCell(6, 0, 2, 1, "出纳"));
//        rows.add(new RowData(CashStatisticsConstant.OTHER_STATISTICS_TYPE, "最后一行",RowType.SINGLE_MULTI_CELL_MERGED_DATA, specialCells));
//
//        return rows;
//    }
//
//    // 创建会计室数据行
//    private List<RowData> createAccountingRows(List<RowData> allData) {
//        int rowIndex = 0;
//        //从所有数据中剥离出会计室相关的数据
//        List<RowData> accountingRows = allData.stream()
//                .filter(data -> data.getTableType().equals(CashStatisticsConstant.ACCOUNTING_STATISTICS_TYPE))
//                .collect(Collectors.toList());
//
//        // 会计室数据行
//        List<RowData> rows = new ArrayList<>();
//        for (RowData item : accountingRows) {
//            rowIndex++;
//            item.setStyle(RowType.NO_MERGED_DATA);
//            item.setIndex(rowIndex);
//            rows.add(item);
//        }
//
//        // 会计室合计行
//        rows.add(calculateTotal(accountingRows, CashStatisticsConstant.ACCOUNTING_STATISTICS_TYPE, CashStatisticsConstant.ACCOUNTING_STATISTICS_NAME));
//
//        return rows;
//    }
//
//    // 创建预约中心数据行
//    private List<RowData> createApptionRows(List<RowData> allData) {
//        int rowIndex = 0;
//        //从所有数据中剥离出预约中心相关的数据
//        List<RowData> appointmentRows = allData.stream()
//                .filter(data -> data.getTableType().equals(CashStatisticsConstant.APPOINTMENT_STATISTICS_TYPE))
//                .collect(Collectors.toList());
//        // 会计室数据行
//        List<RowData> rows = new ArrayList<>();
//        // 预约中心分割行
//        rows.add(new RowData(CashStatisticsConstant.APPOINTMENT_STATISTICS_TYPE, "预约中心分割行",
//                RowType.SINGLE_SINGLE_CELL_MERGED_DATA, List.of(new LayoutCell(2, 0, 1, 14, "预约中心分割行"))));
//
//        for (RowData item : appointmentRows) {
//            rowIndex++;
//            item.setIndex(rowIndex);
//            item.setStyle(RowType.NO_MERGED_DATA);
//            rows.add(item);
//        }
//
//        // 预约合计行
//        rows.add(calculateTotal(appointmentRows, CashStatisticsConstant.APPOINTMENT_STATISTICS_TYPE, CashStatisticsConstant.APPOINTMENT_STATISTICS_NAME));
//
//        return rows;
//    }
//
//    // 创建总计数据行
//    private List<RowData> createTotalRows(List<RowData> allData) {
//
//        // 总计数据行
//        List<RowData> rows = new ArrayList<>();
//        // 总合计行
//        rows.add(calculateTotal(allData, CashStatisticsConstant.ALL_STATISTICS_TYPE, CashStatisticsConstant.ALL_STATISTICS_NAME));
//
//        return rows;
//    }


    // 创建通用处理
    private List<RowData> createCommonRows(List<RowData> allData, Integer tableType, String tableName, Integer rowMergeIndex) {
        int rowIndex = 0;
        //从所有数据中剥离出指定类型的数据
        List<RowData> partRows = allData.stream().filter(data -> data.getTableType().equals(tableType)).collect(Collectors.toList());

        List<RowData> rows = new ArrayList<>();

        if (tableType == 1) {
            // 预约中心分割行
            rows.add(new RowData(tableType, "预约中心分割行", RowType.SINGLE_SINGLE_CELL_MERGED_DATA, List.of(new LayoutCell(2, 0, 1, 14, "预约中心"))));
        }

        if (tableType == 2) {
            // 通用合计行
            rows.add(calculateTotal(allData, tableType, tableName, rowMergeIndex));
        }else{
            // 1 和 0 通用数据行
            for (RowData item : partRows) {
                rowIndex++;
                item.setStyle(RowType.NO_MERGED_DATA);
                item.setIndex(rowIndex);
                rows.add(item);
            }
            rows.add(calculateTotal(partRows, tableType, tableName, rowMergeIndex));
        }

        if(tableType == 3) {
                // 其他自定义行
            for (String rowName : CashStatisticsConstant.CUSTOM_ROW_NAMES) {
                rows.add(new RowData(tableType, rowName,RowType.SINGLE_SINGLE_CELL_MERGED_DATA, List.of(new LayoutCell(6, 0, 1, 2, rowName))));
            }
            List<LayoutCell> specialCells = new ArrayList<>();
            specialCells.add(new LayoutCell(6, 0, 1, 2, "审核"));
            specialCells.add(new LayoutCell(6, 0, 2, 1, "出纳"));
            rows.add(new RowData(tableType, "最后一行",RowType.SINGLE_MULTI_CELL_MERGED_DATA, specialCells));
        }
        return rows;
    }


}