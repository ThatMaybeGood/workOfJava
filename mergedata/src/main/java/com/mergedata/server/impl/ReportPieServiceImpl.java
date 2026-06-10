package com.mergedata.server.impl;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.mergedata.constants.Constant;
import com.mergedata.exception.BusinessException;
import com.mergedata.model.dto.InpReportRequestBody;
import com.mergedata.model.dto.OutpReportPieRequestBody;
import com.mergedata.model.dto.OutpReportRequestBody;
import com.mergedata.model.dto.external.HisInpIncomeResponseDTO;
import com.mergedata.model.dto.external.HisOutpIncomeResponseDTO;
import com.mergedata.model.entity.*;
import com.mergedata.model.vo.OutpReportMainVO;
import com.mergedata.model.vo.OutpReportSubVO;
import com.mergedata.model.vo.pie.*;
import com.mergedata.server.*;
import com.mergedata.util.PrimaryKeyGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import javax.xml.soap.Detail;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ReportPieServiceImpl implements ReportPieService {

    @Autowired
    HisDataService hisdata;

    @Autowired
    YQCashService cashService;


    @Autowired
    OutpReportService outpReportService;


    @Autowired
    YQHolidayService holidayService;

    @Autowired
    YQOperatorService operatorService;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Override
    public OutReportPieDTO queryOutpReportPie(OutpReportPieRequestBody body) {
        return queryReport(body.getStartDate(),body.getEndDate());
    }


    /**
     * 查询财务报表数据
     */
    public OutReportPieDTO queryReport(LocalDate startDate, LocalDate endDate) {

        OutReportPieDTO dto = new OutReportPieDTO();

        // 1. 设置查询日期范围
        DateRangeDTO dateRange = new DateRangeDTO();
        dateRange.setStartDate(startDate.toString());
        dateRange.setEndDate(endDate.toString());
        long days = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        dateRange.setDays((int) days);
        dto.setQueryDateRange(dateRange);

        // 2. 获取核心5个项目数据（从数据库查询，这里模拟）
        dto.setCoreItems(getCoreItems(startDate, endDate));

        // 3. 获取辅助5个项目数据
        dto.setAuxiliaryItems(getAuxiliaryItems(startDate, endDate));

        // 4. 计算汇总统计
        dto.setSummary(calculateSummary(dto.getCoreItems(), dto.getAuxiliaryItems()));

        return dto;
    }

    /**
     * 获取核心5个项目数据（预交金、门诊收入、暂收款、实交报表数、疫苗收入）
     * 实际开发中这里应该查询数据库
     */
    private Map<String, ItemDetailDTO> getCoreItems(LocalDate startDate, LocalDate endDate) {
//        List<OutpCashSubEntity> allSubs = outpReportService.findBatchByDateRange(startDate, endDate, Constant.NO)
//                .stream()
//                .flatMap(main -> main.getSubs().stream())
//                .collect(Collectors.toList());
//
//        // 分组统计
//        Map<String, ItemStats> statsMap = new HashMap<>();
//
//        // 正常用户统计
//        List<OutpCashSubEntity> normalSubs = allSubs.stream()
//                .filter(sub -> !"TEST_LY2".equals(sub.getDbUser()))
//                .collect(Collectors.toList());
//
//        statsMap.put("prepay", calculateStats(normalSubs, OutpCashSubEntity::getHisAdvancePayment));
//        statsMap.put("medical", calculateStats(normalSubs, OutpCashSubEntity::getHisMedicalIncome));
//        statsMap.put("tempReceive", calculateStats(normalSubs, OutpCashSubEntity::getCurrentTemporaryReceipt));
//        statsMap.put("actualReport", calculateStats(normalSubs, OutpCashSubEntity::getActualReportAmount));
//        statsMap.put("preTempReceive", calculateStats(normalSubs, OutpCashSubEntity::getPreviousTemporaryReceipt));
//
//        // 疫苗用户统计
//        List<OutpCashSubEntity> vaccineSubs = allSubs.stream()
//                .filter(sub -> "TEST_LY2".equals(sub.getDbUser()))
//                .collect(Collectors.toList());
//        statsMap.put("vaccine", calculateStats(vaccineSubs, OutpCashSubEntity::getHisMedicalIncome));
//
//        // 构建返回结果
//        Map<String, ItemDetailDTO> coreItems = new LinkedHashMap<>();
//        coreItems.put("prepay", buildItemDetail("HIS预交金", startDate, endDate,
//                statsMap.get("prepay")));
//        coreItems.put("clinicIncome", buildItemDetail("his医疗收入", startDate, endDate,
//                statsMap.get("medical")));
//        coreItems.put("tempReceive", buildItemDetail("当日暂收款", startDate, endDate,
//                statsMap.get("tempReceive")));
//        coreItems.put("actualReport", buildItemDetail("实交报表数", startDate, endDate,
//                statsMap.get("actualReport")));
//        coreItems.put("vaccine", buildItemDetail("疫苗收入", startDate, endDate,
//                statsMap.get("vaccine")));
//        coreItems.put("preTempReceive", buildItemDetail("前日暂收款", startDate, endDate,
//                statsMap.get("preTempReceive")));


        // 模拟数据示例，实际开发中应该从数据库查询
        Map<String, ItemDetailDTO> coreItems = new LinkedHashMap<>();
        coreItems.put("prepay", buildItemDetail("HIS预交金", startDate, endDate, new BigDecimal(32680), 132, new BigDecimal(4280), 18));
        coreItems.put("clinicIncome", buildItemDetail("门诊医疗收入", startDate, endDate, new BigDecimal(49650), 215, new BigDecimal(2950), 24));
        coreItems.put("tempReceive", buildItemDetail("当日暂收款", startDate, endDate, new BigDecimal(15120), 84, new BigDecimal(1380), 11));
        coreItems.put("actualReport", buildItemDetail("实交报表数", startDate, endDate, new BigDecimal(38250), 168, new BigDecimal(2060), 16));
        coreItems.put("vaccine", buildItemDetail("疫苗收入", startDate, endDate, new BigDecimal(10280), 48, new BigDecimal(580), 7));
        coreItems.put("preTempReceive", buildItemDetail("前日暂收款", startDate, endDate, new BigDecimal(15120), 48, new BigDecimal(1380), 352));


        return coreItems;
    }

    // 通用的统计方法
    private ItemStats calculateStats(List<OutpCashSubEntity> subs,
                                     Function<OutpCashSubEntity, BigDecimal> amountExtractor) {
        ItemStats stats = new ItemStats();

        for (OutpCashSubEntity sub : subs) {
            BigDecimal amount = amountExtractor.apply(sub);
            if (amount == null) {
                continue;
            }
            if (amount.compareTo(BigDecimal.ZERO) > 0) {
                stats.addIncome(amount);
            } else if (amount.compareTo(BigDecimal.ZERO) < 0) {
                stats.addRefund(amount);
            }
        }

        return stats;
    }

    // 重载 buildItemDetail 方法，接受 ItemStats 对象
    private ItemDetailDTO buildItemDetail(String itemName, LocalDate startDate,
                                          LocalDate endDate, ItemStats stats) {
        return buildItemDetail(itemName, startDate, endDate,
                stats.getIncomeAmt(), stats.getIncomeCnt(),
                stats.getRefundAmt(), stats.getRefundCnt());
    }

    /**
     * 构建单个项目的完整数据
     */
    private ItemDetailDTO buildItemDetail(String itemName, LocalDate startDate, LocalDate endDate,
                                          BigDecimal incomeAmt, int incomeCnt,
                                          BigDecimal refundAmt, int refundCnt) {
        // 实际开发中，这些数据应该从数据库聚合查询得到
        // 这里根据日期范围做个简单模拟，天数越多金额越大
        long days = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        double factor = days;

        ItemDetailDTO detail = new ItemDetailDTO();

        // 收入
        CoreMetricDTO income = new CoreMetricDTO();
        income.setItemName(itemName);
        income.setAmount(incomeAmt);
        income.setCount((int) (incomeCnt));
        detail.setIncome(income);

        // 退款
        CoreMetricDTO refund = new CoreMetricDTO();
        refund.setItemName(itemName);
        refund.setAmount(refundAmt);
        refund.setCount((int) (refundCnt * factor));
        detail.setRefund(refund);

        // 合计
        CoreMetricDTO total = new CoreMetricDTO();
        total.setItemName(itemName);
        total.setAmount(income.getAmount().subtract(refund.getAmount()));
        total.setCount(income.getCount() + refund.getCount());
        detail.setTotal(total);

        return detail;
    }

    /**
     * 获取辅助5个项目数据（门诊借款、住院借款、门诊回款、住院回款、门诊实存）
     */
    private Map<String, AuxiliaryMetricDTO> getAuxiliaryItems(LocalDate startDate, LocalDate endDate) {
        Map<String, AuxiliaryMetricDTO> auxiliaryItems = new LinkedHashMap<>();

        long days = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        double factor = days;

        auxiliaryItems.put("outpatientLoan", buildAuxiliaryItem("门诊借款", 6350 * factor));
        auxiliaryItems.put("inpatientLoan", buildAuxiliaryItem("住院借款", 14500 * factor));
        auxiliaryItems.put("outpatientRepay", buildAuxiliaryItem("门诊回款", 5600 * factor));
        auxiliaryItems.put("inpatientRepay", buildAuxiliaryItem("住院回款", 11050 * factor));
        auxiliaryItems.put("outpatientSave", buildAuxiliaryItem("门诊实存", 26800 * factor));

        return auxiliaryItems;
    }

    /**
     * 构建辅助项目
     */
    private AuxiliaryMetricDTO buildAuxiliaryItem(String itemName, double amount) {
        AuxiliaryMetricDTO item = new AuxiliaryMetricDTO();
        item.setItemName(itemName);
        item.setAmount(BigDecimal.valueOf(amount));
        return item;
    }

    /**
     * 计算汇总统计
     */
    private SummaryDTO calculateSummary(Map<String, ItemDetailDTO> coreItems,
                                        Map<String, AuxiliaryMetricDTO> auxiliaryItems) {
        SummaryDTO summary = new SummaryDTO();

        // 初始化汇总对象
        CoreMetricDTO totalRepay = new CoreMetricDTO();
        totalRepay.setAmount(BigDecimal.ZERO);
        totalRepay.setCount(0);

        CoreMetricDTO totalLoan = new CoreMetricDTO();
        totalLoan.setAmount(BigDecimal.ZERO);
        totalLoan.setCount(0);

        CoreMetricDTO totalNet = new CoreMetricDTO();
        totalNet.setAmount(BigDecimal.ZERO);
        totalNet.setCount(0);

        // 累加核心项目
        for (ItemDetailDTO item : coreItems.values()) {
            totalRepay.setAmount(totalRepay.getAmount().add(item.getIncome().getAmount()));
            totalRepay.setCount(totalRepay.getCount() + item.getIncome().getCount());

            totalLoan.setAmount(totalLoan.getAmount().add(item.getRefund().getAmount()));
            totalLoan.setCount(totalLoan.getCount() + item.getRefund().getCount());

            totalNet.setAmount(totalNet.getAmount().add(item.getTotal().getAmount()));
            totalNet.setCount(totalNet.getCount() + item.getTotal().getCount());
        }

        summary.setTotalRepay(totalRepay);
        summary.setTotalLoan(totalLoan);
        summary.setTotalNet(totalNet);

        // 累加辅助项目总金额
        BigDecimal totalAuxiliary = BigDecimal.ZERO;
        for (AuxiliaryMetricDTO item : auxiliaryItems.values()) {
            totalAuxiliary = totalAuxiliary.add(item.getAmount());
        }
        summary.setTotalAuxiliary(totalAuxiliary);

        return summary;
    }

}