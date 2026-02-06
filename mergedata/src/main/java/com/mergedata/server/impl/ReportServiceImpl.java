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
    CashMapper cashMapper;

    @Autowired
    OperatorMapper operatorMapper;

    @Autowired
    HolidayMapper holidayMapper;

    @Autowired
    OutpReportMapper outpReportMapper;

    @Autowired
    YQHolidayService holidayService;


     /**
      * 获取门诊报表数据
      * @param body 门诊报表请求体
      * @return 门诊报表数据列表
      */
    @Override
    public List<OutpReportVO> getOutpReport(OutpReportRequestBody body) {


        //调用存储过程获取报表数据
        try {

            List<OutpReportVO> outpResults = outpReportMapper.findReport(body);
            LocalDate currentDate = body.getReportDate();
            //接收ExtendParams1为true时，即初始化报表
            Boolean isInitFlag ="true".equalsIgnoreCase(body.getExtendParams1());

            // 判断结果集，判断是否平台有无数据，有则查询出返回，无则调用接口获取数据并返回
            if (outpResults.isEmpty() || isInitFlag) {
                outpResults = getOutpReportData(currentDate);
                //查询时候数据库没有相关的数据，插入数据库，此处调用 firstInsert 方法批量插入数据
                isInitInsertOutp(outpResults, currentDate);
            }

//        results.add(calculateTotal(results, LocalDate.parse(reportdate)));
        // 进行筛选
        return outpResults.stream()
                .filter(r -> (body.getInpWindow() == null || !body.getInpWindow().equals(1) || Integer.valueOf(1).equals(r.getInpWindow())))
                .filter(r -> (body.getAtm() == null || !body.getAtm().equals(1) || Integer.valueOf(1).equals(r.getAtm())))
                .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("获取报表数据异常", e);
            throw new RuntimeException("获取报表数据异常");
        }
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
           if (!outpReportMapper.selectReportByDate(reportDate).isEmpty()) {
               outpReportMapper.updateByDate(reportDate);
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
           int mainCount = outpReportMapper.batchInsertList(mainList);

           int subCount = 0;
           if (!subList.isEmpty()) {
               subCount = outpReportMapper.batchInsertSubList(subList);
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
     * 统一计算核心：将 VO 的计算逻辑抽离，确保 get 和 exchange 共用逻辑
     */
    private void computeOutpFields(OutpReportVO dto, LocalDate currtDate, String holidayType,
                                   Set<LocalDate> holidaySet, Map<LocalDate, Map<String, OutpReportVO>> cache) {

        // 执行回溯计算逻辑   1. 执行回溯计算逻辑 (A = B - Sum(C) - D)
        handleOutpBacktrackLogic(dto, currtDate, holidayType, holidaySet, cache);

        // 5.实收现金数 = 实收报表数 + 当日暂收款
        dto.setActualCashAmount(getSafeBigDecimal(dto.getActualReportAmount()).add(getSafeBigDecimal(dto.getCurrentTemporaryReceipt())));

        // 6.留存数差额 = 留存现金 - 备用金 + 实收报表数
        dto.setRetainedDifference(getSafeBigDecimal(dto.getRetainedCash())
                .subtract(getSafeBigDecimal(dto.getPettyCash()))
                .add(getSafeBigDecimal(dto.getActualReportAmount())));
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

        if (allMains == null || allMains.isEmpty()) {
            return summary;
        }

        InpCashSubEntity totalSub = new InpCashSubEntity();

        List<InpCashSubEntity> allSubs = allMains.stream()
                .flatMap(m -> m.getSubs().stream()).collect(Collectors.toList());


        for (InpCashSubEntity item : allSubs) {
            // 上午部分
            totalSub.setPreviousDayAdvanceReceipt(totalSub.getPreviousDayAdvanceReceipt().add(item.getPreviousDayAdvanceReceipt()));
            totalSub.setTodayAdvancePayment(totalSub.getTodayAdvancePayment().add(item.getTodayAdvancePayment()));
            totalSub.setTodaySettlementIncome(totalSub.getTodaySettlementIncome().add(item.getTodaySettlementIncome()));
            totalSub.setTodayPreHospitalIncome(totalSub.getTodayPreHospitalIncome().add(item.getTodayPreHospitalIncome()));
            totalSub.setTrafficAssistanceFund(totalSub.getTrafficAssistanceFund().add(item.getTrafficAssistanceFund()));
            totalSub.setBloodDonationCompensation(totalSub.getBloodDonationCompensation().add(item.getBloodDonationCompensation()));
            totalSub.setReceivablePayable(totalSub.getReceivablePayable().add(item.getReceivablePayable()));
            totalSub.setTodayReportTotal(totalSub.getTodayReportTotal().add(item.getTodayReportTotal()));
            totalSub.setPreviousDayIOU(totalSub.getPreviousDayIOU().add(item.getPreviousDayIOU()));
            totalSub.setTodayOutpatientIOU(totalSub.getTodayOutpatientIOU().add(item.getTodayOutpatientIOU()));
            totalSub.setTodayReportReceivablePayable(totalSub.getTodayReportReceivablePayable().add(item.getTodayReportReceivablePayable()));

            // 下午及留存部分
            totalSub.setTodayAdvanceReceipt(totalSub.getTodayAdvanceReceipt().add(item.getTodayAdvanceReceipt()));
            totalSub.setTodayReportCashReceived(totalSub.getTodayReportCashReceived().add(item.getTodayReportCashReceived()));
            totalSub.setTodayCashReceivedTotal(totalSub.getTodayCashReceivedTotal().add(item.getTodayCashReceivedTotal()));
            totalSub.setBalance(totalSub.getBalance().add(item.getBalance()));
            totalSub.setAdjustment(totalSub.getAdjustment().add(item.getAdjustment()));
            totalSub.setTodayIOU(totalSub.getTodayIOU().add(item.getTodayIOU()));
            totalSub.setHolidayPayment(totalSub.getHolidayPayment().add(item.getHolidayPayment()));

            // 收费员留存部分
            totalSub.setCashOnHand(totalSub.getCashOnHand().add(item.getCashOnHand()));
            totalSub.setDifference(totalSub.getDifference().add(item.getDifference()));

        }

        summary.setHolidayTotalFlag(Constant.YES);
        summary.setReportDate(reportDate);
        summary.setReportYear(reportDate.getYear());
        summary.setSubs(Collections.singletonList(totalSub)); // 返回单条汇总结果

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
            List<YQHolidayEntity> holidays = holidayMapper.selectByYear(currtDate.getYear());
            List<YQOperatorEntity> operators = operatorMapper.selectByCategory(Constant.TYPE_INP);
            List<YQCashRegRecordEntity> yqRecordList = cashMapper.selectByDate(currtDate);

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

                // 计算其他字段
                calculateInpSubEntityFields(inpCashSub);

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
            if (!outpReportMapper.selectReportByDate(date).isEmpty()) {
                outpReportMapper.updateByDate(date);
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
                });

                //不能用savedBatch 作为判断情况
                boolean savedBatch = Db.saveBatch(main.getSubs());

                /*
                 * 插入主表数据
                 */
                boolean saveMain = Db.save(main);

            }

            log.info("{} {}  报表数据保存成功！", Constant.REPORT_NAME_INP,main.getReportDate());
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
            if (allSubs == null || allSubs.isEmpty()) return new ArrayList<>();

            allSubs.forEach(this::calculateInpSubEntityFields);
            return allSubs;

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
            Set<LocalDate> holidaySet = getHolidaySet(currtDate.getYear());
            Map<LocalDate, Map<String, OutpReportVO>> historyCache = new HashMap<>();


            // 4. 以操作员为主，遍历构建报表数据
            for (OutpReportVO dto  : list) {
                computeOutpFields(dto, currtDate, holidayType, holidaySet, historyCache);
            }

            log.info("{}门诊报表转换完成，共处理 {} 个操作员", currtDate.toString(), list.size());
            return list;

        } catch (Exception e) {
            log.error("门诊报表转换失败", e);
            return Collections.emptyList();
        }
    }



    // 示例：为住院子表提取一个计算方法
    private void calculateInpSubEntityFields(InpCashSubEntity inpCashSub) {

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
                        .add(inpCashSub.getTodayOutpatientIOU())
                        .subtract(inpCashSub.getHolidayPayment())));

        //（14）=（12）+（13） 今日实收现金合计
        inpCashSub.setTodayCashReceivedTotal(
                inpCashSub.getTodayAdvanceReceipt()
                        .add(inpCashSub.getTodayReportCashReceived()));

        //（15）=（13）-（11）余额
        inpCashSub.setBalance(
                inpCashSub.getTodayReportCashReceived()
                        .subtract(inpCashSub.getTodayReportReceivablePayable()));

        //（17）=（16）-（15）今日欠条
        inpCashSub.setTodayIOU(
                inpCashSub.getAdjustment()
                    .subtract(inpCashSub.getBalance()));

        //（20）=（19）-（11）  差额
        inpCashSub.setDifference(
                inpCashSub.getCashOnHand()
                .subtract(inpCashSub.getTodayReportReceivablePayable()));

    }

    //  结果类来封装回溯计算的结果
    private static class BacktrackResult {
        BigDecimal totalHolidayReceipts = BigDecimal.ZERO; // 对应 totalC
        BigDecimal lastWorkdayReceipt = BigDecimal.ZERO;   // 对应 dAmountFriday
    }

    /** 获取门诊报表---优化了回溯查询
     * 1. 从各数据源获取数据。
     * 2. 以操作员为基准进行匹配和计算。
     * 3. 对周一进行特殊的回溯计算 (A = B - Sum(C) - D)。
     * 4. 对其他工作日进行正常计算 (A = B - C - D)。
     *
     * @return 包含所有操作员计算结果的 ReportDTO 列表
     */
    public List<OutpReportVO> getOutpReportData(LocalDate currtDate) {
        try {
            String holidayType = holidayService.queryDateType(currtDate, Constant.TYPE_OUTP);
            List<YQOperatorEntity> operators = operatorMapper.selectByCategory(Constant.TYPE_OUTP);
            Set<LocalDate> holidaySet = holidayMapper.selectByYear(currtDate.getYear()).stream()
                    .map(YQHolidayEntity::getHolidayDate).collect(Collectors.toSet());


            // 预加载 HIS 数据和现金记录
            Map<String, HisOutpIncomeResponseDTO> hisDataMap = hisdata.findByDateOutp(currtDate.toString()).stream()
                    .collect(Collectors.toMap(HisOutpIncomeResponseDTO::getOperatorNo, Function.identity(), (v1, v2) -> v1));
            Map<String, YQCashRegRecordEntity> cashMap = cashMapper.selectByDate(currtDate).stream()
                    .collect(Collectors.toMap(YQCashRegRecordEntity::getOperatorNo, Function.identity(), (v1, v2) -> v1));
            // 获取历史数据（昨日）
            Map<String, OutpReportVO> yesterdayMap = outpReportMapper.selectReportByDate(currtDate.minusDays(1)).stream()
                    .collect(Collectors.toMap(OutpReportVO::getOperatorNo, Function.identity(), (v1, v2) -> v1));


            // 增加历史日期查询缓存，避免 N+1 问题 ---
            Map<LocalDate, Map<String, OutpReportVO>> historyCache = new HashMap<>();
            List<OutpReportVO> resultList = new ArrayList<>();
            String batchPk = new PrimaryKeyGenerator().generateKey();

            for (YQOperatorEntity operator : operators) {
                OutpReportVO dto = new OutpReportVO();
                dto.setSerialNo(batchPk);
                dto.setOperatorNo(operator.getOperatorNo());
                dto.setOperatorName(operator.getOperatorName());
                dto.setReportDate(currtDate);

                // 1. 基础 HIS 收入赋值
                HisOutpIncomeResponseDTO hisDto = hisDataMap.get(operator.getOperatorNo());
                if (hisDto != null) {
                    dto.setHisAdvancePayment(getSafeBigDecimal(hisDto.getHisAdvancePayment()));
                    dto.setHisMedicalIncome(getSafeBigDecimal(hisDto.getHisMedicalIncome()));
                    dto.setReportAmount(dto.getHisAdvancePayment().add(dto.getHisMedicalIncome()));
                    dto.setAcctNo(hisDto.getAcctNo());
                    dto.setAcctDate(hisDto.getAcctDate());
                } else {
                    dto.setReportAmount(BigDecimal.ZERO);
                }

                // 填充昨日和现金记录
                OutpReportVO yest = yesterdayMap.get(operator.getOperatorNo());
                dto.setPreviousTemporaryReceipt(yest != null ? getSafeBigDecimal(yest.getCurrentTemporaryReceipt()) : BigDecimal.ZERO);
                YQCashRegRecordEntity cashRec = cashMap.get(operator.getOperatorNo());
                dto.setRetainedCash(cashRec != null ? getSafeBigDecimal(cashRec.getRetainedCash()) : BigDecimal.ZERO);

                //统一计算核心 自动处理回溯缓存
                computeOutpFields(dto, currtDate, holidayType, holidaySet, historyCache);

                resultList.add(dto);
            }
            return resultList;
        } catch (Exception e) {
            log.error("门诊报表生成失败", e);
            return Collections.emptyList();
        }
    }


    /**
     * 封装回溯逻辑，使用缓存减少数据库IO
     */
    private void handleOutpBacktrackLogic(OutpReportVO dto, LocalDate targetDate, String holidayType,
                                          Set<LocalDate> holidaySet, Map<LocalDate, Map<String, OutpReportVO>> cache) {

        if (Constant.HOLIDAY_AFTER.equals(holidayType)) {
            // 回溯计算 A = B - Sum(C) - D
            BacktrackResult res = executeBacktrack(dto.getOperatorNo(), targetDate, holidaySet, cache);
            dto.setActualReportAmount(dto.getReportAmount().subtract(res.totalHolidayReceipts).subtract(res.lastWorkdayReceipt));
            dto.setHolidayTemporaryReceipt(res.totalHolidayReceipts);
        } else if (Constant.HOLIDAY_IS.equals(holidayType)) {
            dto.setActualReportAmount(dto.getReportAmount().subtract(getSafeBigDecimal(dto.getHolidayTemporaryReceipt())));
            // 节假日且是月末需特殊处理...
        } else if (Constant.HOLIDAY_PRE.equals(holidayType)) {
            dto.setActualReportAmount(dto.getReportAmount().subtract(dto.getPreviousTemporaryReceipt()).subtract(getSafeBigDecimal(dto.getCurrentTemporaryReceipt())));
        } else {
            dto.setActualReportAmount(dto.getReportAmount());
            dto.setHolidayTemporaryReceipt(BigDecimal.ZERO);
        }
    }


    private BacktrackResult executeBacktrack(String opNo, LocalDate targetDate, Set<LocalDate> holidaySet,
                                             Map<LocalDate, Map<String, OutpReportVO>> cache) {
        BacktrackResult result = new BacktrackResult();
        LocalDate current = targetDate.minusDays(1);

        while (targetDate.toEpochDay() - current.toEpochDay() <= 30 && current.getDayOfMonth() >= 1) {
            // 从缓存获取，没命中则查数据库
            Map<String, OutpReportVO> dayData = cache.computeIfAbsent(current,
                    date -> outpReportMapper.selectReportByDate(date).stream()
                            .collect(Collectors.toMap(OutpReportVO::getOperatorNo, Function.identity(), (v1,v2)->v1)));

            OutpReportVO hist = dayData.get(opNo);
            BigDecimal c = hist != null ? getSafeBigDecimal(hist.getHolidayTemporaryReceipt()) : BigDecimal.ZERO;

            if (holidaySet.contains(current)) {
                result.totalHolidayReceipts = result.totalHolidayReceipts.add(c);
                if (current.getDayOfMonth() == 1) break;
                current = current.minusDays(1);
            } else {
                // 找到工作日（周五）
                result.totalHolidayReceipts = result.totalHolidayReceipts.add(c);
                result.lastWorkdayReceipt = hist != null ? getSafeBigDecimal(hist.getCurrentTemporaryReceipt()) : BigDecimal.ZERO;
                break;
            }
        }
        return result;
    }



    /**
     * 判断日期是否为节假日 (使用 Set 版本)
     */
    private Set<LocalDate> getHolidaySet(int year) {
        return holidayMapper.selectByYear(year).stream()
                .map(YQHolidayEntity::getHolidayDate).collect(Collectors.toSet());
    }

    /**
     * 安全获取 BigDecimal 值，如果为 null 则返回 BigDecimal.ZERO
     */
    private BigDecimal getSafeBigDecimal(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }


}