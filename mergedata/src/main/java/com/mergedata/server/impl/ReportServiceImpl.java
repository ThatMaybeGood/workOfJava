package com.mergedata.server.impl;

import com.mergedata.dto.*;
import com.mergedata.server.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ReportServiceImpl implements ReportService {


    private String HIS_DATA_URL;


    @Autowired
    private HisDataService hisDataService;
    @Autowired
    private YQCashRegRecordService yqCashRegRecordService;
    @Autowired
    private HisOperatorService hisOperatorService;
    @Autowired
    private HolidayCalendarService holidayCalendarService;

    @Autowired
    private RestTemplate restTemplate;

    @Override
    public List<ReportDTO> getAll(LocalDate reportDate) {

        // 1、判断当前日期类型  0 正常 ，1 节假日 ，2 节假日前一天 3 节假日后一天
        // 2、根据日期类型获取对应的操作员数据
        // 3、将获取到的数据转换为DTO对象，并填充到结果集中
        return getAllReportData(reportDate);
    }

    @Override
    public Boolean insert(LocalDate reportDate) {
        // 建议: 如果方法未实现，返回 false 或抛出异常
        // throw new UnsupportedOperationException("Insert operation is not yet implemented.");
        return false;
    }


    public List<ReportDTO> getAllReportData(LocalDate reportDate) {
        try {
            // 1. 获取所有数据（列表形式）
            List<YQOperatorDTO> operators = hisOperatorService.findData(); // 操作员列表
            List<HisIncomeDTO> hisIncomeDTOList = hisDataService.findByDate(reportDate);           // His数据列表
            List<YQCashRegRecordDTO> yqDataList = yqCashRegRecordService.findByDate(reportDate);     // YQ数据列表

            // 2. 将关联数据转换为以 operatorNo 为key的Map, 使用 (v1, v2) -> v1 来处理可能的重复键
            Map<String, HisIncomeDTO> hisDataMap = hisIncomeDTOList.stream()
                    .collect(Collectors.toMap(HisIncomeDTO::getOperatorNo, Function.identity(), (v1, v2) -> v1));

            Map<String, YQCashRegRecordDTO> cashRecordMap = yqDataList.stream()
                    .collect(Collectors.toMap(YQCashRegRecordDTO::getOperatorNo, Function.identity(), (v1, v2) -> v1));

            // 3. 以操作员为主，构建结果集
            List<ReportDTO> result = new ArrayList<>();
            
            // 优化: 在循环外计算一次即可
            double actualReportAmount = getActualReportAmount(reportDate);

            for (YQOperatorDTO operator : operators) {
                ReportDTO dto = new ReportDTO();

                // 设置操作员基础信息
                dto.setOperatorNo(operator.getOperatorNo());
                dto.setOperatorName(operator.getOperatorName());
                dto.setReportDate(reportDate);

                // 1、关联 HisData 数据（如果有）
                HisIncomeDTO hisIncomeDTO = hisDataMap.get(operator.getOperatorNo());
                if (hisIncomeDTO != null) {
                    dto.setHisAdvancePayment(getSafeDouble(hisIncomeDTO.getHisAdvancePayment()));
                    dto.setHisMedicalIncome(getSafeDouble(hisIncomeDTO.getHisMedicalIncome()));
                    //目前默认为0.00，后续根据实际业务调整
                    dto.setHisRegistrationIncome(0.00);

                    //应交报表数
                    dto.setReportAmount(dto.getHisAdvancePayment() + dto.getHisMedicalIncome() + dto.getHisRegistrationIncome());
                }

                // 2、关联 YQCashRegRecord 数据（如果有）
                YQCashRegRecordDTO cashRecord = cashRecordMap.get(operator.getOperatorNo());
                if (cashRecord != null) {
                    dto.setRetainedCash(getSafeDouble(cashRecord.getRetainedCash()));
                    dto.setWindowNo(cashRecord.getWindowNo());
                    dto.setOperatType(cashRecord.getOperatType());
                    dto.setSechduling(cashRecord.getSechduling());
                    dto.setApplyDate(cashRecord.getApplyDate());
                    
                    // 修复: 将依赖 cashRecord 的计算移入此代码块
                    dto.setRetainedDifference(cashRecord.getRetainedCash() - actualReportAmount - dto.getPettyCash());
                }

                //前日暂收款 正常情况为前一天的当日暂收款
                dto.setPreviousTemporaryReceipt(0.00);
                dto.setHolidayTemporaryReceipt(0.00);
                dto.setCurrentTemporaryReceipt(0.00);

                //实交报表数  通过计算得出
                dto.setActualReportAmount(actualReportAmount);
                dto.setActualCashAmount(dto.getActualReportAmount() + dto.getCurrentTemporaryReceipt());

                // 以放入结果
                result.add(dto);
            }

            log.info("{}生成报表完成，共处理 {} 个操作员", reportDate, result.size());

            return result;
        } catch (Exception e) {
            log.error("报表生成失败", e);
            // 建议: 返回空集合而不是 null
            return Collections.emptyList();
        }
    }


    /*
    根据日期的情况分类得到最后时间，
     1、判断当前日期类型  0 正常 ，1 节假日 ，2 节假日前一天 3 节假日后一天
     */
    public double getActualReportAmount(LocalDate reportDate) {
        Double amount = 0.00;

        //1、获取目前维护的节假日列表
        List<HolidayCalendarDTO> holidayCalendarDTOS = holidayCalendarService.findByDate(reportDate);

        if (holidayCalendarDTOS.isEmpty()) {
            log.warn("No holiday calendar data found.");
            return amount;
        }

        //1、判断当前日期是否为节假日
        if (isDateInvalid(holidayCalendarDTOS, reportDate)) {
            //节假日
        }

        if (isDateInvalid(holidayCalendarDTOS, reportDate.minusDays(1))) {
            //节假日后一天
            while (true) {
                reportDate = reportDate.minusDays(1);
                if (isDateInvalid(holidayCalendarDTOS, reportDate)) {
                    break;
                } else {
                    amount = amount + 0.00;
                }

            }
        }

        if (isDateInvalid(holidayCalendarDTOS, reportDate.plusDays(1))) {
            //节假日前一天
        }

        //正常情况


        return amount;

    }

    // 安全获取Double值
    private Double getSafeDouble(Double value) {
        return value != null ? value : 0.0;
    }

    // 计算衍生字段
    private ReportDTO calculateTotal(List<ReportDTO> dto, LocalDate reportDate) {
        // 汇总数据，计算总金额等字段
        ReportDTO total = new ReportDTO();
        total.setOperatorNo("");
        total.setOperatorName("合计");

        total.setHisAdvancePayment(dto.stream().mapToDouble(ReportDTO::getHisAdvancePayment).sum());
        total.setHisMedicalIncome(dto.stream().mapToDouble(ReportDTO::getHisMedicalIncome).sum());
        total.setHisRegistrationIncome(dto.stream().mapToDouble(ReportDTO::getHisRegistrationIncome).sum());


        total.setRetainedCash(dto.stream().mapToDouble(ReportDTO::getRetainedCash).sum());

        total.setReportAmount(dto.stream().mapToDouble(ReportDTO::getReportAmount).sum());
        total.setPreviousTemporaryReceipt(dto.stream().mapToDouble(ReportDTO::getPreviousTemporaryReceipt).sum());
        total.setHolidayTemporaryReceipt(dto.stream().mapToDouble(ReportDTO::getHolidayTemporaryReceipt).sum());
        total.setActualCashAmount(dto.stream().mapToDouble(ReportDTO::getActualCashAmount).sum());
        total.setCurrentTemporaryReceipt(dto.stream().mapToDouble(ReportDTO::getCurrentTemporaryReceipt).sum());
        total.setActualCashAmount(dto.stream().mapToDouble(ReportDTO::getActualCashAmount).sum());
        total.setRetainedDifference(dto.stream().mapToDouble(ReportDTO::getRetainedDifference).sum());
        total.setPettyCash(dto.stream().mapToDouble(ReportDTO::getPettyCash).sum());

        total.setRemarks("合计行，不展示在报表中");
        total.setReportDate(reportDate);
        total.setCreateTime(LocalDateTime.now());


        return total;
    }


    /**
     * 最优解：字符串作废标志的HashSet方案
     */
    public boolean isDateInvalid(List<HolidayCalendarDTO> holidayCalendarDTOList, LocalDate targetDate) {
        if (holidayCalendarDTOList == null || targetDate == null) {
            return false;
        }

        // 先把所有日期提取到Set中
        Set<LocalDate> dateSet = holidayCalendarDTOList.stream()
                .map(HolidayCalendarDTO::getHolidayDate)
                .collect(Collectors.toSet());

        // 然后快速检查
        boolean exists = dateSet.contains(targetDate);

        return exists;
    }


}