//package com.mergedata.server.impl;
//
//import com.mergedata.mapper.CashMapper;
//import com.mergedata.mapper.HolidayMapper;
//import com.mergedata.mapper.OperatorMapper;
//import com.mergedata.mapper.ReportMapper;
//import com.mergedata.model.*;
//import com.mergedata.server.HisDataService;
//import com.mergedata.server.ReportService;
//import com.mergedata.util.PrimaryKeyGenerator;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.BeanUtils;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.math.BigDecimal;
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.util.*;
//import java.util.function.BinaryOperator;
//import java.util.function.Function;
//import java.util.stream.Collectors;
//
//@Service
//@Slf4j
//public class ReportServiceImplBak implements ReportService {
//
//    @Autowired
//    HisDataService hisdata;
//
//    @Autowired
//    CashMapper cash;
//
//    @Autowired
//    OperatorMapper oper;
//
//    @Autowired
//    HolidayMapper hiliday;
//
//    @Autowired
//    ReportMapper report;
//
//
//    List<Report> results = new ArrayList<>();
//
//
//    @Override
//    public List<Report> getAll(String reportdate) {
//        //调用存储过程获取报表数据
//        try {
//             results = report.selectReportByDate(LocalDate.parse(reportdate));
//
//            // 判断结果集，判断是否平台有无数据，有则查询出返回，无则调用接口获取数据并返回
//            if (results.isEmpty()) {
//
//                results = getAllReportData(reportdate);
//
//                //❗查询时候数据库没有相关的数据，插入数据库，此处调用 batchInsert 方法批量插入数据
//                batchInsert(results);
//            }
//        } catch (Exception e) {
//            log.error("获取报表数据异常", e);
//            throw new RuntimeException("获取报表数据异常");
//         }
//        // 添加计算合计数据
//        results.add(calculateTotal(results, reportdate));
//
//        return results;
//    }
//
//    @Override
//    @Transactional
//    public Boolean batchInsert(List<Report> list) {
//        if (list == null || list.isEmpty()) {
//            // 如果列表为空，直接返回
//            return false;
//        }
//        //作废原有
//        for (Report dto : list) {
//            report.updateByPK(dto.getSerialNo());
//        }
//
//        // 生成唯一序列号，此处使用 PrimaryKeyGenerator 类生成主键
//        PrimaryKeyGenerator pks = new PrimaryKeyGenerator();
//        String pk = pks.generateKey();
//        List<CashStattisticsMain> mainList = new ArrayList<>();
//        List<CashStatisticsSub> subList = new ArrayList<>();
//
//        // --- 核心逻辑：遍历 Report 列表，拆分数据 ---
//        for (Report report : list) {
//
//            // ------------------------------------------
//            // 1. 转换为 CashStattisticsMain (主表)
//            // ------------------------------------------
//            CashStattisticsMain main = new CashStattisticsMain();
//
//            // 复制同名字段（如 serialNo, createTime, updateTime, reportYear, reportDate）
//            // 注意：因为 Report.reportDate是String，而 Main.reportDate是LocalDate，BeanUtils不会自动转换，所以需要手动处理
//
//            // 复制同名字段（如 serialNo, reportYear, createTime...）
//            // 警告：如果 Report 和 Main 中字段类型不匹配，BeanUtils会忽略或报错，所以需要手动处理不匹配的字段
//            // 简单起见，这里选择手动映射关键字段:
//
//            main.setSerialNo(pk);
//            main.setIsvalid(true); // 默认有效
//            // 处理 Report.reportDate (String) -> Main.reportDate (LocalDate)
//            if (report.getReportDate() != null) {
//                try {
//                    main.setReportDate(LocalDate.parse(report.getReportDate()));
//                } catch (Exception e) {
//                    // 记录日志或抛出业务异常
//                    throw new RuntimeException("日期格式错误: " + report.getReportDate());
//                }
//            }
//            if (report.getReportYear() != null) {
//                main.setReportYear(Integer.valueOf(report.getReportYear()));
//            }
//
//            // 保持创建和更新时间
//            main.setCreateTime(report.getCreateTime() != null ? report.getCreateTime().toLocalDate() : LocalDate.now());
//            main.setUpdateTime(LocalDate.now());
//
//            mainList.add(main);
//
//            // ------------------------------------------
//            // 2. 转换为 CashStatisticsSub (明细表)
//            // ------------------------------------------
//            CashStatisticsSub sub = new CashStatisticsSub();
//
//            // ⭐ 快捷操作：使用 BeanUtils 复制大部分同名同类型字段
//            // 将 Report 中大量的 his*, amount*, cash* 字段复制到 Sub 对象
//            BeanUtils.copyProperties(report, sub);
//
//            // 关键：手动设置外键和名称不匹配的字段
//            sub.setSerialNo(report.getSerialNo()); // 外键，必须设置
//            sub.setHisOperatorNo(report.getOperatorNo());
//            sub.setHisOperatorName(report.getOperatorName());
//
//            subList.add(sub);
//        }
//
//        // --- 3. 批量插入操作（事务生效） ---
//        int mainCount = report.batchInsertList(mainList);
//
//        int subCount = 0;
//        if (!subList.isEmpty()) {
//            subCount = report.batchInsertSubList(subList);
//        }
//        // 验证插入数量是否一致
//        if (mainCount != subList.size()) {
//            throw new RuntimeException("插入数据不一致");
//        }
//
//        return true;
//    }
//
//
//
//
//    private Report calculateTotal(List<Report> dtoList, String reportdate) {
//        // 1. 定义 BigDecimal 求和初始值 (0) 和操作符 (加法)
//        final BigDecimal ZERO = BigDecimal.ZERO;
//        BinaryOperator<BigDecimal> sumOperator = BigDecimal::add;
//
//        // 2. 初始化合计对象
//        Report total = new Report();
//        total.setOperatorNo("");
//        total.setOperatorName("合计");
//
//        // 3. 定义一个通用的求和方法 (可选，但推荐简化代码)
//        // 这是一个接受 List<ReportDTO> 和 Getter 方法引用的泛型函数
//        Function<Function<Report, BigDecimal>, BigDecimal> sumByField =
//                getter -> dtoList.stream()
//                        .map(getter)
//                        .filter(Objects::nonNull) // 过滤掉可能存在的 null 值
//                        .reduce(ZERO, sumOperator); // 使用 reduce 进行高精度求和
//
//        // 4. 应用求和逻辑到每个字段
//        total.setHisAdvancePayment(sumByField.apply(Report::getHisAdvancePayment));
//        total.setHisMedicalIncome(sumByField.apply(Report::getHisMedicalIncome));
//        total.setHisRegistrationIncome(sumByField.apply(Report::getHisRegistrationIncome));
//        total.setRetainedCash(sumByField.apply(Report::getRetainedCash));
//        total.setReportAmount(sumByField.apply(Report::getReportAmount));
//        total.setPreviousTemporaryReceipt(sumByField.apply(Report::getPreviousTemporaryReceipt));
//        total.setHolidayTemporaryReceipt(sumByField.apply(Report::getHolidayTemporaryReceipt));
//
//        // 注意：原代码中有重复求和 ActualCashAmount，这里只保留一次
//        total.setActualCashAmount(sumByField.apply(Report::getActualCashAmount));
//        total.setCurrentTemporaryReceipt(sumByField.apply(Report::getCurrentTemporaryReceipt));
//        total.setRetainedDifference(sumByField.apply(Report::getRetainedDifference));
//        total.setPettyCash(sumByField.apply(Report::getPettyCash));
//
//        // 5. 设置其他字段
//        total.setRemarks("合计行，不展示在报表中");
//        total.setReportDate(reportdate);
//        total.setCreateTime(LocalDateTime.now());
//
//        return total;
//    }
//
//
//    /**
//     * 1. 从各数据源获取数据。
//     * 2. 以操作员为基准进行匹配和计算。
//     * 3. 对周一进行特殊的回溯计算 (A = B - Sum(C) - D)。
//     *
//     * @param reportdate 目标报表日期 (String yyyy-MM-dd)
//     * @return 包含所有操作员计算结果的 ReportDTO 列表
//     */
//
//    public List<Report> getAllReportData(String reportdate) {
//        try {
//            LocalDate currtDate = LocalDate.parse(reportdate);
//            LocalDate preDate = currtDate.minusDays(1);
//
//            // 1. 获取所有必需的原始数据 (保持不变)
//            List<YQHolidayCalendar> holidays = hiliday.selectAll();
//            List<YQOperator> operators = oper.selectAll();
//            List<YQCashRegRecord> yqRecordList = cash.selectByDate(currtDate);
//            List<HisIncome> hisIncomeList = hisdata.findByDate(reportdate);
//            List<Report> preReport = report.selectReportByDate(preDate);
//
//            // 2. 数据预处理：转换为 Map/Set (保持不变)
//            Map<String, HisIncome> hisDataMap = hisIncomeList.stream()
//                    .collect(Collectors.toMap(HisIncome::getOperatorNo, Function.identity(), (v1, v2) -> v1));
//
//            // 2. 数据预处理：转换为 Map/Set (保持不变)
//            Map<String, YQCashRegRecord> cashMap = yqRecordList.stream()
//                    .collect(Collectors.toMap(YQCashRegRecord::getOperatorNo, Function.identity(), (v1, v2) -> v1));
//
//            // ... (其他 Map 转换不变)
//            Set<LocalDate> holidaySet = holidays.stream()
//                    .map(YQHolidayCalendar::getHolidayDate)
//                    .collect(Collectors.toSet());
//
//            // 3. 构建结果集 (保持不变)
//            List<Report> resultList = new ArrayList<>();
//
//            PrimaryKeyGenerator pks = new PrimaryKeyGenerator();
//            String pk = pks.generateKey();
//
//            Integer count = 0;
//
//            // 4. 以操作员为主，遍历构建报表数据 (保持不变)
//            for (YQOperator operator : operators) {
//                Report currentDto = new Report();
//                count++;
//
//                currentDto.setSerialNo(pk);
//                currentDto.setOperatorNo(operator.getOperatorNo());
//                currentDto.setOperatorName(operator.getOperatorName());
//                currentDto.setReportDate(reportdate);
//
//
//
//                // =========================================================================
//                // 基础信息赋值区域：填充 HIS 收入、报表金额和各项暂收款
//                // =========================================================================
//
//                // 1. 获取当前操作员的 HIS 收入数据 (使用预处理的 Map 查找)
//                HisIncome hisIncome = hisDataMap.get(operator.getOperatorNo());
//
//                // 2. 从昨日数据 (preReport) 查找操作员的记录
//                Report yesterdayReport = preReport.stream()
//                        .filter(r -> operator.getOperatorNo().equals(r.getOperatorNo()))
//                        .findFirst()
//                        .orElse(null);
//
//                // --- 填充 HIS 收入和 ReportAmount ---
//                if (hisIncome != null) {
//                    // 假设 HisIncome 具有 getAdvancePayment(), getMedicalIncome(), getRegistrationIncome() 方法
//                    currentDto.setHisAdvancePayment(getSafeBigDecimal(hisIncome.getHisAdvancePayment()));
//                    currentDto.setHisMedicalIncome(getSafeBigDecimal(hisIncome.getHisMedicalIncome()));
//                    currentDto.setHisRegistrationIncome(BigDecimal.ZERO);
//
//                    // ReportAmount (应交报表数) = HIS 各项收入总和
//                    BigDecimal reportAmount = currentDto.getHisAdvancePayment()
//                            .add(currentDto.getHisMedicalIncome());
//
//                    currentDto.setReportAmount(reportAmount);
//
//                } else {
//                    // 如果没有 HIS 收入数据，所有收入和报表金额都设为零
//                    currentDto.setHisAdvancePayment(BigDecimal.ZERO);
//                    currentDto.setHisMedicalIncome(BigDecimal.ZERO);
//                    currentDto.setHisRegistrationIncome(BigDecimal.ZERO);
//                    currentDto.setReportAmount(BigDecimal.ZERO);
//                }
//
//
//                // --- 填充 昨日留存和暂收款 ---
//                if (yesterdayReport != null) {
//
//                    // PreviousTemporaryReceipt (昨日暂收款) = 昨日的 CurrentTemporaryReceipt
//                    currentDto.setPreviousTemporaryReceipt(getSafeBigDecimal(yesterdayReport.getCurrentTemporaryReceipt()));
//
//                } else {
//                    // 如果没有找到昨日数据，设为零
//                    currentDto.setPreviousTemporaryReceipt(BigDecimal.ZERO);
//                }
//
//
//                YQCashRegRecord  cashRecord = cashMap.get(operator.getOperatorNo());
//                //7.留存现金
//                if (cashRecord != null) {
//                    // RetainedCash (留存现金)
//                    currentDto.setRetainedCash(getSafeBigDecimal(cashRecord.getRetainedCash()));
//                }else {
//
//                    currentDto.setRetainedCash(BigDecimal.ZERO);
//                }
//
//                // =========================================================================
//                // End of 基础信息赋值
//                // =========================================================================
//
//                    /*
//                    根据日期的情况分类得到最后时间，
//                     1、判断当前日期类型  0 正常 ，1 节假日 ，2 节假日前一天 3 节假日后一天
//                     */
//                // ❗当前是工作日 且 前一天是节假日/周末
//                boolean isAfterHoliday = !isHoliday(holidaySet, currtDate) && isHoliday(holidaySet, currtDate.minusDays(1));
//
//                if (isAfterHoliday) {
//                    // 符合条件：执行复杂回溯计算 (A = B - Sum(C) - D)
//                    log.info("第{}条，[{}]节假日后正常工作日第一天特殊处理，回溯计算实际报表金额开始......................",count,currtDate);
//                    calculateActualReportAmountForMonday(
//                            currentDto,
//                            currtDate,
//                            holidaySet,
//                            // 传递查询函数
//                            dateString -> report.selectReportByDate(dateString)
//                    );
//                } else if (isHoliday(holidaySet, currtDate)) {
//                    // 节假日逻辑：A = B - C
//                    BigDecimal actualReportAmount = currentDto.getReportAmount().subtract(currentDto.getHolidayTemporaryReceipt());
//                    currentDto.setActualReportAmount(actualReportAmount);
//
//                } else if (isHoliday(holidaySet, currtDate.plusDays(1))) {
//                    // 节假日前一天 (周五/最后一天) 逻辑
//                    // A = B - D' - E'
//                    BigDecimal actualReportAmount = currentDto.getReportAmount()
//                            .subtract(currentDto.getPreviousTemporaryReceipt())
//                            .subtract(currentDto.getCurrentTemporaryReceipt());
//
//                    currentDto.setActualReportAmount(actualReportAmount);
//
//                } else {
//                    // 正常工作日逻辑
//                    currentDto.setActualReportAmount(currentDto.getReportAmount());
//                }
//
//
//                // 5.实收现金数 5 = 3+4
//                currentDto.setActualCashAmount(currentDto.getActualReportAmount().add(currentDto.getCurrentTemporaryReceipt()));
//
//                //留存数差额 6 = 7-3-8
//                currentDto.setRetainedDifference(currentDto.getRetainedCash().
//                        subtract(currentDto.getPettyCash()).
//                        add(currentDto.getActualReportAmount()));
//
//                // 7. 加入结果集 (保持不变)
//                resultList.add(currentDto);
//            }
//
//            log.info("{}生成报表完成，共处理 {} 个操作员", reportdate, resultList.size());
//            return resultList;
//
//        } catch (Exception e) {
//            log.error("报表生成失败", e);
//            return Collections.emptyList();
//        }
//    }
//
//    /**
//     * 【核心逻辑实现】处理周一的复杂回溯计算
//     * 公式: A = B - Sum(C){周末/节假日} - D{周五}
//     * C = HolidayTemporaryReceipt (节假日暂收款), D = CurrentTemporaryReceipt (当日暂收款)
//     */
//    private void calculateActualReportAmountForMonday(
//            Report currentDto,
//            LocalDate targetDate,
//            Set<LocalDate> holidaySet,
//            Function<String, List<Report>> reportQueryFunction) {
//
//        BigDecimal totalC = BigDecimal.ZERO;
//        BigDecimal dAmountFriday = BigDecimal.ZERO;
//        LocalDate currentDate = targetDate.minusDays(1); // 从周日开始回溯
//
//        // 2. 回溯循环
//        while (true) {
//
//            // 2.1 获取当前回溯日期的历史报表数据 (通过 Mapper)
//            List<Report> historicalReports = reportQueryFunction.apply(currentDate);
//
//            // 2.2 查找当前操作员在历史报表中的记录 (确保用户匹配)
//            Optional<Report> historicalDtoOpt = historicalReports.stream()
//                    .filter(r -> currentDto.getOperatorNo().equals(r.getOperatorNo()))
//                    .findFirst();
//
//            // 提取当前的 金额 (HolidayTemporaryReceipt)
//            //{实交报表数}(3)={应交报表数}（1）- {周五，周六，周末}sum(x) - {周五：当日暂收款}(4)
//            // 无论是否为假日，这个金额都会被提取，以便在周五时累加
//            BigDecimal cAmount = historicalDtoOpt
//                    .map(Report::getHolidayTemporaryReceipt)
//                    .orElse(BigDecimal.ZERO); // 缺失数据默认为 0
//
//            if (isHoliday(holidaySet, currentDate)) {
//                // 是节假日（周六、周日）：累加 C 金额 (HolidayTemporaryReceipt)
//
//                totalC = totalC.add(getSafeBigDecimal(cAmount));
//
//                currentDate = currentDate.minusDays(1); // 继续往前
//
//            } else {
//                // 找到中断点：第一个非节假日日期（即周五）
//                // ❗ 将周五的 C (HolidayTemporaryReceipt) 也累加进去
//                totalC = totalC.add(getSafeBigDecimal(cAmount));
//
//                // 提取 D 金额 (CurrentTemporaryReceipt)
//                dAmountFriday = historicalDtoOpt
//                        .map(Report::getCurrentTemporaryReceipt)
//                        .orElse(BigDecimal.ZERO); // 缺失数据默认为 0
//                break; // 跳出循环
//            }
//
//            // 安全检查：防止无限循环
//            if (targetDate.toEpochDay() - currentDate.toEpochDay() > 15) {
//                log.warn("回溯查找失败，连续节假日过多，在 {} 无法找到工作日。", targetDate);
//                break;
//            }
//        }
//
//        // 3. 应用公式：A = B - Sum(C) - D
//        BigDecimal finalActualReportAmount = currentDto.getReportAmount()
//                .subtract(totalC)
//                .subtract(dAmountFriday);
//        log.info("员工ID:{}，姓名:{},回溯计算实际报表金额 {} - {} - {} = {} ",
//                currentDto.getOperatorNo(),currentDto.getOperatorName(),currentDto.getReportAmount(),totalC,dAmountFriday,finalActualReportAmount);
//            // 4. 设置计算结果
//        currentDto.setActualReportAmount(finalActualReportAmount);
//    }
//
//
//    /**
//     * 判断日期是否为节假日 (使用 Set 版本)
//     */
//    private boolean isHoliday(Set<LocalDate> holidaySet, LocalDate targetDate) {
//        if (holidaySet == null || targetDate == null) {
//            return false;
//        }
//        return holidaySet.contains(targetDate);
//    }
//
//    /**
//     * 安全获取 BigDecimal 值，如果为 null 则返回 BigDecimal.ZERO
//     */
//    private BigDecimal getSafeBigDecimal(BigDecimal value) {
//        return value != null ? value : BigDecimal.ZERO;
//
//    }
//
//
//}