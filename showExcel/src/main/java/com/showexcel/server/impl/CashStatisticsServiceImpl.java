package com.showexcel.server.impl;

import com.showexcel.model.CashStatistics;
import com.showexcel.dao.CashStatisticsDTO;
import com.showexcel.repository.CashStatisticsRepository;
import com.showexcel.server.CashStatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Mine
 * @version 1.0
 * 描述:
 * @date 2025/10/29 15:54
 */

@Service
public class CashStatisticsServiceImpl implements CashStatisticsService {

    @Autowired
    private CashStatisticsRepository cashStatisticsRepository;

    @Override
    public List<CashStatisticsDTO> getAllStatistics() {
        // 从数据库获取所有数据
        List<CashStatistics> allData = cashStatisticsRepository.findAll();

        // 按类型分组
        List<CashStatistics> accountingData = getAccountingData();
        List<CashStatistics> appointmentData = getAppointmentData();

        // 转换为DTO
        List<CashStatisticsDTO> result = new ArrayList<>();

        // 添加会计室数据
        if (accountingData != null && !accountingData.isEmpty()) {
            List<CashStatisticsDTO> accountingDTOs = accountingData.stream()
                    .map(CashStatisticsDTO::new)
                    .collect(Collectors.toList());
            result.addAll(accountingDTOs);
        }
        // 添加会计室合计行
        if (accountingData != null && !accountingData.isEmpty()) {
            CashStatistics accountingTotal = calculateAccountingTotal(accountingData);
            CashStatisticsDTO accountingTotalDTO = createMergedRow(accountingTotal, "会计室合计", 2, 1);
            result.add(accountingTotalDTO);
        }

        // 添加预约中心数据
        if (appointmentData != null && !appointmentData.isEmpty()) {
            List<CashStatisticsDTO> accountingDTOs = appointmentData.stream()
                    .map(CashStatisticsDTO::new)
                    .collect(Collectors.toList());
            result.addAll(accountingDTOs);
        }

        // 添加预约合计行
        if (appointmentData != null && !appointmentData.isEmpty()) {
            CashStatistics appointmentTotal = calculateAppointmentTotal(appointmentData);
            CashStatisticsDTO appointmentTotalDTO = createMergedRow(appointmentTotal, "预约合计", 2, 1);
            result.add(appointmentTotalDTO);
        }

        // 添加总计行
        if ((accountingData != null && !accountingData.isEmpty()) || (appointmentData != null && !appointmentData.isEmpty())) {
            CashStatistics accountingTotal = calculateAccountingTotal(accountingData);
            CashStatistics appointmentTotal = calculateAppointmentTotal(appointmentData);
            CashStatistics grandTotal = calculateGrandTotal(accountingTotal, appointmentTotal);
            CashStatisticsDTO grandTotalDTO = createMergedRow(grandTotal, "总计", 2, 1);
            result.add(grandTotalDTO);
        }

        // 添加自定义行
        result.addAll(createCustomRows());

        return result;
    }

    @Override
    public List<CashStatistics> getDataByType(Integer type) {
        return cashStatisticsRepository.findByType(type);
    }



    @Override
    public List<CashStatistics> getAccountingData() {
        return getDataByType(0);
    }

    @Override
    public List<CashStatistics> getAppointmentData() {
        return getDataByType(1);
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
        total.setPettyCash(sum(accountingTotal.getPettyCash(), appointmentTotal.getPettyCash()));

        // 重新计算公式字段
        total.calculateFormulas();

        return total;
    }

    // ============ 私有方法 ============

    private List<CashStatisticsDTO> createAppointmentCenterRows(List<CashStatistics> appointmentData) {
        List<CashStatisticsDTO> appointmentRows = new ArrayList<>();

        if (appointmentData == null || appointmentData.isEmpty()) {
            return appointmentRows;
        }

        // 预约中心标题行
        CashStatisticsDTO titleRow = new CashStatisticsDTO();
        titleRow.setDisplayName("预约中心");
        titleRow.setColspan(14);
        titleRow.setRowspan(1);
        titleRow.setIsMerged(true);
        appointmentRows.add(titleRow);

        // 预约中心数据行
        List<CashStatisticsDTO> dataRows = appointmentData.stream()
                .map(CashStatisticsDTO::new)
                .collect(Collectors.toList());

        // 如果有数据，设置第一行为合并单元格
        if (!dataRows.isEmpty()) {
            CashStatisticsDTO firstRow = dataRows.get(0);
            firstRow.setDisplayName("预约中心");
            firstRow.setColspan(2); // 向左合并2列
            firstRow.setRowspan(appointmentData.size()); // 向下合并所有行
            firstRow.setIsMerged(true);
        }

        appointmentRows.addAll(dataRows);

        return appointmentRows;
    }

    private List<CashStatisticsDTO> createCustomRows() {
        List<CashStatisticsDTO> customRows = new ArrayList<>();

        // 添加自定义行
        customRows.add(createCustomMergedRow("当日暂收款", 2));
        customRows.add(createCustomMergedRow("日报表数", 2));
        customRows.add(createCustomMergedRow("合计存款金额", 2));
        customRows.add(createCustomMergedRow("住院部当日借款", 2));
        customRows.add(createCustomMergedRow("住院部当日回款", 2));
        customRows.add(createCustomMergedRow("门诊当日借款", 2));
        customRows.add(createCustomMergedRow("门诊当日回款", 2));
        customRows.add(createCustomMergedRow("门诊当日抵扣报表金额", 2));
        customRows.add(createCustomMergedRow("门诊当日退主病房", 2));
        customRows.add(createCustomMergedRow("门诊当日退三住院部", 2));
        customRows.add(createCustomMergedRow("门诊当日实存金额", 2));

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