package com.mergedata.server.impl;

import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.mergedata.constants.Constant;
import com.mergedata.model.dto.InpReportRequestBody;
import com.mergedata.model.dto.external.HisInpIncomeResponseDTO;
import com.mergedata.model.dto.external.HisOutpIncomeResponseDTO;
import com.mergedata.model.vo.OutpReportVO;
import com.mergedata.model.dto.OutpReportRequestBody;
import com.mergedata.mapper.CashMapper;
import com.mergedata.mapper.HolidayMapper;
import com.mergedata.mapper.OperatorMapper;
import com.mergedata.mapper.OutpReportMapper;
import com.mergedata.model.entity.*;
import com.mergedata.server.HisDataService;
import com.mergedata.server.ReportService;
import com.mergedata.server.YQHolidayService;
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
    OutpReportMapper report;

    @Autowired
    YQHolidayService holidayService;

    List<OutpReportVO> outpResults = new ArrayList<>();

     /**
      * 获取门诊报表数据
      * @param body 门诊报表请求体
      * @return 门诊报表数据列表
      */
    @Override
    public List<OutpReportVO> getOutpReport(OutpReportRequestBody body) {
        //调用存储过程获取报表数据
        try {
            LocalDate currentDate = body.getReportDate();
            outpResults = report.findReport(body);

            //接收ExtendParams1为true时，即初始化报表
            String param1 = body.getExtendParams1();
            Boolean isInitFlag = (param1 != null && "true".equalsIgnoreCase(param1));

            // 判断结果集，判断是否平台有无数据，有则查询出返回，无则调用接口获取数据并返回
            if (outpResults.isEmpty() || isInitFlag) {
                outpResults = getOutpReportData(currentDate);
                //查询时候数据库没有相关的数据，插入数据库，此处调用 firstInsert 方法批量插入数据
                isInitInsertOutp(outpResults, currentDate);
            }
        } catch (Exception e) {
            log.error("获取报表数据异常", e);
            throw new RuntimeException("获取报表数据异常");
        }

        // 添加计算合计数据
        //屏蔽计算合计金额
//        results.add(calculateTotal(results, LocalDate.parse(reportdate)));


        // 进行筛选
        return outpResults.stream()
                .filter(r -> (body.getInpWindow() == null || body.getInpWindow() != 1 || Integer.valueOf(1).equals(r.getInpWindow())))
                .filter(r -> (body.getAtm() == null || body.getAtm() != 1 || Integer.valueOf(1).equals(r.getAtm())))
                .collect(Collectors.toList());
    }


    /**
     * 批量插入门诊报表数据
     * @param outpReportVOList 门诊报表数据列表
     * @return 是否成功
     */
    @Override
    @Transactional
    public Integer insertOutpReport(List<OutpReportVO> outpReportVOList) {
       try {

           if (outpReportVOList == null || outpReportVOList.isEmpty()) {
               return Constant.FAILURE;
           }

           // 1. 确定要作废的日期
           LocalDate reportDate = outpReportVOList.get(0).getReportDate();

           // 2. 作废该日期下所有的主表记录
           if (!report.selectReportByDate(reportDate).isEmpty()) {
               report.updateByDate(reportDate);
           }
           log.info("{} {} 历史报表数据作废完成 !", reportDate, Constant.REPORT_NAME_OUTP);

           //3.转换对应门诊报表数据
           List<OutpReportVO> list = exchangeOutpReportData(reportDate, outpReportVOList);


           // =======================================================
           // 只生成一个主键，作为主表的主键和明细表的外键
           // =======================================================
           PrimaryKeyGenerator pks = new PrimaryKeyGenerator();
           String pk = pks.generateKey();

           List<OutpCashMainEntity> mainList = new ArrayList<>();
           List<OutpCashSubEntity> subList = new ArrayList<>();

           // ------------------------------------------
           //  只创建 1 条 CashStattisticsMain (主表) 记录
           // ------------------------------------------
           OutpCashMainEntity main = new OutpCashMainEntity();
           OutpReportVO firstOutpReportVO = list.get(0); // 随便取一条 Report 来获取通用的主表信息

           main.setSerialNo(pk); // 唯一主键
           main.setIsvalid(true);

           // 复制通用的日期/年份信息
           if (firstOutpReportVO.getReportDate() != null) {
               try {
                   main.setReportDate(firstOutpReportVO.getReportDate());
               } catch (Exception e) {
                   throw new RuntimeException("日期格式错误: " + firstOutpReportVO.getReportDate());
               }
           }
           if (firstOutpReportVO.getReportYear() != null) {
               main.setReportYear(Integer.valueOf(firstOutpReportVO.getReportYear()));
           }


           main.setCreateTime(firstOutpReportVO.getCreateTime() != null ? firstOutpReportVO.getCreateTime() : LocalDateTime.now());
           main.setUpdateTime(LocalDateTime.now());

           mainList.add(main); // 主表列表中只有 1 条记录


           // ------------------------------------------
           // 遍历 Report 列表，创建 N 条 CashStatisticsSub (明细表) 记录
           // ------------------------------------------
           for (OutpReportVO outpReportVO : list) {
               OutpCashSubEntity sub = new OutpCashSubEntity();

               BeanUtils.copyProperties(outpReportVO, sub);

               // 所有明细记录使用同一个主键作为外键
               sub.setSerialNo(pk);
               sub.setHisOperatorNo(outpReportVO.getOperatorNo());
               sub.setHisOperatorName(outpReportVO.getOperatorName());

               //添加 结账序号 结账时间  2025.12.31
               sub.setRowNum(outpReportVO.getRowNum());
               sub.setAcctDate(outpReportVO.getAcctDate());
               sub.setAcctNo(outpReportVO.getAcctNo());

               if (!outpReportVO.getOperatorName().contains("合计")) {
                   subList.add(sub);
               }
           }

           // --- 3. 批量插入操作（事务生效） ---
           // 此时 mainList 只有 1 条记录
           int mainCount = report.batchInsertList(mainList);

           int subCount = 0;
           if (!subList.isEmpty()) {
               subCount = report.batchInsertSubList(subList);
           }


           return Constant.SUCCESS;
       } catch (Exception e) {
           log.error("插入门诊报表数据异常", e);
           throw new RuntimeException("插入门诊报表数据异常");
       }
    }


    /**
     * 获取住院报表数据
     * @param body 住院报表请求体
     * @return 住院报表数据
     */
    @Override
    public InpCashMainEntity getInpReport(InpReportRequestBody body) {

        LocalDate currentDate = body.getReportDate();
        String holidayTotalFlag = body.getHolidayTotalFlag();
        //接收initFlag为1时，即初始化报表
        String initFlag = body.getInitFlag();
        Boolean isInitFlag = (initFlag != null && "1".equalsIgnoreCase(initFlag));

        InpCashMainEntity inpResult = new InpCashMainEntity();
        List<InpCashMainEntity> mainList = new ArrayList<>();

        try {
            //查询日期类型
            String holidayType = holidayService.queryDateType(currentDate, Constant.TYPE_INP);

            //是否节假日汇总
            if (holidayTotalFlag.equals(Constant.YES)) {
                if (holidayType.equals(Constant.HOLIDAY_AFTER)) {
                    LocalDate startDate = currentDate;
                    //开始汇总计算
                    while (true) {

                        startDate = startDate.minusDays(1);  //日期倒减

                        inpResult = queryInpReportByDate(startDate, Constant.NO);

                        // 1. 查主表单条 是否存在
                        if ( inpResult== null) {
                            //获取初始化的数据
                            inpResult = getInpReportData(startDate, holidayType, Constant.NO);
                            //方法批量插入数据
                            isInitInsertInp(inpResult, Constant.YES);
                        }

                        mainList.add(inpResult);

                        if (holidayService.queryDateType(startDate, Constant.TYPE_INP).equals(Constant.HOLIDAY_PRE)) {
                            break;
                        }

                        // 防止无限循环
                        if (currentDate.toEpochDay() - startDate.toEpochDay() > 30) {
                            log.warn("回溯查找失败，连续节假日超过30天，从{}开始，在 {} 无法找到正常工作日。", currentDate, startDate);
                            break;
                        }

                    }
                    // 2. 对主表进行节假日汇总
                    //汇总的数据插入数据库
                    inpResult = inpHolidayTotal(mainList, currentDate);

                    isInitInsertInp(inpResult, Constant.YES);
                    log.info("住院现金统计-节假日汇总写入成功，报表日期{}", currentDate);
                }

            } else {
                //查询数据库是否有相关数据
                inpResult = queryInpReportByDate(currentDate, Constant.NO);

                // 1. 查主表单条 是否存在
                if (inpResult == null || isInitFlag) {
                    //获取初始化的数据
                    inpResult = getInpReportData(currentDate, holidayType, Constant.NO);
                    //查询时候数据库没有相关的数据，插入数据库，此处调用插入数据
                    isInitInsertInp(inpResult, Constant.YES);

                }
            }
        } catch (Exception e) {
            log.error("获取住院报表数据异常", e);
            throw new RuntimeException("获取住院报表数据异常");
        }

        return inpResult;
    }

    /**
     * 插入住院现金表数据
     */
    @Override
    public Integer insertInpReport(InpCashMainEntity main) {
        return isInitInsertInp(main, Constant.NO);
    }



    /**
     * 对住院现金统计主表实体类进行节假日汇总
     * @param allMains 所有住院现金统计主表实体类列表
     * @param reportDate 报表日期
     * @return 汇总后的住院现金统计主表实体类
     */
    public InpCashMainEntity inpHolidayTotal(List<InpCashMainEntity> allMains, LocalDate reportDate) {
        // 1. 创建一个汇总对象（合计行）
        InpCashMainEntity summary = new InpCashMainEntity();
        InpCashSubEntity summarySub = new InpCashSubEntity();
        List<InpCashSubEntity> summarySubs = new ArrayList<>();

        if (allMains == null || allMains.isEmpty()) {
            return summary;
        }

        List<InpCashSubEntity> allSubs = allMains.stream()
                .map(InpCashMainEntity::getSubs)
                .flatMap(List::stream)
                .collect(Collectors.toList());

        for (InpCashSubEntity item : allSubs) {
            // 上午部分
            summarySub.setPreviousDayAdvanceReceipt(summarySub.getPreviousDayAdvanceReceipt().add(item.getPreviousDayAdvanceReceipt()));
            summarySub.setTodayAdvancePayment(summarySub.getTodayAdvancePayment().add(item.getTodayAdvancePayment()));
            summarySub.setTodaySettlementIncome(summarySub.getTodaySettlementIncome().add(item.getTodaySettlementIncome()));
            summarySub.setTodayPreHospitalIncome(summarySub.getTodayPreHospitalIncome().add(item.getTodayPreHospitalIncome()));
            summarySub.setTrafficAssistanceFund(summarySub.getTrafficAssistanceFund().add(item.getTrafficAssistanceFund()));
            summarySub.setBloodDonationCompensation(summarySub.getBloodDonationCompensation().add(item.getBloodDonationCompensation()));
            summarySub.setReceivablePayable(summarySub.getReceivablePayable().add(item.getReceivablePayable()));
            summarySub.setTodayReportTotal(summarySub.getTodayReportTotal().add(item.getTodayReportTotal()));
            summarySub.setPreviousDayIOU(summarySub.getPreviousDayIOU().add(item.getPreviousDayIOU()));
            summarySub.setTodayOutpatientIOU(summarySub.getTodayOutpatientIOU().add(item.getTodayOutpatientIOU()));
            summarySub.setTodayReportReceivablePayable(summarySub.getTodayReportReceivablePayable().add(item.getTodayReportReceivablePayable()));

            // 下午及留存部分
            summarySub.setTodayAdvanceReceipt(summarySub.getTodayAdvanceReceipt().add(item.getTodayAdvanceReceipt()));
            summarySub.setTodayReportCashReceived(summarySub.getTodayReportCashReceived().add(item.getTodayReportCashReceived()));
            summarySub.setTodayCashReceivedTotal(summarySub.getTodayCashReceivedTotal().add(item.getTodayCashReceivedTotal()));
            summarySub.setBalance(summarySub.getBalance().add(item.getBalance()));
            summarySub.setAdjustment(summarySub.getAdjustment().add(item.getAdjustment()));
            summarySub.setTodayIOU(summarySub.getTodayIOU().add(item.getTodayIOU()));
            summarySub.setHolidayPayment(summarySub.getHolidayPayment().add(item.getHolidayPayment()));

            // 收费员留存部分
            summarySub.setCashOnHand(summarySub.getCashOnHand().add(item.getCashOnHand()));
            summarySub.setDifference(summarySub.getDifference().add(item.getDifference()));

            summarySubs.add(summarySub);
        }

        summary.setHolidayTotalFlag(Constant.YES);
        summary.setReportDate(reportDate);
        summary.setReportYear(reportDate.getYear());
        summary.setSubs(summarySubs);

        log.info("住院现金统计-节假日汇总计算，报表日期：{}", reportDate);

        return summary;
    }

    /**
     * 根据日期查询住院现金统计主表
     * @param date 日期
     * @param holidayTotalFlag 节假日汇总标志 0：非节假日汇总 1：节假日汇总
     * @return 住院现金统计主表
     */
    public InpCashMainEntity queryInpReportByDate(LocalDate date, String holidayTotalFlag) {
        // 1. 查主表单条
        InpCashMainEntity main = Db.lambdaQuery(InpCashMainEntity.class)
                .eq(InpCashMainEntity::getReportDate, date)
                .eq(InpCashMainEntity::getValidFlag, Constant.YES)
                .eq(InpCashMainEntity::getHolidayTotalFlag, holidayTotalFlag)
                .one();

        if (main == null) {
            return null;
        }

        // 2. 查从表列表
        List<InpCashSubEntity> subs = Db.lambdaQuery(InpCashSubEntity.class)
                .eq(InpCashSubEntity::getSerialNo, main.getSerialNo())
                .list();
        // 4. 设置子表列表
        main.setSubs(subs != null ? subs : new ArrayList<>());

        return main;
    }


    /**
     * 1. 从各数据源获取数据。
     * 2. 以操作员为基准进行匹配和计算。
     * 3. 各种公式的计算和 暂收款的取值
     * @param currtDate        报表日期
     * @param holidayType      节假日类型
     * @param holidayTotalFlag 节假日汇总标志 0：非节假日汇总 1：节假日汇总
     * @return 住院现金统计
     */
    public InpCashMainEntity getInpReportData(LocalDate currtDate, String holidayType, String holidayTotalFlag) {
        try {
            LocalDate preDate = currtDate.minusDays(1);

            // 1. 获取所有必需的原始数据
            List<YQHolidayEntity> holidays = hiliday.selectByYear(currtDate.getYear());
            List<YQOperatorEntity> operators = oper.selectByCategory(Constant.TYPE_INP);

            List<YQCashRegRecordEntity> yqRecordList = cash.selectByDate(currtDate);

            // 假设 HIS 接口需要 String，则转换
            List<HisInpIncomeResponseDTO> hisInpIncomeResponseDTOList = hisdata.findByDateInp(currtDate.toString());

            // 1. 获取前一天对象
            InpCashMainEntity preInpResult = queryInpReportByDate(preDate, holidayTotalFlag);
            List<InpCashSubEntity> preInpReportSub;
            if (preInpResult != null) {
                // 2. 如果存在，正常取子表
                preInpReportSub = preInpResult.getSubs();
            } else {
                log.warn("前一天 {} 的数据不存在，初始化为空列表", preDate);
                preInpReportSub = new ArrayList<>();
            }


            // 2. 数据预处理：转换为 Map/Set (保持不变)
            Map<String, HisInpIncomeResponseDTO> hisDataMap = hisInpIncomeResponseDTOList.stream()
                    .collect(Collectors.toMap(HisInpIncomeResponseDTO::getOperatorNo, Function.identity(), (v1, v2) -> v1));

            Map<String, YQCashRegRecordEntity> cashMap = yqRecordList.stream()
                    .collect(Collectors.toMap(YQCashRegRecordEntity::getOperatorNo, Function.identity(), (v1, v2) -> v1));

            Set<LocalDate> holidaySet = holidays.stream()
                    .map(YQHolidayEntity::getHolidayDate)
                    .collect(Collectors.toSet());

            // 3. 构建结果集
            InpCashMainEntity resultVo = new InpCashMainEntity();
            List<InpCashSubEntity> inpCashSubList = new ArrayList<>();

            PrimaryKeyGenerator pks = new PrimaryKeyGenerator();
            String pk = pks.generateKey();

            int count = 0;
            // 4. 以操作员为主，遍历构建报表数据
            for (YQOperatorEntity operator : operators) {
                InpCashSubEntity inpCashSub = new InpCashSubEntity();

                count++;

                inpCashSub.setSerialNo(pk);
                inpCashSub.setOperatorNo(operator.getOperatorNo());
                inpCashSub.setOperatorName(operator.getOperatorName());
                inpCashSub.setCreatedTime(LocalDateTime.now());

                // =========================================================================
                // 基础信息赋值区域
                // =========================================================================

                // 1. 获取当前操作员的 HIS 收入数据 (保持不变)
                HisInpIncomeResponseDTO hisInpIncomeResponseDTO = hisDataMap.get(operator.getOperatorNo());

                // 2. 从昨日数据 (preReport) 查找操作员的记录 (保持不变)
                InpCashSubEntity yesterdayOutpReportVO = preInpReportSub.stream()
                        .filter(r -> operator.getOperatorNo().equals(r.getOperatorNo()))
                        .findFirst()
                        .orElse(null);

                // --- 填充 HIS 收入和 ReportAmount (保持不变) ---
                if (hisInpIncomeResponseDTO != null) {
                    inpCashSub.setTodayAdvancePayment(getSafeBigDecimal(hisInpIncomeResponseDTO.getHisAdvancePayment()));
                    inpCashSub.setTodaySettlementIncome(getSafeBigDecimal(hisInpIncomeResponseDTO.getHisSettlementIncome()));
                    inpCashSub.setTodayPreHospitalIncome(getSafeBigDecimal(hisInpIncomeResponseDTO.getHisPreHospitalIncome()));
                }

                // --- 提取前日暂收款
                if (yesterdayOutpReportVO != null) {
                    inpCashSub.setPreviousDayAdvanceReceipt(getSafeBigDecimal(yesterdayOutpReportVO.getPreviousDayAdvanceReceipt()));
                }

                YQCashRegRecordEntity cashRecord = cashMap.get(operator.getOperatorNo());

                //获取小程序数据源
                if (cashRecord != null) {
                    inpCashSub.setCashOnHand(getSafeBigDecimal(cashRecord.getRetainedCash()));
//                    inpCashSub.setRemarks(cashRecord.getRemarks());
                }

//                /*
//                 *回溯计算实收报表    住院没有回溯计算
//                 */
//                if (holidayType.equals(Constant.HOLIDAY_AFTER)) {
//                    //住院如果是周一，即（1）数据为周五（12）的数据
//
//                    log.info("第{}条，[{}]节假日后正常工作日第一天特殊处理，回溯计算实际报表金额开始......................", count, currtDate);
//                    inpCalculateForMonday(
//                            inpCashSub,
//                            currtDate,
//                            holidaySet,
//                            date -> {
//                                InpCashMainEntity main = queryInpReportByDate(date, holidayTotalFlag);
//                                // 增加判空：如果主表不存在或子表为 null，返回空列表而不是抛异常
//                                return (main != null && main.getSubs() != null) ? main.getSubs() : new ArrayList<>();
//                            }
//                    );
//
//                }


                // 8 =（2）-（1）+（3）+（4）+（5）+(6)+(7) 今日报表数合计
                inpCashSub.setTodayReportTotal(
                        inpCashSub.getTodayAdvancePayment()
                                .subtract(inpCashSub.getPreviousDayAdvanceReceipt())
                                .add(inpCashSub.getTodaySettlementIncome())
                                .add(inpCashSub.getTodayPreHospitalIncome())
                                .add(inpCashSub.getTrafficAssistanceFund())
                                .add(inpCashSub.getBloodDonationCompensation()
                                        .add(inpCashSub.getReceivablePayable())));

                //（11）=（8）+（9）+（10）-（18） 今日报表应收/应付
                inpCashSub.setTodayReportReceivablePayable(
                        inpCashSub.getTodayReportTotal()
                                .add(inpCashSub.getPreviousDayIOU()
                                        .add(inpCashSub.getTodayIOU()
                                                .add(inpCashSub.getTodayOutpatientIOU()
                                                        .subtract(inpCashSub.getHolidayPayment()))))
                );

                //（14）=（12）+（13） 今日实收现金合计
                inpCashSub.setTodayCashReceivedTotal(
                        inpCashSub.getTodayAdvanceReceipt()
                                .add(inpCashSub.getTodayReportCashReceived()));

                //（15）=（13）-（11）余额
                inpCashSub.setBalance(
                        inpCashSub.getTodayReportCashReceived()
                                .subtract(inpCashSub.getTodayReportReceivablePayable()));

                //（17）=（16）-（15）今日欠条
                inpCashSub.setTodayIOU(inpCashSub.getAdjustment()
                        .subtract(inpCashSub.getBalance()));

                //（20）=（19）-（11）  差额
                inpCashSub.setDifference(inpCashSub.getCashOnHand()
                        .subtract(inpCashSub.getTodayReportReceivablePayable()));

                // 加入结果集
                inpCashSubList.add(inpCashSub);
            }

            resultVo.setReportDate(currtDate);
            resultVo.setReportYear(currtDate.getYear());
            resultVo.setSubs(inpCashSubList);

            log.info("{}生成住院报表完成，共处理 {} 个操作员", currtDate.toString(), inpCashSubList.size());
            return resultVo;

        } catch (Exception e) {
            log.error("住院报表生成失败", e);
            return null;
        }
    }

    /**
     * 住院如果是周一，即（1）数据为周五（12）的数据
     */
    private void inpCalculateForMonday(
            InpCashSubEntity currentDto,
            LocalDate targetDate,
            Set<LocalDate> holidaySet,
            // ❗ 修正点 11: 将 Function 的输入类型改为 LocalDate
            Function<LocalDate, List<InpCashSubEntity>> reportQueryFunction) {

        BigDecimal totalC = BigDecimal.ZERO;
        BigDecimal dAmountFriday = BigDecimal.ZERO;
        LocalDate currentDate = targetDate.minusDays(1); // 从周日开始回溯


        //当前传入日期为当月第一天
        if (targetDate.getDayOfMonth() != 1) {

            // 2. 回溯循环
            while (true) {

                // 2.1 获取历史报表数据
                // 这里调用的是上面 Lambda 定义的 apply。
                // 只要按上面的方案修改了 Lambda，这里 historicalInpCashSubEntityS 就永远不会是 null。
                List<InpCashSubEntity> historicalInpCashSubEntityS = reportQueryFunction.apply(currentDate);

                // 2.2 查找当前操作员记录
                Optional<InpCashSubEntity> historicalDtoOpt = historicalInpCashSubEntityS.stream()
                        .filter(r -> r.getOperatorNo() != null && r.getOperatorNo().equals(currentDto.getOperatorNo()))
                        .findFirst();

//                // 提取当前的节假日的金额 (HolidayTemporaryReceipt)
//                BigDecimal cAmount = historicalDtoOpt
//                        .map(InpCashSubEntity::getHolidayTemporaryReceipt)
//                        .orElse(BigDecimal.ZERO); // 缺失数据默认为 0


                if (isHoliday(holidaySet, currentDate)) {
                    // 是节假日（周六、周日）：累加 C 金额 (HolidayTemporaryReceipt)

                    //当前日期为当月第一天
                    if (currentDate.getDayOfMonth() == 1) {
                        break;
                    }

//                    totalC = totalC.add(getSafeBigDecimal(cAmount));

                    currentDate = currentDate.minusDays(1); // 继续往前

                } else {
                    // 找到中断点：第一个非节假日日期（即周五）
                    // 周五的 C (HolidayTemporaryReceipt) 也累加进去
//                    totalC = totalC.add(getSafeBigDecimal(cAmount));

                    // 提取周五金额 (今日暂收款)
                    dAmountFriday = historicalDtoOpt
                            .map(InpCashSubEntity::getPreviousDayAdvanceReceipt)
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

//        // 3. 应用公式：A = B - Sum(C) - D
//        BigDecimal finalActualReportAmount = currentDto.getReportAmount()
//                .subtract(totalC)
//                .subtract(dAmountFriday);
//        log.info("员工ID:{}，姓名:{},回溯计算实际报表金额 {} - {} - {} = {} ",
//                currentDto.getOperatorNo(), currentDto.getOperatorName(), currentDto.getReportAmount(), totalC, dAmountFriday, finalActualReportAmount);


        // 4. 设置计算结果
        currentDto.setPreviousDayAdvanceReceipt(dAmountFriday);

    }


    /*
     * 对应门诊日期报表无数据时候，是否初始化写入数据
     * @param list 门诊报表数据列表
     * @param date 日期
     */
    @Transactional(rollbackFor = Exception.class)
    public void isInitInsertOutp(List<OutpReportVO> list, LocalDate date) {
        try {
            {

            }
            // 2. 作废该日期下所有的主表记录
            if (!report.selectReportByDate(date).isEmpty()) {
                report.updateByDate(date);
            }
            log.info("历史报表数据作废完成.");


            // =======================================================
            // 只生成一个主键，作为主表的主键和明细表的外键
            // =======================================================
            PrimaryKeyGenerator pks = new PrimaryKeyGenerator();
            String pk = pks.generateKey();

            List<OutpCashSubEntity> subList = new ArrayList<>();

            // ------------------------------------------
            //  只创建 1 条 CashStattisticsMain (主表) 记录
            // ------------------------------------------
            OutpCashMainEntity main = new OutpCashMainEntity();

            main.setSerialNo(pk); // 唯一主键
            main.setIsvalid(true);
            main.setReportDate(date);
            main.setReportYear(date.getYear());
            main.setCreateTime(LocalDateTime.now());


            // ------------------------------------------
            // 遍历 Report 列表，创建 N 条 CashStatisticsSub (明细表) 记录
            // ------------------------------------------
            for (OutpReportVO outpReportVO : list) {
                OutpCashSubEntity sub = new OutpCashSubEntity();

                BeanUtils.copyProperties(outpReportVO, sub);

                // 所有明细记录使用同一个主键作为外键
                sub.setSerialNo(pk);
                sub.setHisOperatorNo(outpReportVO.getOperatorNo());
                sub.setHisOperatorName(outpReportVO.getOperatorName());

                //添加 结账序号 结账时间  2025.12.31
                sub.setRowNum(outpReportVO.getRowNum());
                sub.setAcctDate(outpReportVO.getAcctDate());
                sub.setAcctNo(outpReportVO.getAcctNo());

                if (!outpReportVO.getOperatorName().contains("合计")) {
                    subList.add(sub);
                }
            }


            // 2. 写入主表
            Db.save(main);

            // 3. 批量写入子表
            if (!subList.isEmpty()) {
                Db.saveBatch(subList);
            }
        } catch (Exception e) {
            log.error("初始化插入门诊现金主表数据失败，日期：{}", date, e);
            throw new RuntimeException("初始化插入门诊现金主表数据失败" + e.getMessage());
        }
    }




    /**
     *   初始化插入住院现金主表数据
     * @param main       住院现金主表实体
     * @param isInitFlag 是否初次写入标志 ，默认值为"1"，表示初次写入
     * @return 插入成功的记录数
     */
    @Transactional(rollbackFor = Exception.class)
    public Integer isInitInsertInp(InpCashMainEntity main, String isInitFlag) {
        PrimaryKeyGenerator pks = new PrimaryKeyGenerator();
        String pk = pks.generateKey();

        //界面手工录入修改时候，保存数据重新计算明细的公式
        if (isInitFlag.equals(Constant.NO)) {
            main.setSubs(exchangeInpReportData(main.getSubs()));
        }


        // 通用设置这些公共属性
        main.setValidFlag(Constant.YES);
        main.setCreateTime(LocalDateTime.now());
        main.setSerialNo(pk); // 唯一主键

        try {

            /*
             * 作废旧数据
             * 根据 report_date 将之前已生效的报表全部改为作废(0)
             */
            Db.lambdaUpdate(InpCashMainEntity.class)
                    .eq(InpCashMainEntity::getReportDate, main.getReportDate())
                    .eq(InpCashMainEntity::getValidFlag, Constant.YES) // 只作废当前有效的
                    .eq(InpCashMainEntity::getHolidayTotalFlag, main.getHolidayTotalFlag())  //对应节假日汇总类型
                    .set(InpCashMainEntity::getValidFlag, Constant.NO)
                    .set(InpCashMainEntity::getUpdateTime, LocalDateTime.now())
                    .update();
            log.info("{} {}  历史报表数据作废完成", Constant.REPORT_NAME_INP,main.getReportDate());

            if (!main.getSubs().isEmpty()) {

                /*
                 * 插入子表数据
                 */
                // 确保子表的关联字段和主表一致
                main.getSubs().forEach(sub -> {
                    sub.setSerialNo(main.getSerialNo());
                    // 如果子表也有创建时间等字段，建议在此补充
                });
                boolean savedBatch = Db.saveBatch(main.getSubs());

                if (savedBatch) {
                    /*
                     * 插入主表数据
                     */
                    boolean saveMain = Db.save(main);

                }
            }
            return 1;
        } catch (Exception e) {
            log.error("插入住院报表数据失败，日期：{}", main.getReportDate(), e);
            throw new RuntimeException("插入住院报表数据失败" + e.getMessage());
        }
    }





    private OutpReportVO calculateTotal(List<OutpReportVO> dtoList, LocalDate reportdate) {
        final BigDecimal ZERO = BigDecimal.ZERO;
        BinaryOperator<BigDecimal> sumOperator = BigDecimal::add;

        OutpReportVO total = new OutpReportVO();
        total.setOperatorNo("sum_total");
        total.setOperatorName("合计");

        Function<Function<OutpReportVO, BigDecimal>, BigDecimal> sumByField =
                getter -> dtoList.stream()
                        .map(getter)
                        .filter(Objects::nonNull)
                        .reduce(ZERO, sumOperator);

        total.setHisAdvancePayment(sumByField.apply(OutpReportVO::getHisAdvancePayment));
        total.setHisMedicalIncome(sumByField.apply(OutpReportVO::getHisMedicalIncome));
        total.setHisRegistrationIncome(sumByField.apply(OutpReportVO::getHisRegistrationIncome));
        total.setRetainedCash(sumByField.apply(OutpReportVO::getRetainedCash));
        total.setReportAmount(sumByField.apply(OutpReportVO::getReportAmount));
        total.setPreviousTemporaryReceipt(sumByField.apply(OutpReportVO::getPreviousTemporaryReceipt));
        total.setHolidayTemporaryReceipt(sumByField.apply(OutpReportVO::getHolidayTemporaryReceipt));

        total.setActualCashAmount(sumByField.apply(OutpReportVO::getActualCashAmount));
        total.setCurrentTemporaryReceipt(sumByField.apply(OutpReportVO::getCurrentTemporaryReceipt));
        total.setRetainedDifference(sumByField.apply(OutpReportVO::getRetainedDifference));
        total.setPettyCash(sumByField.apply(OutpReportVO::getPettyCash));

        total.setRemarks("合计行，不展示在报表中");
        total.setReportDate(reportdate);
        total.setCreateTime(LocalDateTime.now());

        return total;
    }


    /**
     * 住院前端界面保存数据时候，也需要做对应计算
     * @param allSubs 所有子表数据
     * @return 处理后的住院子表数据列表
     */
    public List<InpCashSubEntity> exchangeInpReportData(List<InpCashSubEntity> allSubs) {

        try {
            // 1. 创建一个汇总对象（合计行）
            List<InpCashSubEntity> inpCashSubList = new ArrayList<>();

            if (allSubs == null || allSubs.isEmpty()) {
                return inpCashSubList;
            }

            // 4. 以操作员为主，遍历构建报表数据
            for (InpCashSubEntity inpCashSub : allSubs) {

                // 8 =（2）-（1）+（3）+（4）+（5）+(6)+(7) 今日报表数合计
                inpCashSub.setTodayReportTotal(
                        inpCashSub.getTodayAdvancePayment()
                                .subtract(inpCashSub.getPreviousDayAdvanceReceipt())
                                .add(inpCashSub.getTodaySettlementIncome())
                                .add(inpCashSub.getTodayPreHospitalIncome())
                                .add(inpCashSub.getTrafficAssistanceFund())
                                .add(inpCashSub.getBloodDonationCompensation()
                                        .add(inpCashSub.getReceivablePayable())));

                //（11）=（8）+（9）+（10）-（18） 今日报表应收/应付
                inpCashSub.setTodayReportReceivablePayable(
                        inpCashSub.getTodayReportTotal()
                                .add(inpCashSub.getPreviousDayIOU()
                                        .add(inpCashSub.getTodayIOU()
                                                .add(inpCashSub.getTodayOutpatientIOU()
                                                        .subtract(inpCashSub.getHolidayPayment()))))
                );

                //（14）=（12）+（13） 今日实收现金合计
                inpCashSub.setTodayCashReceivedTotal(
                        inpCashSub.getTodayAdvanceReceipt()
                                .add(inpCashSub.getTodayReportCashReceived()));

                //（15）=（13）-（11）余额
                inpCashSub.setBalance(
                        inpCashSub.getTodayReportCashReceived()
                                .subtract(inpCashSub.getTodayReportReceivablePayable()));

                //（17）=（16）-（15）今日欠条
                inpCashSub.setTodayIOU(inpCashSub.getAdjustment()
                        .subtract(inpCashSub.getBalance()));

                //（20）=（19）-（11）  差额
                inpCashSub.setDifference(inpCashSub.getCashOnHand()
                        .subtract(inpCashSub.getTodayReportReceivablePayable()));

                // 加入结果集
                inpCashSubList.add(inpCashSub);
            }


            return inpCashSubList;

        } catch (Exception e) {
            log.error("住院现金报表转换保存失败!", e);
            return Collections.emptyList();
        }
    }

    /*
     * 转换对应存储的门诊报表数据
     * @param currtDate 当前日期
     * @param list 门诊报表数据列表
     * @return 处理后的门诊报表数据列表
     */
    public List<OutpReportVO> exchangeOutpReportData(LocalDate currtDate, List<OutpReportVO> list) {
        try {
            //查询日期类型
            String holidayType = holidayService.queryDateType(currtDate, Constant.TYPE_OUTP);

            // 1. 获取所有必需的原始数据
            List<YQHolidayEntity> holidays = hiliday.selectByYear(currtDate.getYear());

            Set<LocalDate> holidaySet = holidays.stream()
                    .map(YQHolidayEntity::getHolidayDate)
                    .collect(Collectors.toSet());

            List<YQCashRegRecordEntity> yqRecordList = cash.selectByDate(currtDate);
            Map<String, YQCashRegRecordEntity> cashMap = yqRecordList.stream()
                    .collect(Collectors.toMap(YQCashRegRecordEntity::getOperatorNo, Function.identity(), (v1, v2) -> v1));


            // 3. 构建结果集
            List<OutpReportVO> resultList = new ArrayList<>();

            PrimaryKeyGenerator pks = new PrimaryKeyGenerator();
            String pk = pks.generateKey();

            Integer count = 0;

            // 4. 以操作员为主，遍历构建报表数据
            for (OutpReportVO rpt : list) {
                OutpReportVO currentDto = new OutpReportVO();
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



                if (Constant.HOLIDAY_AFTER.equals(holidayType)) {
                    // 符合条件：执行复杂回溯计算 (A = B - Sum(C) - D)
                    log.info("第{}条，[{}]节假日后正常工作日第一天特殊处理，回溯计算实际报表金额开始......................", count, currtDate);
                    calculateActualReportAmountForMonday(
                            currentDto,
                            currtDate,
                            holidaySet,
                            // 传递的 Lambda 表达式现在接收 LocalDate
                            date -> report.selectReportByDate(date)
                    );
                } else if (Constant.HOLIDAY_IS.equals(holidayType)) {
                    // 节假日逻辑：A = B - C (保持不变)
                    BigDecimal actualReportAmount = currentDto.getReportAmount().subtract(currentDto.getHolidayTemporaryReceipt());
                    currentDto.setActualReportAmount(actualReportAmount);


                } else if (Constant.HOLIDAY_PRE.equals(holidays)) {
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

            log.info("{}门诊报表转换完成，共处理 {} 个操作员", currtDate.toString(), resultList.size());
            return resultList;

        } catch (Exception e) {
            log.error("门诊报表转换失败", e);
            return Collections.emptyList();
        }
    }

    /**
     * 1. 从各数据源获取数据。
     * 2. 以操作员为基准进行匹配和计算。
     * 3. 对周一进行特殊的回溯计算 (A = B - Sum(C) - D)。
     * 4. 对其他工作日进行正常计算 (A = B - C - D)。
     *
     * @return 包含所有操作员计算结果的 ReportDTO 列表
     */
    public List<OutpReportVO> getOutpReportData(LocalDate currtDate) {
        try {
            //查询日期类型
            String holidayType = holidayService.queryDateType(currtDate, Constant.TYPE_OUTP);

            LocalDate preDate = currtDate.minusDays(1);

            // 1. 获取所有必需的原始数据
            List<YQHolidayEntity> holidays = hiliday.selectByYear(currtDate.getYear());
            List<YQOperatorEntity> operators = oper.selectByCategory(Constant.TYPE_OUTP);

            List<YQCashRegRecordEntity> yqRecordList = cash.selectByDate(currtDate);

            // 假设 HIS 接口需要 String，则转换
            List<HisOutpIncomeResponseDTO> hisOutpIncomeResponseDTOList = hisdata.findByDateOutp(currtDate.toString());

            // Mapper 调用传入 LocalDate
            List<OutpReportVO> preOutpReportVO = report.selectReportByDate(preDate);

            // 2. 数据预处理：转换为 Map/Set (保持不变)
            Map<String, HisOutpIncomeResponseDTO> hisDataMap = hisOutpIncomeResponseDTOList.stream()
                    .collect(Collectors.toMap(HisOutpIncomeResponseDTO::getOperatorNo, Function.identity(), (v1, v2) -> v1));

            Map<String, YQCashRegRecordEntity> cashMap = yqRecordList.stream()
                    .collect(Collectors.toMap(YQCashRegRecordEntity::getOperatorNo, Function.identity(), (v1, v2) -> v1));

            Set<LocalDate> holidaySet = holidays.stream()
                    .map(YQHolidayEntity::getHolidayDate)
                    .collect(Collectors.toSet());

            // 3. 构建结果集
            List<OutpReportVO> resultList = new ArrayList<>();

            PrimaryKeyGenerator pks = new PrimaryKeyGenerator();
            String pk = pks.generateKey();

            Integer count = 0;

            // 4. 以操作员为主，遍历构建报表数据
            for (YQOperatorEntity operator : operators) {
                OutpReportVO currentDto = new OutpReportVO();
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
                HisOutpIncomeResponseDTO hisOutpIncomeResponseDTO = hisDataMap.get(operator.getOperatorNo());

                // 2. 从昨日数据 (preReport) 查找操作员的记录 (保持不变)
                OutpReportVO yesterdayOutpReportVO = preOutpReportVO.stream()
                        .filter(r -> operator.getOperatorNo().equals(r.getOperatorNo()))
                        .findFirst()
                        .orElse(null);

                // --- 填充 HIS 收入和 ReportAmount (保持不变) ---
                if (hisOutpIncomeResponseDTO != null) {
                    /*
                    添加结账序号 结账时间 2025.12.31
                     */
                    currentDto.setAcctNo(hisOutpIncomeResponseDTO.getAcctNo());
                    currentDto.setAcctDate(hisOutpIncomeResponseDTO.getAcctDate());

                    currentDto.setHisAdvancePayment(getSafeBigDecimal(hisOutpIncomeResponseDTO.getHisAdvancePayment()));
                    currentDto.setHisMedicalIncome(getSafeBigDecimal(hisOutpIncomeResponseDTO.getHisMedicalIncome()));
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
                if (yesterdayOutpReportVO != null) {

                    currentDto.setPreviousTemporaryReceipt(getSafeBigDecimal(yesterdayOutpReportVO.getCurrentTemporaryReceipt()));

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
                if (Constant.HOLIDAY_AFTER.equals(holidayType)) {
                    // 符合条件：执行复杂回溯计算 (A = B - Sum(C) - D)
                    log.info("第{}条，[{}]节假日后正常工作日第一天特殊处理，回溯计算实际报表金额开始......................", count, currtDate);
                    calculateActualReportAmountForMonday(
                            currentDto,
                            currtDate,
                            holidaySet,
                            // 传递的 Lambda 表达式现在接收 LocalDate
                            date -> report.selectReportByDate(date)
                    );
                } else if (Constant.HOLIDAY_IS.equals(holidayType)) {  //是节假日且是当月的最后一天是否，实收报表是否 需要回溯计算
                    // 节假日逻辑：A = B - C (保持不变)
                    BigDecimal actualReportAmount = currentDto.getReportAmount().subtract(currentDto.getHolidayTemporaryReceipt());
                    currentDto.setActualReportAmount(actualReportAmount);

                } else if (Constant.HOLIDAY_PRE.equals(holidayType)) {
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
                if (Constant.HOLIDAY_AFTER.equals(holidayType)) {
                    // 符合条件：执行复杂回溯计算 (A = B - Sum(C) - D)
                    log.info("第{}条，[{}]节假日后正常工作日第一天特殊处理，回溯计算节假日暂收款金额开始......................", count, currtDate);
                    calculateAHolidayTemporaryReceiptForMonday(
                            currentDto,
                            currtDate,
                            holidaySet,
                            // 传递的 Lambda 表达式现在接收 LocalDate
                            date -> report.selectReportByDate(date)
                    );
                } else if (Constant.HOLIDAY_IS.equals(holidayType)) {
                    // 节假日逻辑且是当月最后一天
                    if (currtDate.getDayOfMonth() == currtDate.lengthOfMonth()) {
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

            log.info("{} {} 生成报表完成，共处理 {} 个操作员", currtDate,Constant.REPORT_NAME_OUTP, resultList.size());
            return resultList;

        } catch (Exception e) {
            log.error("{} {} 报表生成失败", currtDate,Constant.REPORT_NAME_OUTP, e);
            return Collections.emptyList();
        }
    }

    /**
     * 【核心逻辑实现】处理周一的复杂回溯计算
     * 公式: A = B - Sum(C){周末/节假日} - D{周五}
     * C = HolidayTemporaryReceipt (节假日暂收款), D = CurrentTemporaryReceipt (当日暂收款)
     * @param currentDto 当前操作员的报表数据
     * @param targetDate 目标日期 (周一)
     * @param holidaySet 节假日集合
     * @param reportQueryFunction 用于查询历史报表数据的函数 (接收 LocalDate 参数)
     */
    private void calculateActualReportAmountForMonday(
            OutpReportVO currentDto,
            LocalDate targetDate,
            Set<LocalDate> holidaySet,
            // ❗ 修正点 11: 将 Function 的输入类型改为 LocalDate
            Function<LocalDate, List<OutpReportVO>> reportQueryFunction) {

        BigDecimal totalC = BigDecimal.ZERO;
        BigDecimal dAmountFriday = BigDecimal.ZERO;
        LocalDate currentDate = targetDate.minusDays(1); // 从周日开始回溯


        //当前传入日期为当月第一天
        if (targetDate.getDayOfMonth() != 1) {

            // 2. 回溯循环
            while (true) {

                // 2.1 获取当前回溯日期的历史报表数据 (通过 Mapper)
                List<OutpReportVO> historicalOutpReportVOS = reportQueryFunction.apply(currentDate);

                // 2.2 查找当前操作员在历史报表中的记录 (确保用户匹配)
                Optional<OutpReportVO> historicalDtoOpt = historicalOutpReportVOS.stream()
                        .filter(r -> currentDto.getOperatorNo().equals(r.getOperatorNo()))
                        .findFirst();

                // 提取当前的节假日的金额 (HolidayTemporaryReceipt)
                BigDecimal cAmount = historicalDtoOpt
                        .map(OutpReportVO::getHolidayTemporaryReceipt)
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
                    // 周五的 C (HolidayTemporaryReceipt) 也累加进去
                    totalC = totalC.add(getSafeBigDecimal(cAmount));

                    // 提取 D 金额 (CurrentTemporaryReceipt)
                    dAmountFriday = historicalDtoOpt
                            .map(OutpReportVO::getCurrentTemporaryReceipt)
                            .orElse(BigDecimal.ZERO); // 缺失数据默认为 0
                    break; // 跳出循环
                }

                // 防止无限循环
                if (targetDate.toEpochDay() - currentDate.toEpochDay() > 30) {
                    log.warn("回溯查找失败，连续节假日超过30天，从{}开始，到 {} 之间无法找到正常工作日。", currentDate, targetDate);
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
            OutpReportVO currentDto,
            LocalDate targetDate,
            Set<LocalDate> holidaySet,
            // 将 Function 的输入类型改为 LocalDate
            Function<LocalDate, List<OutpReportVO>> reportQueryFunction) {

        BigDecimal totalC = BigDecimal.ZERO;
        BigDecimal dAmountFriday = BigDecimal.ZERO;
        LocalDate currentDate = targetDate.minusDays(1); // 从周日开始回溯


        //当前传入日期为当月第一天
        if (targetDate.getDayOfMonth() != 1) {

            // 2. 回溯循环
            while (true) {

                // 2.1 获取当前回溯日期的历史报表数据 (通过 Mapper)
                List<OutpReportVO> historicalOutpReportVOS = reportQueryFunction.apply(currentDate);

                // 2.2 查找当前操作员在历史报表中的记录 (确保用户匹配)
                Optional<OutpReportVO> historicalDtoOpt = historicalOutpReportVOS.stream()
                        .filter(r -> currentDto.getOperatorNo().equals(r.getOperatorNo()))
                        .findFirst();

                // 提取当前的节假日的金额 (HolidayTemporaryReceipt)
                BigDecimal cAmount = historicalDtoOpt
                        .map(OutpReportVO::getHolidayTemporaryReceipt)
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
                    // 将周五的 C (HolidayTemporaryReceipt) 也累加进去
                    totalC = totalC.add(getSafeBigDecimal(cAmount));
                    break; // 跳出循环
                }

                // 安全检查：防止无限循环
                if (targetDate.toEpochDay() - currentDate.toEpochDay() > 30) {
                    log.warn("回溯查找失败，连续节假日超过30天，从{}开始，到 {} 之间无法找到正常工作日。", currentDate, targetDate);
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