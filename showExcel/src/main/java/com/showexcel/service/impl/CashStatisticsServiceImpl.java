package com.showexcel.service.impl;

import com.showexcel.constant.CashStatisticsConstant;
import com.showexcel.dto.CashStatisticsRow;
import com.showexcel.dto.CashStatisticsTableDTO;
import com.showexcel.model.CashStatistics;
import com.showexcel.dto.CashStatisticsDTO;
import com.showexcel.model.CellMergeConfig;
import com.showexcel.repository.CashStatisticsRepository;
import com.showexcel.service.CashStatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

import java.util.*;
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

    /**
     * 统一计算合计方法
     *
     * @param data 数据列表
     * @param type 计算类型：0-会计室合计，1-预约合计，2-总计
     * @return 合计结果
     */
    private CashStatistics calculateTotal(List<CashStatistics> data, int type) {
        CashStatistics total = new CashStatistics();

        switch (type) {
            case 0: // 会计室合计
                total.setName("会计室合计");
                break;
            case 1: // 预约合计
                total.setName("预约合计");
                break;
            case 2: // 总计
                total.setName("总计");
                break;
            default:
                total.setName("合计");
        }

        // 计算公共字段
        total.setHisAdvancePayment(sumField(data, CashStatistics::getHisAdvancePayment));
        total.setHisMedicalIncome(sumField(data, CashStatistics::getHisMedicalIncome));
        total.setHisRegistrationIncome(sumField(data, CashStatistics::getHisRegistrationIncome));
        total.setReportAmount(sumField(data, CashStatistics::getReportAmount));
        total.setPreviousTemporaryReceipt(sumField(data, CashStatistics::getPreviousTemporaryReceipt));
        total.setCurrentTemporaryReceipt(sumField(data, CashStatistics::getCurrentTemporaryReceipt));
        total.setRetainedCash(sumField(data, CashStatistics::getRetainedCash));

        // 根据类型处理pettyCash
        switch (type) {
            case 0: // 会计室合计：只有在包含类型0数据时才计算pettyCash
                if (data != null && !data.isEmpty()) {
                    boolean hasType0Data = data.stream().anyMatch(item -> item.getTableType() != null && item.getTableType() == 0);
                    if (hasType0Data) {
                        total.setPettyCash(sumField(data, CashStatistics::getPettyCash));
                    } else {
                        total.setPettyCash(0d);
                    }
                } else {
                    total.setPettyCash(0d);
                }
                break;
            case 1: // 预约合计：不计算pettyCash
                total.setPettyCash(0d);
                break;
            case 2: // 总计：不计算pettyCash
                total.setPettyCash(0d);
                break;
            default:
                total.setPettyCash(0d);
        }

        // 重新计算公式字段
        total.calculateFormulas();
        return total;
    }

    @Override
    public CashStatistics calculateAccountingTotal(List<CashStatistics> data) {
        return calculateTotal(data, 0);
    }

    @Override
    public CashStatistics calculateAppointmentTotal(List<CashStatistics> data) {
        return calculateTotal(data, 1);
    }

    @Override
    public CashStatistics calculateGrandTotal(List<CashStatistics> data) {
        return calculateTotal(data, 2);
    }


    @Override
    public List<CashStatisticsTableDTO> getAllStatisticsTable() {
        // 一次性查询所有数据
        List<CashStatistics> allData = cashStatisticsRepository.findAll();

        // 构建表格数据
        CashStatisticsTableDTO table = new CashStatisticsTableDTO();

        // 添加表头
        for (String header : CashStatisticsConstant.TABLE_HEADERS) {
            table.addHeader(header);
        }

        // 按类型分组处理数据
        Map<Integer, List<CashStatistics>> groupedData = allData.stream()
                .collect(Collectors.groupingBy(CashStatistics::getTableType));

        // 处理三种类型的数据
        processAllData(table, groupedData, allData);

        return Collections.singletonList(table);
    }

    /**
     * 处理所有数据，构建完整的表格结构
     * 包含三种类型的数据处理：
     * 1. 类型0数据（会计室数据 + 合计行）
     * 2. 类型1数据（预约数据 + 合计行）
     * 3. 类型2数据（总计行，前两个合计行的合计）
     * 4. 自定义行设置
     * 5. 合并单元格配置
     *
     * @param table 目标表格DTO对象
     * @param groupedData 按类型分组的数据
     * @param allData 所有原始数据
     */
    private void processAllData(CashStatisticsTableDTO table,
                              Map<Integer, List<CashStatistics>> groupedData,
                              List<CashStatistics> allData) {
        // 获取类型0和类型1的数据
        List<CashStatistics> type0Data = groupedData.getOrDefault(0, Collections.emptyList());
        List<CashStatistics> type1Data = groupedData.getOrDefault(1, Collections.emptyList());

        int currentRowIndex = 0;
        // 添加会计室行
        int accountingRowIndex = 0;
        // 添加预约中心行
        int appointmentRowIndex = 0;


        // 1. 处理类型0数据（会计室数据 + 合计行）
        if (!type0Data.isEmpty()) {
            // 添加明细数据
            currentRowIndex = processRowsByType(table, type0Data, currentRowIndex, 0);

            // 添加会计室合计行
            CashStatistics accountingTotal = calculateAccountingTotal(type0Data);
            accountingTotal.setTableType(0);
            List<CashStatistics> totalRow = Collections.singletonList(accountingTotal);
            currentRowIndex = processRowsByType(table, totalRow, currentRowIndex, 0);
        }

        // 2. 添加预约中心标题行
        table.addMergeConfig(new CellMergeConfig(currentRowIndex, 0, 1, table.getHeaders().size(), "预约中心"));
        currentRowIndex++;

        // 添加预约行索引
        appointmentRowIndex = currentRowIndex;

        // 3. 处理类型1数据（预约数据 + 合计行）
        if (!type1Data.isEmpty()) {
            // 添加明细数据
            currentRowIndex = processRowsByType(table, type1Data, currentRowIndex, 1);

            // 添加预约合计行
            CashStatistics appointmentTotal = calculateAppointmentTotal(type1Data);
            appointmentTotal.setTableType(1);
            List<CashStatistics> totalRow = Collections.singletonList(appointmentTotal);
            currentRowIndex = processRowsByType(table, totalRow, currentRowIndex, 1);
        }

        // 4. 处理类型2数据（总计行）
        if (!type0Data.isEmpty() || !type1Data.isEmpty()) {
            CashStatistics grandTotal = calculateGrandTotalFromAllData(allData);
            grandTotal.setTableType(2);
            List<CashStatistics> totalRow = Collections.singletonList(grandTotal);
            currentRowIndex = processRowsByType(table, totalRow, currentRowIndex, 2);
        }

        // 5. 添加自定义行
        String[] customRowNames = {"当日暂收款", "日报表数", "合计存款金额",
                "住院部当日借款", "住院部当日回款", "门诊当日借款", "门诊当日回款",
                "门诊当日抵扣报表金额", "门诊当日退主病房", "门诊当日退三住院部", "门诊当日实存金额"};
        for (String rowName : customRowNames) {
            table.addMergeConfig(new CellMergeConfig(currentRowIndex, 0, 1, 2, rowName));
            currentRowIndex++;
        }

        // 6. 添加审核和出纳行
        table.addMergeConfig(new CellMergeConfig(currentRowIndex, 0, 1, 2, "审核"));
        table.addMergeConfig(new CellMergeConfig(currentRowIndex, table.getHeaders().size() - 5, 1, 0, "出纳"));

        // 7. 添加自定义合并配置
        if (!type1Data.isEmpty()) {
            table.addMergeConfig(new CellMergeConfig(appointmentRowIndex, table.getHeaders().size() -2, type1Data.size()+1, 2, "合并预约中心"));
        }
    }



    /**
     * 计算所有数据的汇总合计（类型0和类型1的合计之和）
     *
     * @param allData 所有数据
     * @return 总合计数据
     */
    public CashStatistics calculateGrandTotalFromAllData(List<CashStatistics> allData) {
        // 过滤出类型0和类型1的数据
        List<CashStatistics> type0Data = allData.stream()
                .filter(item -> item.getTableType() != null && item.getTableType() == 0)
                .collect(Collectors.toList());
        List<CashStatistics> type1Data = allData.stream()
                .filter(item -> item.getTableType() != null && item.getTableType() == 1)
                .collect(Collectors.toList());

        // 分别计算两个类型的合计
        CashStatistics total0 = calculateAccountingTotal(type0Data);
        CashStatistics total1 = calculateAppointmentTotal(type1Data);

        // 计算总合计（两个合计的和）
        CashStatistics grandTotal = new CashStatistics();
        grandTotal.setName("总计");

        grandTotal.setHisAdvancePayment(sum(total0.getHisAdvancePayment(), total1.getHisAdvancePayment()));
        grandTotal.setHisMedicalIncome(sum(total0.getHisMedicalIncome(), total1.getHisMedicalIncome()));
        grandTotal.setHisRegistrationIncome(sum(total0.getHisRegistrationIncome(), total1.getHisRegistrationIncome()));
        grandTotal.setReportAmount(sum(total0.getReportAmount(), total1.getReportAmount()));
        grandTotal.setPreviousTemporaryReceipt(sum(total0.getPreviousTemporaryReceipt(), total1.getPreviousTemporaryReceipt()));
        grandTotal.setCurrentTemporaryReceipt(sum(total0.getCurrentTemporaryReceipt(), total1.getCurrentTemporaryReceipt()));
        grandTotal.setRetainedCash(sum(total0.getRetainedCash(), total1.getRetainedCash()));

        // 总合计不计算pettyCash
        grandTotal.setPettyCash(0d);

        // 重新计算公式字段
        grandTotal.calculateFormulas();
        return grandTotal;
    }


    /**
     * 根据行类型处理数据行并添加到表格中
     *
     * @param table 目标表格DTO对象
     * @param data 待处理的现金统计数据列表
     * @param startIndex 起始行索引
     * @param rowType 行类型标识
     * @return 处理后的下一个可用行索引
     */
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