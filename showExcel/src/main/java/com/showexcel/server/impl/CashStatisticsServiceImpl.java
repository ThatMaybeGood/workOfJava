package com.showexcel.server.impl;

import com.showexcel.dao.CashStatisticsRow;
import com.showexcel.dao.CashStatisticsTableDTO;
import com.showexcel.model.CashStatistics;
import com.showexcel.dao.CashStatisticsDTO;
import com.showexcel.model.CellMergeConfig;
import com.showexcel.repository.CashStatisticsRepository;
import com.showexcel.server.CashStatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Mine
 * @version 1.0
 * 描述:
 * @date 2025/10/29 15:54
 */
@Slf4j
@Service
public class CashStatisticsServiceImpl implements CashStatisticsService {

    @Autowired
    private CashStatisticsRepository cashStatisticsRepository;

    @Override
    public List<CashStatisticsDTO> getAllStatistics() {
        // 从数据库获取所有数据
        List<CashStatistics> allData = cashStatisticsRepository.findAll();

        // 转换为DTO
        List<CashStatisticsDTO> result = new ArrayList<>();

        // 添加会计室数据（只处理type为0的数据）
        if (allData != null && !allData.isEmpty()) {
            List<CashStatisticsDTO> accountingDTOs = allData.stream()
                    .filter(item -> item.getTableType() != null && item.getTableType() == 0) // 只筛选type为0的数据
                    .map(CashStatisticsDTO::new)
                    .collect(Collectors.toList());
            result.addAll(accountingDTOs);
        }
        // 添加会计室合计行
        if (allData != null && !allData.isEmpty()) {
            CashStatistics accountingTotal = calculateAccountingTotal(allData);
            CashStatisticsDTO accountingTotalDTO = createMergedRow(accountingTotal, "会计室合计", 2, 1);
            result.add(accountingTotalDTO);
        }

        // 预约中心标题行
        CashStatisticsDTO titleRow = new CashStatisticsDTO();
        titleRow.setDisplayName("预约中心");
        titleRow.setColspan(14);
        titleRow.setRowspan(1);
        titleRow.setIsMerged(true);
        result.add(titleRow);


        // 添加预约数据（只处理type为1的数据）
        if (allData != null && !allData.isEmpty()) {
            List<CashStatisticsDTO> accountingDTOs = allData.stream()
                    .filter(item -> item.getTableType() != null && item.getTableType() == 1) // 只筛选type为1的数据
                    .map(CashStatisticsDTO::new)
                    .collect(Collectors.toList());
            result.addAll(accountingDTOs);
        }


        // 添加预约合计行
        if (allData != null && !allData.isEmpty()) {
            CashStatistics appointmentTotal = calculateAppointmentTotal(allData);
            CashStatisticsDTO appointmentTotalDTO = createMergedRow(appointmentTotal, "预约合计", 2, 1);
            result.add(appointmentTotalDTO);
        }

        // 添加总计行
        if ((allData != null && !allData.isEmpty()) || (allData != null && !allData.isEmpty())) {
            CashStatistics accountingTotal = calculateAccountingTotal(allData);
            CashStatistics appointmentTotal = calculateAppointmentTotal(allData);
            CashStatistics grandTotal = calculateGrandTotal(accountingTotal, appointmentTotal);
            CashStatisticsDTO grandTotalDTO = createMergedRow(grandTotal, "总计", 2, 1);
            result.add(grandTotalDTO);
        }

        // 添加自定义行
        result.addAll(createCustomRows(3));

        return result;
    }

    @Override
    public List<CashStatistics> getDataByType(Integer type) {
        return cashStatisticsRepository.findByTableType(type);
    }

    @Override
    public List<CashStatistics> getAccountingData() {
        return null;
    }

    @Override
    public List<CashStatistics> getAppointmentData() {
        return null;
    }




    @Override
    public CashStatistics getById(Integer id) {
        // 这里需要实现根据ID查询的逻辑
        // 暂时返回null，您可以根据实际需求实现
        return null;
    }

    @Override
    public CashStatistics add(CashStatistics item) {
        item.calculateFormulas();
        // 这里需要实现保存到数据库的逻辑
        return item;
    }

    @Override
    public CashStatistics update(Integer id, CashStatistics item) {
        item.setId(id);
        item.calculateFormulas();
        // 这里需要实现更新数据库的逻辑
        return item;
    }

    @Override
    public boolean delete(Integer id) {
        // 这里需要实现从数据库删除的逻辑
        return true;
    }

