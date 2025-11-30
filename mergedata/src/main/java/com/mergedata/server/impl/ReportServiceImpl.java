package com.mergedata.server.impl;

import com.mergedata.mapper.YQReportMapper;
import com.mergedata.model.*;
import com.mergedata.server.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ReportServiceImpl implements ReportService {

    @Autowired
    HisDataService hisdata;

    @Autowired
    YQCashService cashs;

    @Autowired
    YQOperatorService operors;

    @Autowired
    YQHolidayService hilidays;

    @Autowired
    YQReportMapper reports;

    List<ReportDTO> results = new ArrayList<>();


    @Override
    public List<ReportDTO> getAll(String reportdate)  {
        // 调用存储过程获取报表数据
        try {

//            // 调用通用方法，传入过程名和 Mapper
//            List<ReportDTO> resultLists = SPQueryDao.executeQueryNoParam(
//                    "GET_ALL_REPORTS",  // 存储过程名称
//                    yqReportMapper     // 对应的 RowMapper Bean
//            );
//
            //调用调用查询数据方法，传入参数为日期
           results = reports.getOpertList(Collections.singletonMap("A_REPORT_DATE", reportdate));
            // 判断结果集，判断是否平台有无数据，有则查询出返回，无则调用接口获取数据并返回
            if (results.isEmpty()){
                results = getAllReportData(reportdate);
            }
        } catch (Exception e) {
            log.error("获取报表数据异常", e);
        }

        return  results;


    }

    @Override
    public Boolean insert(String reportdate) {
        // 建议: 如果方法未实现，返回 false 或抛出异常
        // throw new UnsupportedOperationException("Insert operation is not yet implemented.");
        return false;
    }


    public List<ReportDTO> getAllReportData(String reportdate) {
        try {
            // 1. 获取所有数据（列表形式）
            List<YQOperator> operators = operors.findData(); // 操作员列表
            List<HisIncomeDTO> hisIncomeDTOList = hisdata.findByDate(reportdate);           // His数据列表
            List<YQCashRegRecordDTO> yqDataList = cashs.findByDate(reportdate);     // YQ数据列表

            // 2. 将关联数据转换为以 operatorNo 为key的Map, 使用 (v1, v2) -> v1 来处理可能的重复键
            Map<String, HisIncomeDTO> hisDataMap = hisIncomeDTOList.stream()
                    .collect(Collectors.toMap(HisIncomeDTO::getOperatorNo, Function.identity(), (v1, v2) -> v1));

            Map<String, YQCashRegRecordDTO> cashRecordMap = yqDataList.stream()
                    .collect(Collectors.toMap(YQCashRegRecordDTO::getOperatorNo, Function.identity(), (v1, v2) -> v1));

            // 3. 以操作员为主，构建结果集
            List<ReportDTO> result = new ArrayList<>();
            
            // 优化: 在循环外计算一次即可
            BigDecimal actualReportAmount = getActualReportAmount(reportdate);

            for (YQOperator operator : operators) {
                ReportDTO dto = new ReportDTO();

                // 设置操作员基础信息
                dto.setOperatorNo(operator.getOperatorNo());
                dto.setOperatorName(operator.getOperatorName());
                dto.setReportDate(reportdate);

                // 1、关联 HisData 数据（如果有）
                HisIncomeDTO hisIncomeDTO = hisDataMap.get(operator.getOperatorNo());
                if (hisIncomeDTO != null) {
                    dto.setHisAdvancePayment(getSafeBigDecimal(hisIncomeDTO.getHisAdvancePayment()));
                    dto.setHisMedicalIncome(getSafeBigDecimal(hisIncomeDTO.getHisMedicalIncome()));
                    //目前默认为0.00，后续根据实际业务调整
                    dto.setHisRegistrationIncome(BigDecimal.ZERO);

                    //应交报表数
                    dto.setReportAmount(dto.getHisAdvancePayment().add(dto.getHisMedicalIncome()).add(dto.getHisRegistrationIncome()));
                }

                // 2、关联 YQCashRegRecord 数据（如果有）
                YQCashRegRecordDTO cashRecord = cashRecordMap.get(operator.getOperatorNo());
                if (cashRecord != null) {
                    dto.setRetainedCash(getSafeBigDecimal(cashRecord.getRetainedCash()));
                    dto.setWindowNo(cashRecord.getWindowNo());
                    dto.setOperatType(cashRecord.getOpeType());
                    dto.setSechduling(cashRecord.getSechduling());
                    dto.setApplyDate(cashRecord.getApplyDate());
                    
                    // 修复: 将依赖 cashRecord 的计算移入此代码块
                    dto.setRetainedDifference(cashRecord.getRetainedCash().subtract(actualReportAmount).subtract(dto.getPettyCash()));
                }

                //前日暂收款 正常情况为前一天的当日暂收款
                dto.setPreviousTemporaryReceipt(BigDecimal.ZERO);
                dto.setHolidayTemporaryReceipt(BigDecimal.ZERO);
                dto.setCurrentTemporaryReceipt(BigDecimal.ZERO);

                //实交报表数  通过计算得出
                dto.setActualReportAmount(actualReportAmount);
                dto.setActualCashAmount(dto.getActualReportAmount().add(dto.getCurrentTemporaryReceipt()));

                // 以放入结果
                result.add(dto);
            }

            log.info("{}生成报表完成，共处理 {} 个操作员", reportdate, result.size());

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
    public BigDecimal getActualReportAmount(String date) {

        BigDecimal amount = BigDecimal.ZERO;
        LocalDate reportdate = LocalDate.parse(date);


        //1、获取目前维护的节假日列表
        List<YQHolidayCalendarDTO> YQHolidayCalendarDTOS = hilidays.findByDate(date);

        if (YQHolidayCalendarDTOS.isEmpty()) {
            log.warn("No holiday calendar data found.");
            return amount;
        }

        //1、判断当前日期是否为节假日
        if (isDateInvalid(YQHolidayCalendarDTOS, reportdate)) {
            //节假日
        }

        if (isDateInvalid(YQHolidayCalendarDTOS, reportdate.minusDays(1))) {
            //节假日后一天
            while (true) {
                reportdate = reportdate.minusDays(1);
                if (isDateInvalid(YQHolidayCalendarDTOS, reportdate)) {
                    break;
                } else {
                    //加amount = amount + 昨日留存现金数
                    amount = amount.add(BigDecimal.ZERO) ;
                }

            }
        }

        if (isDateInvalid(YQHolidayCalendarDTOS, reportdate.plusDays(1))) {
            //节假日前一天
        }

        //正常情况


        return amount;

    }

    // 安全获取BigDecimal值
    private BigDecimal getSafeBigDecimal(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    // 计算衍生字段  字段是Double的时候累计加和
//    private ReportDTO calculateTotal(List<ReportDTO> dto, String reportdate) {
//        // 汇总数据，计算总金额等字段
//        ReportDTO total = new ReportDTO();
//        total.setOperatorNo("");
//        total.setOperatorName("合计");
//
//        total.setHisAdvancePayment(dto.stream().mapToBigDecimal(ReportDTO::getHisAdvancePayment).sum());
//        total.setHisMedicalIncome(dto.stream().mapToBigDecimal(ReportDTO::getHisMedicalIncome).sum());
//        total.setHisRegistrationIncome(dto.stream().mapToBigDecimal(ReportDTO::getHisRegistrationIncome).sum());
//
//
//        total.setRetainedCash(dto.stream().mapToBigDecimal(ReportDTO::getRetainedCash).sum());
//
//        total.setReportAmount(dto.stream().mapToBigDecimal(ReportDTO::getReportAmount).sum());
//        total.setPreviousTemporaryReceipt(dto.stream().mapToBigDecimal(ReportDTO::getPreviousTemporaryReceipt).sum());
//        total.setHolidayTemporaryReceipt(dto.stream().mapToBigDecimal(ReportDTO::getHolidayTemporaryReceipt).sum());
//        total.setActualCashAmount(dto.stream().mapToBigDecimal(ReportDTO::getActualCashAmount).sum());
//        total.setCurrentTemporaryReceipt(dto.stream().mapToBigDecimal(ReportDTO::getCurrentTemporaryReceipt).sum());
//        total.setActualCashAmount(dto.stream().mapToBigDecimal(ReportDTO::getActualCashAmount).sum());
//        total.setRetainedDifference(dto.stream().mapToBigDecimal(ReportDTO::getRetainedDifference).sum());
//        total.setPettyCash(dto.stream().mapToBigDecimal(ReportDTO::getPettyCash).sum());
//
//        total.setRemarks("合计行，不展示在报表中");
//        total.setReportDate(reportdate);
//        total.setCreateTime(LocalDateTime.now());
//
//
//        return total;
//    }


// 假设 ReportDTO 中所有相关金额字段都是 BigDecimal 类型

    private ReportDTO calculateTotal(List<ReportDTO> dtoList, String reportdate) {
        // 1. 定义 BigDecimal 求和初始值 (0) 和操作符 (加法)
        final BigDecimal ZERO = BigDecimal.ZERO;
        java.util.function.BinaryOperator<BigDecimal> sumOperator = BigDecimal::add;

        // 2. 初始化合计对象
        ReportDTO total = new ReportDTO();
        total.setOperatorNo("");
        total.setOperatorName("合计");

        // 3. 定义一个通用的求和方法 (可选，但推荐简化代码)
        // 这是一个接受 List<ReportDTO> 和 Getter 方法引用的泛型函数
        java.util.function.Function<java.util.function.Function<ReportDTO, BigDecimal>, BigDecimal> sumByField =
                getter -> dtoList.stream()
                        .map(getter)
                        .filter(Objects::nonNull) // 过滤掉可能存在的 null 值
                        .reduce(ZERO, sumOperator); // 使用 reduce 进行高精度求和

        // 4. 应用求和逻辑到每个字段
        total.setHisAdvancePayment(sumByField.apply(ReportDTO::getHisAdvancePayment));
        total.setHisMedicalIncome(sumByField.apply(ReportDTO::getHisMedicalIncome));
        total.setHisRegistrationIncome(sumByField.apply(ReportDTO::getHisRegistrationIncome));
        total.setRetainedCash(sumByField.apply(ReportDTO::getRetainedCash));
        total.setReportAmount(sumByField.apply(ReportDTO::getReportAmount));
        total.setPreviousTemporaryReceipt(sumByField.apply(ReportDTO::getPreviousTemporaryReceipt));
        total.setHolidayTemporaryReceipt(sumByField.apply(ReportDTO::getHolidayTemporaryReceipt));

        // 注意：原代码中有重复求和 ActualCashAmount，这里只保留一次
        total.setActualCashAmount(sumByField.apply(ReportDTO::getActualCashAmount));
        total.setCurrentTemporaryReceipt(sumByField.apply(ReportDTO::getCurrentTemporaryReceipt));
        total.setRetainedDifference(sumByField.apply(ReportDTO::getRetainedDifference));
        total.setPettyCash(sumByField.apply(ReportDTO::getPettyCash));

        // 5. 设置其他字段
        total.setRemarks("合计行，不展示在报表中");
        total.setReportDate(reportdate);
        total.setCreateTime(LocalDateTime.now());

        return total;
    }


    /**
     * 最优解：字符串作废标志的HashSet方案
     */
    public boolean isDateInvalid(List<YQHolidayCalendarDTO> YQHolidayCalendarDTOList, LocalDate targetDate) {
        if (YQHolidayCalendarDTOList == null || targetDate == null) {
            return false;
        }

        // 先把所有日期提取到Set中
        Set<LocalDate> dateSet = YQHolidayCalendarDTOList.stream()
                .map(YQHolidayCalendarDTO::getHolidayDate)
                .collect(Collectors.toSet());

        // 然后快速检查
        boolean exists = dateSet.contains(targetDate);

        return exists;
    }


}