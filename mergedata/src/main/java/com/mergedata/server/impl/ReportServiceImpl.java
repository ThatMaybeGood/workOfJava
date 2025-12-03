package com.mergedata.server.impl;

import com.mergedata.constants.ReqConstant;
import com.mergedata.mapper.YQReportMapper;
import com.mergedata.model.*;
import com.mergedata.server.*;
import com.mergedata.util.PrimaryKeyGenerator;
import lombok.extern.slf4j.Slf4j;
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
    YQCashService cash;

    @Autowired
    YQOperatorService oper;

    @Autowired
    YQHolidayService hiliday;

    @Autowired
    YQReportMapper report;

    List<Report> results = new ArrayList<>();


    @Override
    public List<Report> getAll(String reportdate) {
        //调用存储过程获取报表数据
        try {

            results = report.getMultParams(Collections.singletonMap("A_REPORTDATE", reportdate));

            // 判断结果集，判断是否平台有无数据，有则查询出返回，无则调用接口获取数据并返回
            if (results.isEmpty()) {

                results = getAllReportData(reportdate);

                //❗查询时候数据库没有相关的数据，插入数据库，此处调用 batchInsert 方法批量插入数据
                batchInsert(results);
            }
        } catch (Exception e) {
            log.error("获取报表数据异常", e);
            throw new RuntimeException("获取报表数据异常");
         }
        // 添加计算合计数据
        results.add(calculateTotal(results, reportdate));

        return results;
    }

    @Override
    @Transactional
    public Boolean batchInsert(List<Report> list) {
        if (list == null || list.isEmpty()) {
            // 如果列表为空，直接返回
            return false;
        }

        //作废原有的
        report.insertMultParams(buildParams(list.get(0).getSerialNo(),list.get(0), ReqConstant.SP_TYPE_UPDATE, "0"));

        // 生成唯一序列号，此处使用 PrimaryKeyGenerator 类生成主键
        PrimaryKeyGenerator pks = new PrimaryKeyGenerator();
        String pk = pks.generateKey();

        for (Report dto : list) {
            report.insertMultParams(buildParams(pk, dto, ReqConstant.SP_TYPE_INSERT, "0"));
        }
        //写入主表数据，此处调用
        report.insertMultParams(buildParams(pk,list.get(0), ReqConstant.SP_TYPE_INSERT, "1"));

        return true;
    }

    /**
     * 封装：构建存储过程所需的参数 Map
     *
     * @param pk   主键生成器
     * @param dto  当前 DTO 对象 (作废时可传入 null)
     * @param type 操作类型 (INSERT/UPDATE/INVALIDATE)
     * @return 封装好的 Map
     */
    private Map<String, Object> buildParams(String pk,
                                            Report dto,
                                            String type, String isWriteMaster) {
        Map<String, Object> map = new HashMap<>();
//        Map<String,ReportInsert> map= new HashMap<>();
        // ❗ 优化：只有 INSERT/UPDATE 时才传入日期数据，作废时传入 null 减少冗余
        map.put("A_SERIAL_NO", pk);

        map.put("A_REPORT_DATE", dto.getReportDate());
        map.put("A_REPORT_YEAR", dto.getReportYear());
        map.put("A_EMP_ID", dto.getOperatorNo());
        map.put("A_EMP_NAME", dto.getOperatorName());
        map.put("A_HISADVANCEPAYMENT", dto.getHisAdvancePayment());
        map.put("A_HISMEDICALINCOME", dto.getHisMedicalIncome());
        map.put("A_HISREGISTRATIONINCOME", dto.getHisRegistrationIncome());
        map.put("A_REPORTAMOUNT", dto.getReportAmount());
        map.put("A_PREVIOUSTEMPORARYRECEIPT", dto.getPreviousTemporaryReceipt());
        map.put("A_HOLIDAYTEMPORARYRECEIPT", dto.getHolidayTemporaryReceipt());
        map.put("A_ACTUALREPORTAMOUNT", dto.getActualReportAmount());
        map.put("A_CURRENTTEMPORARYRECEIPT", dto.getCurrentTemporaryReceipt());
        map.put("A_ACTUALCASHAMOUNT", dto.getActualCashAmount());
        map.put("A_RETAINEDDIFFERENCE", dto.getRetainedDifference());
        map.put("A_RETAINEDCASH", dto.getRetainedCash());
        map.put("A_PETTYCASH", dto.getPettyCash());
        map.put("A_REMARKS", dto.getRemarks());


        map.put("A_TYPE", type);
        // 是否写入主表
        map.put("A_ISINSERTMASTER", isWriteMaster);

        return map;
    }


    /**
     * 封装：执行作废操作
     */
    private void executeInvalidate(String pk, String type, String isWriteMaster) {
        // ❗ 优化：调用封装方法，只传入作废必需的类型和序列号
        Map<String, Object> invalidateMap = buildParams(pk, null, type, isWriteMaster);
        //
        report.insertMultParams(invalidateMap);
    }


    private Report calculateTotal(List<Report> dtoList, String reportdate) {
        // 1. 定义 BigDecimal 求和初始值 (0) 和操作符 (加法)
        final BigDecimal ZERO = BigDecimal.ZERO;
        BinaryOperator<BigDecimal> sumOperator = BigDecimal::add;

        // 2. 初始化合计对象
        Report total = new Report();
        total.setOperatorNo("");
        total.setOperatorName("合计");

        // 3. 定义一个通用的求和方法 (可选，但推荐简化代码)
        // 这是一个接受 List<ReportDTO> 和 Getter 方法引用的泛型函数
        Function<Function<Report, BigDecimal>, BigDecimal> sumByField =
                getter -> dtoList.stream()
                        .map(getter)
                        .filter(Objects::nonNull) // 过滤掉可能存在的 null 值
                        .reduce(ZERO, sumOperator); // 使用 reduce 进行高精度求和

        // 4. 应用求和逻辑到每个字段
        total.setHisAdvancePayment(sumByField.apply(Report::getHisAdvancePayment));
        total.setHisMedicalIncome(sumByField.apply(Report::getHisMedicalIncome));
        total.setHisRegistrationIncome(sumByField.apply(Report::getHisRegistrationIncome));
        total.setRetainedCash(sumByField.apply(Report::getRetainedCash));
        total.setReportAmount(sumByField.apply(Report::getReportAmount));
        total.setPreviousTemporaryReceipt(sumByField.apply(Report::getPreviousTemporaryReceipt));
        total.setHolidayTemporaryReceipt(sumByField.apply(Report::getHolidayTemporaryReceipt));

        // 注意：原代码中有重复求和 ActualCashAmount，这里只保留一次
        total.setActualCashAmount(sumByField.apply(Report::getActualCashAmount));
        total.setCurrentTemporaryReceipt(sumByField.apply(Report::getCurrentTemporaryReceipt));
        total.setRetainedDifference(sumByField.apply(Report::getRetainedDifference));
        total.setPettyCash(sumByField.apply(Report::getPettyCash));

        // 5. 设置其他字段
        total.setRemarks("合计行，不展示在报表中");
        total.setReportDate(reportdate);
        total.setCreateTime(LocalDateTime.now());

        return total;
    }


    /**
     * 1. 从各数据源获取数据。
     * 2. 以操作员为基准进行匹配和计算。
     * 3. 对周一进行特殊的回溯计算 (A = B - Sum(C) - D)。
     *
     * @param reportdate 目标报表日期 (String yyyy-MM-dd)
     * @return 包含所有操作员计算结果的 ReportDTO 列表
     */

    public List<Report> getAllReportData(String reportdate) {
        try {
            LocalDate currtDate = LocalDate.parse(reportdate);
            LocalDate preDate = currtDate.minusDays(1);

            // 1. 获取所有必需的原始数据 (保持不变)
            List<YQHolidayCalendar> holidays = hiliday.findAll();
            List<YQOperator> operators = oper.findAll();
            List<YQCashRegRecord> yqDataList = cash.findByDate(reportdate);
            List<HisIncome> hisIncomeList = hisdata.findByDate(reportdate);
            List<Report> preReport = report.getMultParams(Collections.singletonMap("A_REPORTDATE", preDate.toString()));

            // 2. 数据预处理：转换为 Map/Set (保持不变)
            Map<String, HisIncome> hisDataMap = hisIncomeList.stream()
                    .collect(Collectors.toMap(HisIncome::getOperatorNo, Function.identity(), (v1, v2) -> v1));
            // ... (其他 Map 转换不变)
            Set<LocalDate> holidaySet = holidays.stream()
                    .map(YQHolidayCalendar::getHolidayDate)
                    .collect(Collectors.toSet());

            // 3. 构建结果集 (保持不变)
            List<Report> resultList = new ArrayList<>();

            PrimaryKeyGenerator pks = new PrimaryKeyGenerator();
            String pk = pks.generateKey();


            // 4. 以操作员为主，遍历构建报表数据 (保持不变)
            for (YQOperator operator : operators) {
                Report currentDto = new Report();
                currentDto.setSerialNo(pk);
                currentDto.setOperatorNo(operator.getOperatorNo());
                currentDto.setOperatorName(operator.getOperatorName());
                currentDto.setReportDate(reportdate);

                // ... (基础信息、ReportAmount、PreviousTemporaryReceipt 赋值保持不变)

                    /*
                    根据日期的情况分类得到最后时间，
                     1、判断当前日期类型  0 正常 ，1 节假日 ，2 节假日前一天 3 节假日后一天
                     */
                // ❗当前是工作日 且 前一天是节假日/周末
                boolean isAfterHoliday = !isHoliday(holidaySet, currtDate) && isHoliday(holidaySet, currtDate.minusDays(1));

                if (isAfterHoliday) {
                    // 符合条件：执行复杂回溯计算 (A = B - Sum(C) - D)

                    calculateActualReportAmountForMonday(
                            currentDto,
                            currtDate,
                            holidaySet,
                            // 传递查询函数
                            dateString -> report.getMultParams(Collections.singletonMap("A_REPORTDATE", dateString))
                    );

                } else if (isHoliday(holidaySet, currtDate)) {
                    // 节假日逻辑：A = B - C
                    BigDecimal actualReportAmount = currentDto.getReportAmount().subtract(currentDto.getHolidayTemporaryReceipt());
                    currentDto.setActualReportAmount(actualReportAmount);

                } else if (isHoliday(holidaySet, currtDate.plusDays(1))) {
                    // 节假日前一天 (周五/最后一天) 逻辑
                    // A = B - D' - E'
                    BigDecimal actualReportAmount = currentDto.getReportAmount()
                            .subtract(currentDto.getPreviousTemporaryReceipt())
                            .subtract(currentDto.getCurrentTemporaryReceipt());

                    currentDto.setActualReportAmount(actualReportAmount);

                } else {
                    // 正常工作日逻辑
                    currentDto.setActualReportAmount(currentDto.getReportAmount());
                }


                // 6. 后续计算 (保持不变)
                currentDto.setActualCashAmount(currentDto.getActualReportAmount().add(currentDto.getCurrentTemporaryReceipt()));
                // ... (关联 YQCashRegRecord 数据和 RetainedDifference 计算保持不变)

                // 7. 加入结果集 (保持不变)
                resultList.add(currentDto);
            }

            log.info("{}生成报表完成，共处理 {} 个操作员", reportdate, resultList.size());
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
            Report currentDto,
            LocalDate targetDate,
            Set<LocalDate> holidaySet,
            Function<String, List<Report>> reportQueryFunction) {

        BigDecimal totalC = BigDecimal.ZERO;
        BigDecimal dAmountFriday = BigDecimal.ZERO;
        LocalDate currentDate = targetDate.minusDays(1); // 从周日开始回溯

        // 2. 回溯循环
        while (true) {

            // 2.1 获取当前回溯日期的历史报表数据 (通过 Mapper)
            List<Report> historicalReports = reportQueryFunction.apply(currentDate.toString());

            // 2.2 查找当前操作员在历史报表中的记录 (确保用户匹配)
            Optional<Report> historicalDtoOpt = historicalReports.stream()
                    .filter(r -> currentDto.getOperatorNo().equals(r.getOperatorNo()))
                    .findFirst();

            if (isHoliday(holidaySet, currentDate)) {
                // 是节假日（周六、周日）：累加 C 金额 (HolidayTemporaryReceipt)

                BigDecimal cAmount = historicalDtoOpt
                        .map(Report::getHolidayTemporaryReceipt)
                        .orElse(BigDecimal.ZERO); // 缺失数据默认为 0

                totalC = totalC.add(getSafeBigDecimal(cAmount));

                currentDate = currentDate.minusDays(1); // 继续往前

            } else {
                // 找到中断点：第一个非节假日日期（即周五）

                // 提取 D 金额 (CurrentTemporaryReceipt)
                dAmountFriday = historicalDtoOpt
                        .map(Report::getCurrentTemporaryReceipt)
                        .orElse(BigDecimal.ZERO); // 缺失数据默认为 0

                break; // 跳出循环
            }

            // 安全检查：防止无限循环
            if (targetDate.toEpochDay() - currentDate.toEpochDay() > 15) {
                log.warn("回溯查找失败，连续节假日过多，在 {} 无法找到工作日。", targetDate);
                break;
            }
        }

        // 3. 应用公式：A = B - Sum(C) - D
        BigDecimal finalActualReportAmount = currentDto.getReportAmount()
                .subtract(totalC)
                .subtract(dAmountFriday);

        // 4. 设置计算结果
        currentDto.setActualReportAmount(finalActualReportAmount);
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