    @Override
    public CashStatistics calculateAccountingTotal(List<CashStatistics> data) {
        CashStatistics total = new CashStatistics();
        total.setName("会计室合计");

        total.setHisAdvancePayment(sumField(data, CashStatistics::getHisAdvancePayment));
        total.setHisMedicalIncome(sumField(data, CashStatistics::getHisMedicalIncome));
        total.setHisRegistrationIncome(sumField(data, CashStatistics::getHisRegistrationIncome));
        total.setReportAmount(sumField(data, CashStatistics::getReportAmount));
        total.setPreviousTemporaryReceipt(sumField(data, CashStatistics::getPreviousTemporaryReceipt));
        total.setCurrentTemporaryReceipt(sumField(data, CashStatistics::getCurrentTemporaryReceipt));
        total.setRetainedCash(sumField(data, CashStatistics::getRetainedCash));
        total.setPettyCash(sumField(data, CashStatistics::getPettyCash));

        // 重新计算公式字段
        total.calculateFormulas();

        return total;
    }

    @Override
    public CashStatistics calculateAppointmentTotal(List<CashStatistics> data) {
        CashStatistics total = new CashStatistics();
        total.setName("预约合计");

        total.setHisAdvancePayment(sumField(data, CashStatistics::getHisAdvancePayment));
        total.setHisMedicalIncome(sumField(data, CashStatistics::getHisMedicalIncome));
        total.setHisRegistrationIncome(sumField(data, CashStatistics::getHisRegistrationIncome));
        total.setReportAmount(sumField(data, CashStatistics::getReportAmount));
        total.setPreviousTemporaryReceipt(sumField(data, CashStatistics::getPreviousTemporaryReceipt));
        total.setCurrentTemporaryReceipt(sumField(data, CashStatistics::getCurrentTemporaryReceipt));
        total.setRetainedCash(sumField(data, CashStatistics::getRetainedCash));
        total.setPettyCash(sumField(data, CashStatistics::getPettyCash));

        // 重新计算公式字段
        total.calculateFormulas();

        return total;
    }

    @Override
    public CashStatistics calculateGrandTotal(CashStatistics accountingTotal, CashStatistics appointmentTotal) {
        CashStatistics total = new CashStatistics();
        total.setName("总计");

        total.setHisAdvancePayment(sum(accountingTotal.getHisAdvancePayment(), appointmentTotal.getHisAdvancePayment()));
        total.setHisMedicalIncome(sum(accountingTotal.getHisMedicalIncome(), appointmentTotal.getHisMedicalIncome()));
        total.setHisRegistrationIncome(sum(accountingTotal.getHisRegistrationIncome(), appointmentTotal.getHisRegistrationIncome()));
        total.setReportAmount(sum(accountingTotal.getReportAmount(), appointmentTotal.getReportAmount()));
        total.setPreviousTemporaryReceipt(sum(accountingTotal.getPreviousTemporaryReceipt(), appointmentTotal.getPreviousTemporaryReceipt()));
        total.setCurrentTemporaryReceipt(sum(accountingTotal.getCurrentTemporaryReceipt(), appointmentTotal.getCurrentTemporaryReceipt()));
        total.setRetainedCash(sum(accountingTotal.getRetainedCash(), appointmentTotal.getRetainedCash()));
        // 由于总计行不涉及备用金合计，这两项可以直接设置为0或null
        total.setPettyCash(0d);
//        total.setPettyCash(sum(accountingTotal.getPettyCash(), appointmentTotal.getPettyCash()));

        // 重新计算公式字段
        total.calculateFormulas();

        return total;
    }

    // 表头常量
    private static final String[] TABLE_HEADERS = {
            "序号", "名称", "预交金收入", "医疗收入", "挂号收入",
            "应交报表数", "前日暂收款", "实交报表数",
            "当日暂收款", "实收现金数", "留存数差额",
            "留存现金数", "备用金", "备注"
    };

    @Override
    public List<CashStatisticsTableDTO> getAllStatisticsTable() {
        // 一次性查询所有数据
        List<CashStatistics> allData = cashStatisticsRepository.findAll();

        // 构建表格数据
        CashStatisticsTableDTO table = new CashStatisticsTableDTO();
        initTableHeaders(table);

        // 按类型分组处理数据
        Map<Integer, List<CashStatistics>> groupedData = allData.stream()
                .collect(Collectors.groupingBy(CashStatistics::getTableType));

        List<CashStatistics> type0Data = groupedData.getOrDefault(0, Collections.emptyList());
        List<CashStatistics> type1Data = groupedData.getOrDefault(1, Collections.emptyList());

        processTableData(table, type0Data, type1Data);
        return new ArrayList<>(Collections.singletonList(table));
    }

