package com.mergedata.server.impl;

import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.mergedata.model.dto.external.HisIncomeResponseDTO;
import com.mergedata.model.dto.ReportDTO;
import com.mergedata.model.dto.ReportRequestBody;
import com.mergedata.mapper.CashMapper;
import com.mergedata.mapper.HolidayMapper;
import com.mergedata.mapper.OperatorMapper;
import com.mergedata.mapper.ReportMapper;
import com.mergedata.model.entity.*;
import com.mergedata.server.HisDataService;
import com.mergedata.server.ReportService;
import com.mergedata.util.PrimaryKeyGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ReportServiceImpl implements ReportService {

    @Autowired
    HisDataService hisdata;

    @Autowired
    CashMapper cash;

    @Autowired
    OperatorMapper oper;

    @Autowired
    HolidayMapper hiliday;

    @Autowired
    ReportMapper report;


    List<ReportDTO> results = new ArrayList<>();


    @Override
    public List<ReportDTO> getAll(String reportDate) {
        return Collections.emptyList();
    }

    @Override
    public List<ReportDTO> getAll(ReportRequestBody body) {
        //调用存储过程获取报表数据
        try {
//            results = report.selectReportByDate(body.getReportDate());
            results = report.findReport(body);

            //接收ExtendParams1为true时，即初始化报表
            String param1 = body.getExtendParams1();
            Boolean isInitFlag = (param1 != null && "true".equalsIgnoreCase(param1));

            // 判断结果集，判断是否平台有无数据，有则查询出返回，无则调用接口获取数据并返回
            if (results.isEmpty() || isInitFlag ) {

                // 调用 getAllReportData(LocalDate)
                results = getAllReportData(body);

                //查询时候数据库没有相关的数据，插入数据库，此处调用 firstInsert 方法批量插入数据
                isInitInsert(results,body.getReportDate());
            }
        } catch (Exception e) {
            log.error("获取报表数据异常", e);
            throw new RuntimeException("获取报表数据异常");
        }

        // 添加计算合计数据
        //屏蔽计算合计金额
//        results.add(calculateTotal(results, LocalDate.parse(reportdate)));



        // 进行筛选
        return results.stream()
                .filter(r -> (body.getInpWindow() == null || body.getInpWindow() != 1 || Integer.valueOf(1).equals(r.getInpWindow())))
                .filter(r -> (body.getAtm() == null || body.getAtm() != 1 || Integer.valueOf(1).equals(r.getAtm())))
                .collect(Collectors.toList());
//        return results;
    }

    /*
    对应日期报表无数据时候，是否初始化写入数据
     */
    @Transactional(rollbackFor = Exception.class) // 保证主从要么全成功，要么全失败
    public void isInitInsert(List<ReportDTO> list, LocalDate date) {
        // =======================================================
        // 只生成一个主键，作为主表的主键和明细表的外键
        // =======================================================
        PrimaryKeyGenerator pks = new PrimaryKeyGenerator();
        String pk = pks.generateKey();

        List<CashStatisticsSubEntity> subList = new ArrayList<>();

        // ------------------------------------------
        //  只创建 1 条 CashStattisticsMain (主表) 记录
        // ------------------------------------------
        CashStattisticsMainEntity main = new CashStattisticsMainEntity();

        main.setSerialNo(pk); // 唯一主键
        main.setIsvalid(true);
        main.setReportDate(date);
        main.setReportYear(date.getYear());
        main.setCreateTime(LocalDateTime.now());


        // ------------------------------------------
        // 遍历 Report 列表，创建 N 条 CashStatisticsSub (明细表) 记录
        // ------------------------------------------
        for (ReportDTO reportDTO : list) {
            CashStatisticsSubEntity sub = new CashStatisticsSubEntity();

            BeanUtils.copyProperties(reportDTO, sub);

            // 所有明细记录使用同一个主键作为外键
            sub.setSerialNo(pk);
            sub.setHisOperatorNo(reportDTO.getOperatorNo());
            sub.setHisOperatorName(reportDTO.getOperatorName());

            //添加 结账序号 结账时间  2025.12.31
            sub.setRowNum(reportDTO.getRowNum());
            sub.setAcctDate(reportDTO.getAcctDate());
            sub.setAcctNo(reportDTO.getAcctNo());

            if (!reportDTO.getOperatorName().contains("合计")) {
                subList.add(sub);
            }
        }


        // ================== 核心写入逻辑 ==================

        // 2. 写入主表 (this 指向当前主表 Service)
        Db.save(main);

        // 3. 批量写入子表 (调用 subService)
        if (!subList.isEmpty()) {
            Db.saveBatch(subList);
        }

    }

    @Override
    @Transactional
    public Boolean batchInsert(List<ReportDTO> reportDTOList) {
        if (reportDTOList == null || reportDTOList.isEmpty()) {
            return false;
        }

        // 1. 确定要作废的日期
        LocalDate reportDate= reportDTOList.get(0).getReportDate();

        // 2. 作废该日期下所有的主表记录
        log.info("开始作废日期 {} 下的历史报表数据...", reportDate);
        if(!report.selectReportByDate(reportDate).isEmpty()) {
            report.updateByDate(reportDate);
        }
        log.info("历史报表数据作废完成.");


        //3.转换对应报表数据
        List<ReportDTO> list = exchangeInsertReportData(reportDate, reportDTOList);


        // =======================================================
        // 只生成一个主键，作为主表的主键和明细表的外键
        // =======================================================
        PrimaryKeyGenerator pks = new PrimaryKeyGenerator();
        String pk = pks.generateKey();

        List<CashStattisticsMainEntity> mainList = new ArrayList<>();
        List<CashStatisticsSubEntity> subList = new ArrayList<>();

        // ------------------------------------------
        //  只创建 1 条 CashStattisticsMain (主表) 记录
        // ------------------------------------------
        CashStattisticsMainEntity main = new CashStattisticsMainEntity();
        ReportDTO firstReportDTO = list.get(0); // 随便取一条 Report 来获取通用的主表信息

        main.setSerialNo(pk); // 唯一主键
        main.setIsvalid(true);

        // 复制通用的日期/年份信息
        if (firstReportDTO.getReportDate() != null) {
            try {
                main.setReportDate(firstReportDTO.getReportDate());
            } catch (Exception e) {
                throw new RuntimeException("日期格式错误: " + firstReportDTO.getReportDate());
            }
        }
        if (firstReportDTO.getReportYear() != null) {
            main.setReportYear(Integer.valueOf(firstReportDTO.getReportYear()));
        }


        main.setCreateTime(firstReportDTO.getCreateTime() != null ? firstReportDTO.getCreateTime() : LocalDateTime.now());
        main.setUpdateTime(LocalDateTime.now());

        mainList.add(main); // 主表列表中只有 1 条记录


        // ------------------------------------------
        // 遍历 Report 列表，创建 N 条 CashStatisticsSub (明细表) 记录
        // ------------------------------------------
        for (ReportDTO reportDTO : list) {
            CashStatisticsSubEntity sub = new CashStatisticsSubEntity();

            BeanUtils.copyProperties(reportDTO, sub);

            // 所有明细记录使用同一个主键作为外键
            sub.setSerialNo(pk);
            sub.setHisOperatorNo(reportDTO.getOperatorNo());
            sub.setHisOperatorName(reportDTO.getOperatorName());

            //添加 结账序号 结账时间  2025.12.31
            sub.setRowNum(reportDTO.getRowNum());
            sub.setAcctDate(reportDTO.getAcctDate());
            sub.setAcctNo(reportDTO.getAcctNo());

            if (!reportDTO.getOperatorName().contains("合计")) {
                subList.add(sub);
            }
        }

        // --- 3. 批量插入操作（事务生效） ---
        // 此时 mainList 只有 1 条记录
        int mainCount = report.batchInsertList(mainList);

        int subCount = 0;
        if (!subList.isEmpty()) {
            // subList 有 N 条记录（例如 45 条）
            subCount = report.batchInsertSubList(subList);
        }

//        // 验证插入数量：主表插入 1 条，明细表插入 list.size() 条
//        if (mainCount != 1 || subCount != list.size()) {
//            log.error("插入数据数量不一致，主表插入：{}，明细表插入：{}", mainCount, subCount);
//            throw new RuntimeException("插入数据不一致");
//        }

        return true;
    }


    private ReportDTO calculateTotal(List<ReportDTO> dtoList, LocalDate reportdate) {
        // ... (方法内部逻辑不变)
        final BigDecimal ZERO = BigDecimal.ZERO;
        BinaryOperator<BigDecimal> sumOperator = BigDecimal::add;

        ReportDTO total = new ReportDTO();
        total.setOperatorNo("sum_total");
        total.setOperatorName("合计");

        Function<Function<ReportDTO, BigDecimal>, BigDecimal> sumByField =
                getter -> dtoList.stream()
                        .map(getter)
                        .filter(Objects::nonNull)
                        .reduce(ZERO, sumOperator);

        total.setHisAdvancePayment(sumByField.apply(ReportDTO::getHisAdvancePayment));
        total.setHisMedicalIncome(sumByField.apply(ReportDTO::getHisMedicalIncome));
        total.setHisRegistrationIncome(sumByField.apply(ReportDTO::getHisRegistrationIncome));
        total.setRetainedCash(sumByField.apply(ReportDTO::getRetainedCash));
        total.setReportAmount(sumByField.apply(ReportDTO::getReportAmount));
        total.setPreviousTemporaryReceipt(sumByField.apply(ReportDTO::getPreviousTemporaryReceipt));
        total.setHolidayTemporaryReceipt(sumByField.apply(ReportDTO::getHolidayTemporaryReceipt));

        total.setActualCashAmount(sumByField.apply(ReportDTO::getActualCashAmount));
        total.setCurrentTemporaryReceipt(sumByField.apply(ReportDTO::getCurrentTemporaryReceipt));
        total.setRetainedDifference(sumByField.apply(ReportDTO::getRetainedDifference));
        total.setPettyCash(sumByField.apply(ReportDTO::getPettyCash));

        total.setRemarks("合计行，不展示在报表中");
        total.setReportDate(reportdate);
        total.setCreateTime(LocalDateTime.now());

        return total;
    }

    /*
    转换对应存储的报表数据
     */
    public List<ReportDTO> exchangeInsertReportData(LocalDate currtDate, List<ReportDTO> list) {
        try {
            // 1. 获取所有必需的原始数据
            List<YQHolidayCalendarEntity> holidays = hiliday.selectAll();

            Set<LocalDate> holidaySet = holidays.stream()
                    .map(YQHolidayCalendarEntity::getHolidayDate)
                    .collect(Collectors.toSet());

            List<YQCashRegRecordEntity> yqRecordList = cash.selectByDate(currtDate);
            Map<String, YQCashRegRecordEntity> cashMap = yqRecordList.stream()
                    .collect(Collectors.toMap(YQCashRegRecordEntity::getOperatorNo, Function.identity(), (v1, v2) -> v1));



            // 3. 构建结果集
            List<ReportDTO> resultList = new ArrayList<>();

            PrimaryKeyGenerator pks = new PrimaryKeyGenerator();
            String pk = pks.generateKey();

            Integer count = 0;

            // 4. 以操作员为主，遍历构建报表数据
            for (ReportDTO rpt : list) {
                ReportDTO currentDto = new ReportDTO();
                count++;

                currentDto.setSerialNo(pk);
                currentDto.setOperatorNo(rpt.getOperatorNo());
                currentDto.setOperatorName(rpt.getOperatorName());
                currentDto.setRowNum(rpt.getRowNum());
                /*
                添加结账序号  结账时间 等 2025-13-31
                 */
                currentDto.setAcctDate(rpt.getAcctDate());
                currentDto.setAcctNo(rpt.getAcctNo());
                currentDto.setAtm(rpt.getAtm());
                currentDto.setInpWindow(rpt.getInpWindow());

                // Report 对象的 reportDate 属性是 String，需要转换
                currentDto.setReportDate(currtDate);


                // =========================================================================
                // 基础信息赋值区域
                // =========================================================================


                // --- 填充 HIS 收入和 ReportAmount (保持不变) ---
                currentDto.setHisAdvancePayment(getSafeBigDecimal(rpt.getHisAdvancePayment()));
                currentDto.setHisMedicalIncome(getSafeBigDecimal(rpt.getHisMedicalIncome()));
                currentDto.setHisRegistrationIncome(getSafeBigDecimal(rpt.getHisRegistrationIncome()));

                BigDecimal reportAmount = rpt.getHisAdvancePayment()
                        .add(rpt.getHisMedicalIncome()).add(rpt.getHisRegistrationIncome());
                //应交报表数
                currentDto.setReportAmount(reportAmount);

                // 昨日暂收款 ---
                currentDto.setPreviousTemporaryReceipt(getSafeBigDecimal(rpt.getPreviousTemporaryReceipt()));

                //节假日暂收款
                currentDto.setHolidayTemporaryReceipt(getSafeBigDecimal(rpt.getHolidayTemporaryReceipt()));

                //当日暂收款
                currentDto.setCurrentTemporaryReceipt(getSafeBigDecimal(rpt.getCurrentTemporaryReceipt()));
                //备用金
                currentDto.setPettyCash(getSafeBigDecimal(rpt.getPettyCash()));

                YQCashRegRecordEntity cashRecord = cashMap.get(rpt.getOperatorNo());
                //7.留存现金
                if (cashRecord != null) {
                    currentDto.setRetainedCash(getSafeBigDecimal(cashRecord.getRetainedCash()));
                } else {
                    currentDto.setRetainedCash(BigDecimal.ZERO);
                }

                // =========================================================================
                // =========================================================================
                // End of 基础信息赋值
                // =========================================================================




                // ❗当前是工作日 且 前一天是节假日/周末
                boolean isAfterHoliday = !isHoliday(holidaySet, currtDate) && isHoliday(holidaySet, currtDate.minusDays(1));

                if (isAfterHoliday) {
                    // 符合条件：执行复杂回溯计算 (A = B - Sum(C) - D)
                    log.info("第{}条，[{}]节假日后正常工作日第一天特殊处理，回溯计算实际报表金额开始......................", count, currtDate);
                    calculateActualReportAmountForMonday(
                            currentDto,
                            currtDate,
                            holidaySet,
                            // 传递的 Lambda 表达式现在接收 LocalDate
                            date -> report.selectReportByDate(date)
                    );
                } else if (isHoliday(holidaySet, currtDate)) {
                    // 节假日逻辑：A = B - C (保持不变)
                    BigDecimal actualReportAmount = currentDto.getReportAmount().subtract(currentDto.getHolidayTemporaryReceipt());
                    currentDto.setActualReportAmount(actualReportAmount);


                } else if (isHoliday(holidaySet, currtDate.plusDays(1))) {
                    // 节假日前一天 (周五/最后一天) 逻辑 (保持不变)
                    BigDecimal actualReportAmount = currentDto.getReportAmount()
                            .subtract(currentDto.getPreviousTemporaryReceipt())
                            .subtract(currentDto.getCurrentTemporaryReceipt());

                    currentDto.setActualReportAmount(actualReportAmount);

                } else {
                    // 正常工作日逻辑 (保持不变)
                    currentDto.setActualReportAmount(currentDto.getReportAmount());
                }

                    // 5.实收现金数 5 = 3+4 (保持不变)
                currentDto.setActualCashAmount(currentDto.getActualReportAmount().add(currentDto.getCurrentTemporaryReceipt()));

                //留存数差额 6 = 7-3-8 (保持不变)
                currentDto.setRetainedDifference(currentDto.getRetainedCash().
                        subtract(currentDto.getPettyCash()).
                        subtract(currentDto.getActualReportAmount()));

                // 7. 加入结果集 (保持不变)
                resultList.add(currentDto);
            }

            log.info("{}生成报表完成，共处理 {} 个操作员", currtDate.toString(), resultList.size());
            return resultList;

        } catch (Exception e) {
            log.error("报表生成失败", e);
            return Collections.emptyList();
        }
    }

    /**
     * 1. 从各数据源获取数据。
     * 2. 以操作员为基准进行匹配和计算。
     * 3. 对周一进行特殊的回溯计算 (A = B - Sum(C) - D)。
     *
     * @param body 目标报表日期 (LocalDate)
     * @return 包含所有操作员计算结果的 ReportDTO 列表
     */
    public List<ReportDTO> getAllReportData(ReportRequestBody body ) {
        LocalDate currtDate = body.getReportDate();
        try {
            LocalDate preDate = currtDate.minusDays(1);

            // 1. 获取所有必需的原始数据
            List<YQHolidayCalendarEntity> holidays = hiliday.selectAll();
            List<YQOperatorEntity> operators = oper.selectAll();

            List<YQCashRegRecordEntity> yqRecordList = cash.selectByDate(currtDate);

            // 假设 HIS 接口需要 String，则转换
            List<HisIncomeResponseDTO> hisIncomeResponseDTOList = hisdata.findByDateOutp(currtDate.toString());

            // Mapper 调用传入 LocalDate
            List<ReportDTO> preReportDTO = report.selectReportByDate(preDate);

            // 2. 数据预处理：转换为 Map/Set (保持不变)
            Map<String, HisIncomeResponseDTO> hisDataMap = hisIncomeResponseDTOList.stream()
                    .collect(Collectors.toMap(HisIncomeResponseDTO::getOperatorNo, Function.identity(), (v1, v2) -> v1));

            Map<String, YQCashRegRecordEntity> cashMap = yqRecordList.stream()
                    .collect(Collectors.toMap(YQCashRegRecordEntity::getOperatorNo, Function.identity(), (v1, v2) -> v1));

            Set<LocalDate> holidaySet = holidays.stream()
                    .map(YQHolidayCalendarEntity::getHolidayDate)
                    .collect(Collectors.toSet());

            // 3. 构建结果集
            List<ReportDTO> resultList = new ArrayList<>();

            PrimaryKeyGenerator pks = new PrimaryKeyGenerator();
            String pk = pks.generateKey();

            Integer count = 0;

            // 4. 以操作员为主，遍历构建报表数据
            for (YQOperatorEntity operator : operators) {
                ReportDTO currentDto = new ReportDTO();
                count++;

                currentDto.setSerialNo(pk);
                currentDto.setOperatorNo(operator.getOperatorNo());
                currentDto.setOperatorName(operator.getOperatorName());
                /*
                添加 人员标识 set值等 2025.12.31
                 */
                currentDto.setRowNum(operator.getRowNum());
                currentDto.setInpWindow(Boolean.TRUE.equals(operator.getInpWindow()) ? 1 : 0);
                currentDto.setAtm(Boolean.TRUE.equals(operator.getAtm()) ? 1 : 0);

                // Report 对象的 reportDate 属性是 String，需要转换
                currentDto.setReportDate(currtDate);


                // =========================================================================
                // 基础信息赋值区域
                // =========================================================================

                // 1. 获取当前操作员的 HIS 收入数据 (保持不变)
                HisIncomeResponseDTO hisIncomeResponseDTO = hisDataMap.get(operator.getOperatorNo());

                // 2. 从昨日数据 (preReport) 查找操作员的记录 (保持不变)
                ReportDTO yesterdayReportDTO = preReportDTO.stream()
                        .filter(r -> operator.getOperatorNo().equals(r.getOperatorNo()))
                        .findFirst()
                        .orElse(null);

                // --- 填充 HIS 收入和 ReportAmount (保持不变) ---
                if (hisIncomeResponseDTO != null) {
                    /*
                    添加结账序号 结账时间 2025.12.31
                     */
                    currentDto.setAcctNo(hisIncomeResponseDTO.getAcctNo());
                    currentDto.setAcctDate(hisIncomeResponseDTO.getAcctDate());

                    currentDto.setHisAdvancePayment(getSafeBigDecimal(hisIncomeResponseDTO.getHisAdvancePayment()));
                    currentDto.setHisMedicalIncome(getSafeBigDecimal(hisIncomeResponseDTO.getHisMedicalIncome()));
                    currentDto.setHisRegistrationIncome(BigDecimal.ZERO);

                    BigDecimal reportAmount = currentDto.getHisAdvancePayment()
                            .add(currentDto.getHisMedicalIncome());

                    currentDto.setReportAmount(reportAmount);

                } else {
                    currentDto.setHisAdvancePayment(BigDecimal.ZERO);
                    currentDto.setHisMedicalIncome(BigDecimal.ZERO);
                    currentDto.setHisRegistrationIncome(BigDecimal.ZERO);
                    currentDto.setReportAmount(BigDecimal.ZERO);
                }


                // --- 填充 昨日留存和暂收款 (保持不变) ---
                if (yesterdayReportDTO != null) {

                    currentDto.setPreviousTemporaryReceipt(getSafeBigDecimal(yesterdayReportDTO.getCurrentTemporaryReceipt()));

                } else {
                    currentDto.setPreviousTemporaryReceipt(BigDecimal.ZERO);
                }


                YQCashRegRecordEntity cashRecord = cashMap.get(operator.getOperatorNo());
                //7.留存现金 (保持不变)
                if (cashRecord != null) {
                    currentDto.setRetainedCash(getSafeBigDecimal(cashRecord.getRetainedCash()));
                } else {
                    currentDto.setRetainedCash(BigDecimal.ZERO);
                }

                // =========================================================================
                // End of 基础信息赋值
                // =========================================================================


                // ❗当前是工作日 且 前一天是节假日/周末
                boolean isAfterHoliday = !isHoliday(holidaySet, currtDate) && isHoliday(holidaySet, currtDate.minusDays(1));

                /*
                *回溯计算实收报表
                 */
                if (isAfterHoliday) {
                    // 符合条件：执行复杂回溯计算 (A = B - Sum(C) - D)
                    log.info("第{}条，[{}]节假日后正常工作日第一天特殊处理，回溯计算实际报表金额开始......................", count, currtDate);
                    calculateActualReportAmountForMonday(
                            currentDto,
                            currtDate,
                            holidaySet,
                            // 传递的 Lambda 表达式现在接收 LocalDate
                            date -> report.selectReportByDate(date)
                    );
                } else if (isHoliday(holidaySet, currtDate)) {  //是节假日且是当月的最后一天是否，实收报表是否 需要回溯计算
                    // 节假日逻辑：A = B - C (保持不变)
                    BigDecimal actualReportAmount = currentDto.getReportAmount().subtract(currentDto.getHolidayTemporaryReceipt());
                    currentDto.setActualReportAmount(actualReportAmount);

                } else if (isHoliday(holidaySet, currtDate.plusDays(1))) {
                    // 节假日前一天 (周五/最后一天) 逻辑 (保持不变)
                    BigDecimal actualReportAmount = currentDto.getReportAmount()
                            .subtract(currentDto.getPreviousTemporaryReceipt())
                            .subtract(currentDto.getCurrentTemporaryReceipt());

                    currentDto.setActualReportAmount(actualReportAmount);

                } else {
                    // 正常工作日逻辑 (保持不变)
                    currentDto.setActualReportAmount(currentDto.getReportAmount());
                }

                /*
                 *回溯计算节假日暂收款问题 遇到回溯如果跨月问题 只能加到一号,周一的情况
                 */
                if (isAfterHoliday) {
                    // 符合条件：执行复杂回溯计算 (A = B - Sum(C) - D)
                    log.info("第{}条，[{}]节假日后正常工作日第一天特殊处理，回溯计算节假日暂收款金额开始......................", count, currtDate);
                    calculateAHolidayTemporaryReceiptForMonday(
                            currentDto,
                            currtDate,
                            holidaySet,
                            // 传递的 Lambda 表达式现在接收 LocalDate
                            date -> report.selectReportByDate(date)
                    );
                } else if (isHoliday(holidaySet, currtDate)) {
                    // 节假日逻辑且是当月最后一天
                    if(currtDate.getDayOfMonth() == currtDate.lengthOfMonth()){
                        calculateAHolidayTemporaryReceiptForMonday(
                                currentDto,
                                currtDate,
                                holidaySet,
                                // 传递的 Lambda 表达式现在接收 LocalDate
                                date -> report.selectReportByDate(date)
                        );
                    }
                } else {
                    // 正常工作日逻辑 (保持不变)
                    currentDto.setHolidayTemporaryReceipt(BigDecimal.ZERO);
                }



                // 5.实收现金数 5 = 3+4 (保持不变)
                currentDto.setActualCashAmount(currentDto.getActualReportAmount().add(currentDto.getCurrentTemporaryReceipt()));

                //留存数差额 6 = 7-3-8 (保持不变)
                currentDto.setRetainedDifference(currentDto.getRetainedCash().
                        subtract(currentDto.getPettyCash()).
                        add(currentDto.getActualReportAmount()));

                // 7. 加入结果集 (保持不变)
                resultList.add(currentDto);
            }



//                /**
//                 * 判断是否是当月的第一天
//                 */
//                public static boolean isFirstDayOfMonth(LocalDate date) {
//                    return date.getDayOfMonth() == 1;
//                }
//
//                /**
//                 * 判断是否是当月的最后一天
//                 */
//                public static boolean isLastDayOfMonth(LocalDate date) {
//                    return date.getDayOfMonth() == date.lengthOfMonth();
//                }




            log.info("{}生成报表完成，共处理 {} 个操作员", currtDate.toString(), resultList.size());
            return resultList;

        } catch (Exception e) {
            log.error("报表生成失败", e);
            return Collections.emptyList();
        }
    }

    /**
     * 【核心逻辑实现】处理周一的复杂回溯计算
     * 公式: A = B - Sum(C){周末/节假日} - D{周五}
     * C = HolidayTemporaryReceipt (节假日暂收款), D = CurrentTemporaryReceipt (当日暂收款)
     */
    private void calculateActualReportAmountForMonday(
            ReportDTO currentDto,
            LocalDate targetDate,
            Set<LocalDate> holidaySet,
            // ❗ 修正点 11: 将 Function 的输入类型改为 LocalDate
            Function<LocalDate, List<ReportDTO>> reportQueryFunction) {

        BigDecimal totalC = BigDecimal.ZERO;
        BigDecimal dAmountFriday = BigDecimal.ZERO;
        LocalDate currentDate = targetDate.minusDays(1); // 从周日开始回溯


        //当前传入日期为当月第一天
        if(targetDate.getDayOfMonth() != 1) {

            // 2. 回溯循环
            while (true) {

                // 2.1 获取当前回溯日期的历史报表数据 (通过 Mapper)
                // ❗ 修正点 12: 调用函数时，直接传入 LocalDate 对象
                List<ReportDTO> historicalReportDTOS = reportQueryFunction.apply(currentDate);

                // 2.2 查找当前操作员在历史报表中的记录 (确保用户匹配)
                Optional<ReportDTO> historicalDtoOpt = historicalReportDTOS.stream()
                        .filter(r -> currentDto.getOperatorNo().equals(r.getOperatorNo()))
                        .findFirst();

                // 提取当前的节假日的金额 (HolidayTemporaryReceipt)
                BigDecimal cAmount = historicalDtoOpt
                        .map(ReportDTO::getHolidayTemporaryReceipt)
                        .orElse(BigDecimal.ZERO); // 缺失数据默认为 0


                if (isHoliday(holidaySet, currentDate)) {
                    // 是节假日（周六、周日）：累加 C 金额 (HolidayTemporaryReceipt)

                    //当前日期为当月第一天
                    if (currentDate.getDayOfMonth() == 1) {
                        break;
                    }

                    totalC = totalC.add(getSafeBigDecimal(cAmount));

                    currentDate = currentDate.minusDays(1); // 继续往前

                } else {
                    // 找到中断点：第一个非节假日日期（即周五）
                    // ❗ 将周五的 C (HolidayTemporaryReceipt) 也累加进去
                    totalC = totalC.add(getSafeBigDecimal(cAmount));

                    // 提取 D 金额 (CurrentTemporaryReceipt)
                    dAmountFriday = historicalDtoOpt
                            .map(ReportDTO::getCurrentTemporaryReceipt)
                            .orElse(BigDecimal.ZERO); // 缺失数据默认为 0
                    break; // 跳出循环
                }

                // 安全检查：防止无限循环
                if (targetDate.toEpochDay() - currentDate.toEpochDay() > 15) {
                    log.warn("回溯查找失败，连续节假日过多，在 {} 无法找到工作日。", targetDate);
                    break;
                }
            }
        }

        // 3. 应用公式：A = B - Sum(C) - D
        BigDecimal finalActualReportAmount = currentDto.getReportAmount()
                .subtract(totalC)
                .subtract(dAmountFriday);
        log.info("员工ID:{}，姓名:{},回溯计算实际报表金额 {} - {} - {} = {} ",
                currentDto.getOperatorNo(), currentDto.getOperatorName(), currentDto.getReportAmount(), totalC, dAmountFriday, finalActualReportAmount);


        // 4. 设置计算结果
        currentDto.setActualReportAmount(finalActualReportAmount);

    }





    /**
     * 【核心逻辑实现】处理周一的复杂回溯计算
     * 公式: A = B - Sum(C){周末/节假日} - D{周五}
     * C = HolidayTemporaryReceipt (节假日暂收款), D = CurrentTemporaryReceipt (当日暂收款)
     */
    private void calculateAHolidayTemporaryReceiptForMonday(
            ReportDTO currentDto,
            LocalDate targetDate,
            Set<LocalDate> holidaySet,
            // ❗ 修正点 11: 将 Function 的输入类型改为 LocalDate
            Function<LocalDate, List<ReportDTO>> reportQueryFunction) {

        BigDecimal totalC = BigDecimal.ZERO;
        BigDecimal dAmountFriday = BigDecimal.ZERO;
        LocalDate currentDate = targetDate.minusDays(1); // 从周日开始回溯


        //当前传入日期为当月第一天
        if(targetDate.getDayOfMonth() != 1) {

            // 2. 回溯循环
            while (true) {

                // 2.1 获取当前回溯日期的历史报表数据 (通过 Mapper)
                // ❗ 修正点 12: 调用函数时，直接传入 LocalDate 对象
                List<ReportDTO> historicalReportDTOS = reportQueryFunction.apply(currentDate);

                // 2.2 查找当前操作员在历史报表中的记录 (确保用户匹配)
                Optional<ReportDTO> historicalDtoOpt = historicalReportDTOS.stream()
                        .filter(r -> currentDto.getOperatorNo().equals(r.getOperatorNo()))
                        .findFirst();

                // 提取当前的节假日的金额 (HolidayTemporaryReceipt)
                BigDecimal cAmount = historicalDtoOpt
                        .map(ReportDTO::getHolidayTemporaryReceipt)
                        .orElse(BigDecimal.ZERO); // 缺失数据默认为 0


                if (isHoliday(holidaySet, currentDate)) {
                    // 是节假日（周六、周日）：累加 C 金额 (HolidayTemporaryReceipt)

                    //当前日期为当月第一天
                    if (currentDate.getDayOfMonth() == 1) {
                        break;
                    }

                    totalC = totalC.add(getSafeBigDecimal(cAmount));

                    currentDate = currentDate.minusDays(1); // 继续往前

                } else {
                    // 找到中断点：第一个非节假日日期（即周五）
                    // ❗ 将周五的 C (HolidayTemporaryReceipt) 也累加进去
                    totalC = totalC.add(getSafeBigDecimal(cAmount));
                    break; // 跳出循环
                }

                // 安全检查：防止无限循环
                if (targetDate.toEpochDay() - currentDate.toEpochDay() > 15) {
                    log.warn("回溯查找失败，连续节假日过多，在 {} 无法找到工作日。", targetDate);
                    break;
                }
            }
        }


        log.info("员工ID:{}，姓名:{},回溯计算节假日站收款金额 {} - {} - {} = {} ",
                currentDto.getOperatorNo(), currentDto.getOperatorName(), currentDto.getReportAmount(), totalC, dAmountFriday, totalC);

        //节假日站收款
        currentDto.setHolidayTemporaryReceipt(totalC);

    }


    /**
     * 判断日期是否为节假日 (使用 Set 版本)
     */
    private boolean isHoliday(Set<LocalDate> holidaySet, LocalDate targetDate) {
        if (holidaySet == null || targetDate == null) {
            return false;
        }
        return holidaySet.contains(targetDate);
    }

    /**
     * 安全获取 BigDecimal 值，如果为 null 则返回 BigDecimal.ZERO
     */
    private BigDecimal getSafeBigDecimal(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }






}