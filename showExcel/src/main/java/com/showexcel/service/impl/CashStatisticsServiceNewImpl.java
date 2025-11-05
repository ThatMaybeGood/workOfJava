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
public class CashStatisticsServiceNewImpl implements CashStatisticsNewService {

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
        // 初始化行索引，用于生成合并单元格的起始位置
        int rowIndex = 1;

        // 预计算行数据，避免重复计算
        List<RowData> accountingRows = createCommonRows(allData,
                CashStatisticsConstant.ACCOUNTING_STATISTICS_TYPE,
                CashStatisticsConstant.ACCOUNTING_STATISTICS_NAME,rowIndex);

        rowIndex += accountingRows.size();
        List<RowData> reservationRows = createCommonRows(allData,
                CashStatisticsConstant.APPOINTMENT_STATISTICS_TYPE,
                CashStatisticsConstant.APPOINTMENT_STATISTICS_NAME,rowIndex);

        rowIndex += reservationRows.size();
        List<RowData> totalRows = createCommonRows(allData,
                CashStatisticsConstant.ALL_STATISTICS_TYPE,
                CashStatisticsConstant.ALL_STATISTICS_NAME,rowIndex);

        rowIndex += totalRows.size();
        List<RowData> otherRows = createCommonRows(allData,
                CashStatisticsConstant.OTHER_STATISTICS_TYPE,
                "最后一行",rowIndex);

//        rowIndex += otherRows.size();
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
    private List<RowData> createCommonRows(List<RowData> allData, Integer tableType, String tableName,int lastIndex) {
        // 过滤出指定类型的数据
        List<RowData> partRows = allData.stream()
                .filter(data -> data.getTableType().equals(tableType))
                .collect(Collectors.toList());

        List<RowData> rows = new ArrayList<>();

        // 根据表格类型处理不同的逻辑
        if (CashStatisticsConstant.APPOINTMENT_STATISTICS_TYPE.equals(tableType)) {
            // 预约中心需要添加分割行
            rows.add(createAppointmentDividerRow(lastIndex));
        }
        if (tableType < CashStatisticsConstant.OTHER_STATISTICS_TYPE) {
            if (!CashStatisticsConstant.ALL_STATISTICS_TYPE.equals(tableType)) {
                // 非总计类型：添加数据行和分区合计
                addDataRowsWithIndex(rows, partRows);
                rows.add(calculateTotal(partRows, tableType, tableName, rows.size()+lastIndex));
            } else {
                // 总计类型：只添加总合计行
                rows.add(calculateTotal(allData, tableType, tableName, lastIndex));
            }
        }
        // 其他分区特殊处理
        if (CashStatisticsConstant.OTHER_STATISTICS_TYPE.equals(tableType)) {
            addOtherCustomRows(rows, lastIndex);
        }

        return rows;
    }

    /**
     * 创建预约中心分割行
     */
    private RowData createAppointmentDividerRow(int index) {
        return new RowData(CashStatisticsConstant.APPOINTMENT_STATISTICS_TYPE,
                "预约中心分割行",
                RowType.SINGLE_SINGLE_CELL_MERGED_DATA,
                List.of(new LayoutCell(index, 0, 1, 14, "预约中心")));
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
    private void addOtherCustomRows(List<RowData> rows, int index) {
        // 添加自定义行
        for (String rowName : CashStatisticsConstant.CUSTOM_ROW_NAMES) {
            rows.add(new RowData(CashStatisticsConstant.OTHER_STATISTICS_TYPE,
                    rowName, RowType.SINGLE_SINGLE_CELL_MERGED_DATA,
                    List.of(new LayoutCell(rows.size()+index, 0, 1, 2, rowName))));
        }

        // 添加最后一行（审核、出纳）
        List<LayoutCell> specialCells = List.of(
                new LayoutCell(rows.size()+index, 0, 1, 2, "审核"),
                new LayoutCell(rows.size()+index, 0, 2, 1, "出纳")
        );
        rows.add(new RowData(CashStatisticsConstant.OTHER_STATISTICS_TYPE,
                "最后一行合并的", RowType.SINGLE_MULTI_CELL_MERGED_DATA, specialCells));
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



}