    private void initTableHeaders(CashStatisticsTableDTO table) {
        for (String header : TABLE_HEADERS) {
            table.addHeader(header);
        }
    }

    private void processTableData(CashStatisticsTableDTO table,
                                List<CashStatistics> type0Data,
                                List<CashStatistics> type1Data) {
                // 处理类型0数据
                int currentRowIndex = processRowsByType(table, type0Data, 0, 0);

                // 添加中间合并行
                table.addMergeConfig(new CellMergeConfig(
                        currentRowIndex, 0, 1, table.getHeaders().size(), "预约中心"
                ));
                currentRowIndex++;

                // 处理类型1数据
                processRowsByType(table, type1Data, currentRowIndex, 1);

                // 添加自定义合并配置
                table.addMergeConfig(new CellMergeConfig(16, 12, type1Data.size(), 2, "预约中心"));
        //        table.addMergeConfig(new CellMergeConfig(21, 13, 3, 5, "合并内容2"));
    }


    private int processRowsByType(CashStatisticsTableDTO table,
                                List<CashStatistics> data,
                                int startIndex,
                                int rowType) {
        if (data == null || data.isEmpty()) {
            return startIndex;
        }

        int currentIndex = startIndex;
        for (CashStatistics item : data) {
            CashStatisticsRow row = new CashStatisticsRow(rowType, item);
            row.setRowIndex(currentIndex++);
            table.addRow(row);
        }
        return currentIndex;
    }




    private List<CashStatisticsDTO> createCustomRows(int rowspan) {
        List<CashStatisticsDTO> customRows = new ArrayList<>();

        // 添加自定义行
        customRows.add(createCustomMergedRow("当日暂收款", rowspan));
        customRows.add(createCustomMergedRow("日报表数", rowspan));
        customRows.add(createCustomMergedRow("合计存款金额", rowspan));
        customRows.add(createCustomMergedRow("住院部当日借款", rowspan));
        customRows.add(createCustomMergedRow("住院部当日回款", rowspan));
        customRows.add(createCustomMergedRow("门诊当日借款", rowspan));
        customRows.add(createCustomMergedRow("门诊当日回款", rowspan));
        customRows.add(createCustomMergedRow("门诊当日抵扣报表金额", rowspan));
        customRows.add(createCustomMergedRow("门诊当日退主病房", rowspan));
        customRows.add(createCustomMergedRow("门诊当日退三住院部", rowspan));
        customRows.add(createCustomMergedRow("门诊当日实存金额", rowspan));

        // 审核行
        CashStatisticsDTO auditRow = new CashStatisticsDTO();
        auditRow.setDisplayName("审核：");
        auditRow.setColspan(9);
        auditRow.setRowspan(1);
        auditRow.setIsMerged(true);
        customRows.add(auditRow);

        // 出纳行
        CashStatisticsDTO cashierRow = new CashStatisticsDTO();
        cashierRow.setDisplayName("出纳：");
        cashierRow.setColspan(5);
        cashierRow.setRowspan(1);
        cashierRow.setIsMerged(true);
        customRows.add(cashierRow);

        return customRows;
    }

    private CashStatisticsDTO createCustomMergedRow(String displayName, int colspan) {
        CashStatisticsDTO row = new CashStatisticsDTO();
        row.setDisplayName(displayName);
        row.setColspan(colspan);
        row.setRowspan(1);
        row.setIsMerged(true);
        return row;
    }

    private CashStatisticsDTO createMergedRow(CashStatistics entity, String displayName, int colspan, int rowspan) {
        CashStatisticsDTO dto = new CashStatisticsDTO(entity);
        dto.setDisplayName(displayName);
        dto.setColspan(colspan);
        dto.setRowspan(rowspan);
        dto.setIsMerged(true);
        return dto;
    }

    private double sumField(List<CashStatistics> data, Function<CashStatistics, Double> fieldGetter) {
        if (data == null || data.isEmpty()) {
            return 0.0;
        }
        return data.stream()
                .mapToDouble(item -> {
                    Double value = fieldGetter.apply(item);
                    return value != null ? value : 0.0;
                })
                .sum();
    }

    private Double sum(Double value1, Double value2) {
        double sum = (value1 != null ? value1 : 0.0) + (value2 != null ? value2 : 0.0);
        return sum == 0.0 ? null : sum;
    }
}