package com.mergedata.server.impl;

import com.mergedata.constants.ReqConstant;
import com.mergedata.mapper.YQReportMapper;
import com.mergedata.model.*;
import com.mergedata.server.*;
import com.mergedata.util.PrimaryKeyGenerator;
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
    YQCashService cash;

    @Autowired
    YQOperatorService oper;

    @Autowired
    YQHolidayService hiliday;

    @Autowired
    YQReportMapper report;

    List<ReportDTO> results = new ArrayList<>();



    @Override
    public List<ReportDTO> getAll(String reportdate)  {
         //调用存储过程获取报表数据
        try {


           results = report.getMultParams(Collections.singletonMap("A_REPORTDATE", reportdate));

            // 判断结果集，判断是否平台有无数据，有则查询出返回，无则调用接口获取数据并返回
            if (results.isEmpty()){
                results = getAllReportData(reportdate);
            }
        } catch (Exception e) {
            log.error("获取报表数据异常", e);
            return  results;

        }

        // 添加计算合计数据
        results.add(calculateTotal(results, reportdate));

        return  results;


    }

    @Override
    public Boolean insert(List<ReportDTO> reportDTO) {
        Map<String,Object> map = new HashMap<>();
        PrimaryKeyGenerator pk = new PrimaryKeyGenerator();

        // 生成唯一序列号，此处使用 PrimaryKeyGenerator 类生成主键
        String serialNo = pk.generateKey();
        for (ReportDTO dto : reportDTO) {
            map.put("A_SERIAL_NO",serialNo);
            map.put("A_EMP_ID",dto.getOperatorNo());
            map.put("A_REPORT_DATE",dto.getReportDate().toString());
            map.put("A_REPORT_YEAR",dto.getReportDate().toString().substring(0,4));
            map.put("A_EMP_ID",dto.getOperatorNo());
            map.put("A_EMP_NAME",dto.getOperatorName());
            map.put("A_HISADVANCEPAYMENT",dto.getHisAdvancePayment());
            map.put("A_HISMEDICALINCOME",dto.getHisMedicalIncome());
            map.put("A_HISREGISTRATIONINCOME",dto.getHisRegistrationIncome());
            map.put("A_REPORTAMOUNT",dto.getReportAmount());
            map.put("A_PREVIOUSTEMPORARYRECEIPT",dto.getPreviousTemporaryReceipt());
            map.put("A_HOLIDAYTEMPORARYRECEIPT",dto.getHolidayTemporaryReceipt());
            map.put("A_ACTUALREPORTAMOUNT",dto.getActualReportAmount());
            map.put("A_CURRENTTEMPORARYRECEIPT",dto.getCurrentTemporaryReceipt());
            map.put("A_ACTUALCASHAMOUNT",dto.getActualCashAmount());
            map.put("A_RETAINEDDIFFERENCE",dto.getRetainedDifference());
            map.put("A_RETAINEDCASH",dto.getRetainedCash());
            map.put("a_pettycash",dto.getPettyCash());
            map.put("A_REMARKS",dto.getRemarks());
            map.put("A_TYPE", ReqConstant.SP_TYPE_INSERT);
            }

            report.insertMultParams(map);

        // 建议: 如果方法未实现，返回 false 或抛出异常
        // throw new UnsupportedOperationException("Insert operation is not yet implemented.");
        return true;
    }






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
    public boolean isDateInvalid(List<YQHolidayCalendarDTO> holidays , LocalDate targetDate) {

        if (holidays == null || targetDate == null) {
            return false;
        }

        // 先把所有日期提取到Set中
        Set<LocalDate> dateSet = holidays.stream()
                .map(YQHolidayCalendarDTO::getHolidayDate)
                .collect(Collectors.toSet());

        // 然后快速检查
        boolean exists = dateSet.contains(targetDate);

        return exists;
    }


        /**
         * 【核心报表生成方法】
         * 1. 从各数据源获取数据。
         * 2. 以操作员为基准进行匹配和计算。
         * 3. 对周一进行特殊的财务回溯计算 (A = B - Sum(C) - D)。
         *
         * @param reportdate 目标报表日期 (String yyyy-MM-dd)
         * @return 包含所有操作员计算结果的 ReportDTO 列表
         */


        public List<ReportDTO> getAllReportData(String reportdate) {
            try {
                LocalDate currtDate = LocalDate.parse(reportdate);
                LocalDate preDate = currtDate.minusDays(1);

                // 1. 获取所有必需的原始数据 (保持不变)
                List<YQHolidayCalendarDTO> holidays = hiliday.findByDate();
                List<YQOperator> operators = oper.findData();
                List<YQCashRegRecordDTO> yqDataList = cash.findByDate(reportdate);
                List<HisIncomeDTO> hisIncomeDTOList = hisdata.findByDate(reportdate);
                List<ReportDTO> preReport = report.getMultParams(Collections.singletonMap("A_REPORTDATE", preDate.toString()));

                // 2. 数据预处理：转换为 Map/Set (保持不变)
                Map<String, HisIncomeDTO> hisDataMap = hisIncomeDTOList.stream()
                        .collect(Collectors.toMap(HisIncomeDTO::getOperatorNo, Function.identity(), (v1, v2) -> v1));
                // ... (其他 Map 转换不变)
                Set<LocalDate> holidaySet = holidays.stream()
                        .map(YQHolidayCalendarDTO::getHolidayDate)
                        .collect(Collectors.toSet());

                // 3. 构建结果集 (保持不变)
                List<ReportDTO> resultList = new ArrayList<>();

                // 4. 以操作员为主，遍历构建报表数据 (保持不变)
                for (YQOperator operator : operators) {
                    ReportDTO currentDto = new ReportDTO();

                    // ... (基础信息、ReportAmount、PreviousTemporaryReceipt 赋值保持不变)

                    // 5. 【修正回溯判断条件】
                    /*
                    根据日期的情况分类得到最后时间，
                     1、判断当前日期类型  0 正常 ，1 节假日 ，2 节假日前一天 3 节假日后一天
                     */
                    // ❗ 修正后的条件：当前是工作日 且 前一天是节假日/周末
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
                ReportDTO currentDto,
                LocalDate targetDate,
                Set<LocalDate> holidaySet,
                Function<String, List<ReportDTO>> reportQueryFunction) {

            BigDecimal totalC = BigDecimal.ZERO;
            BigDecimal dAmountFriday = BigDecimal.ZERO;
            LocalDate currentDate = targetDate.minusDays(1); // 从周日开始回溯

            // 2. 回溯循环
            while (true) {

                // 2.1 获取当前回溯日期的历史报表数据 (通过 Mapper)
                List<ReportDTO> historicalReports = reportQueryFunction.apply(currentDate.toString());

                // 2.2 查找当前操作员在历史报表中的记录 (确保用户匹配)
                Optional<ReportDTO> historicalDtoOpt = historicalReports.stream()
                        .filter(r -> currentDto.getOperatorNo().equals(r.getOperatorNo()))
                        .findFirst();

                if (isHoliday(holidaySet, currentDate)) {
                    // 是节假日（周六、周日）：累加 C 金额 (HolidayTemporaryReceipt)

                    BigDecimal cAmount = historicalDtoOpt
                            .map(ReportDTO::getHolidayTemporaryReceipt)
                            .orElse(BigDecimal.ZERO); // 缺失数据默认为 0

                    totalC = totalC.add(getSafeBigDecimal(cAmount));

                    currentDate = currentDate.minusDays(1); // 继续往前

                } else {
                    // 找到中断点：第一个非节假日日期（即周五）

                    // 提取 D 金额 (CurrentTemporaryReceipt)
                    dAmountFriday = historicalDtoOpt
                            .map(ReportDTO::getCurrentTemporaryReceipt)
                            .orElse(BigDecimal.ZERO); // 缺失数据默认为 0

                    break; // 跳出循环
                }

                // 安全检查：防止无限循环
                if (targetDate.toEpochDay() - currentDate.toEpochDay() > 7) {
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