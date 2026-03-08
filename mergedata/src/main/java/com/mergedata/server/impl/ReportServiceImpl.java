package com.mergedata.server.impl;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.mergedata.constants.Constant;
import com.mergedata.mapper.HolidayMapper;
import com.mergedata.mapper.OperatorMapper;
import com.mergedata.mapper.OutpReportMapper;
import com.mergedata.model.dto.InpReportRequestBody;
import com.mergedata.model.dto.OutpReportRequestBody;
import com.mergedata.model.dto.external.HisInpIncomeResponseDTO;
import com.mergedata.model.dto.external.HisOutpIncomeResponseDTO;
import com.mergedata.model.entity.*;
import com.mergedata.model.vo.OutpReportMainVO;
import com.mergedata.model.vo.OutpReportSubVO;
import com.mergedata.server.*;
import com.mergedata.util.PrimaryKeyGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

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
    YQCashService cashService;

    @Autowired
    OperatorMapper operatorMapper;

    @Autowired
    HolidayMapper holidayMapper;
    @Autowired
    OutpReportService outpReportService;

    @Autowired
    OutpReportMapper outpReportMapper;

    @Autowired
    YQHolidayService holidayService;

    @Autowired
    YQOperatorService operatorService;

    @Autowired
    private TransactionTemplate transactionTemplate;

    /**
     * 获取门诊报表数据
     *
     * @param body 门诊报表请求体
     * @return 门诊报表数据列表
     */
    @Override
    public OutpReportMainVO getOutpReport(OutpReportRequestBody body) {


        try {
            //查询数据库是否有数据
            OutpCashMainEntity mainEntity = outpReportService.findByDate(body.getReportDate());

            OutpCashMainEntity main = new OutpCashMainEntity();


            List<OutpReportSubVO> subList = new ArrayList<>();

            LocalDate currentDate = body.getReportDate();
            //接收ExtendParams1为true时，即初始化报表
            Boolean isInitFlag = "true".equalsIgnoreCase(body.getExtendParams1());

            // 判断结果集，判断是否平台有无数据，有则查询出返回，无则调用接口获取数据并返回
            if (mainEntity == null || isInitFlag) {


                List<OutpCashSubEntity> list = getOutpReportData(body);

                //无效查询，返回空列表
                if (list.isEmpty() || list.size() == 0) {
                    return null;
                }

                  //查询时候数据库没有相关的数据，插入数据库，此处调用 firstInsert 方法批量插入数据
                 main.setReportDate(currentDate);
                 main.setTotalFlag(body.getTotalFlag());
                 main.setValidFlag("1");
                 main.setCreateTime(LocalDateTime.now());
                 main.setSerialNo(PrimaryKeyGenerator.generateKey());
                 main.setSubs(list);

                 outpReportService.insert(main);
            }


            OutpReportMainVO mainVO = outpExchangeDbToView(main);

            subList = mainVO.getSubList().stream()
                    .filter(r -> (body.getInpWindow() == null || !body.getInpWindow().equals(1) || Integer.valueOf(1).equals(r.getInpWindow())))
                    .filter(r -> (body.getAtm() == null || !body.getAtm().equals(1) || Integer.valueOf(1).equals(r.getAtm())))
                    .collect(Collectors.toList());

            mainVO.setTotalFlag(body.getTotalFlag());
            mainVO.setSubList(subList);

            // 进行筛选
            return mainVO;

        } catch (Exception e) {
            log.error("获取报表数据异常", e);
            throw new RuntimeException("获取报表数据异常");
        }
    }



    /**
     * 批量插入门诊报表数据
     *
     * @param mainVO 门诊报表数据列表
     * @return 是否成功
     */
    @Override
    public Integer insertOutpReport(OutpReportMainVO mainVO) {

        // ----- 参数校验（无事务）-----
        if (mainVO.getSubList().size() == 0 || mainVO.getSubList().isEmpty()) {
            return Constant.FAILURE;
        }

        LocalDate reportDate = mainVO.getSubList().get(0).getReportDate();

        String remark = "";
        for (OutpReportSubVO sub : mainVO.getSubList()) {
            if (sub.getOperatorName().equals("当日暂收款")) {
                remark = sub.getRemarks();
            }
        }


        //门诊接收的转换为对应实体类保存到数据库
        OutpCashMainEntity main = new OutpCashMainEntity();

        main = outpExchangeViewToDb(reportDate, mainVO.getTotalFlag(), remark, mainVO.getSubList());

        //转换为实体类的数据值需要验证，防止写入的数据有非修改的 而改动 校验方法
        //明细数据校验方法
        ouptInsertVerityDetailData(reportDate,main.getSubs());


        try {
            //先作废目前已经有的报表数据
            outpReportService.updateByDate(reportDate, mainVO.getTotalFlag());
            //在写入数据库
            outpReportService.insert(main);

            return Constant.SUCCESS;

        } catch (Exception e) {
            log.error("插入门诊报表数据异常", e);
            throw new RuntimeException("插入门诊报表数据异常", e);
        }
    }
    /**
     * 门诊明细数据写入前校验方法
     */
    private void ouptInsertVerityDetailData(LocalDate localDate,List<OutpCashSubEntity> subs) {
        // 校验明细数据


    }


    /**
     * 实际的数据库操作（带事务）
     */
    private Integer insertWithTransaction(LocalDate reportDate, List<OutpReportSubVO> list, String totalFlag) {

        // 2. 生成主键
        String pk = new PrimaryKeyGenerator().generateKey();

        // 3. 构建主表
//        OutpCashMainEntity main = buildMainEntity(pk, list.get(0),totalFlag);

        // 4. 构建明细表
        List<OutpCashSubEntity> subList = buildSubEntities(pk, list);

        // 2. 显式开启事务
        return transactionTemplate.execute(status -> {
            try {
                // 检测事务为 true
                System.out.println("是否开启事务: " + status.isNewTransaction());

                // 1. 作废历史
                if (!outpReportMapper.selectReportByDate(reportDate).isEmpty()) {
                    outpReportMapper.updateByDate(reportDate);
                    log.info("{} {} 历史报表数据作废完成", reportDate, Constant.REPORT_NAME_OUTP);
                }

//                Db.save(main);

                Db.saveBatch(subList);

                return Constant.SUCCESS;

            } catch (Exception e) {
                status.setRollbackOnly(); // 手动回滚
                log.error("数据库操作失败，执行回滚", e);
                throw new RuntimeException("门诊现金报表写入失败，已回滚", e);
            }
        });

    }


    /**
     * 门诊报表数据实体转换视图
     */
    private OutpReportMainVO outpExchangeDbToView(OutpCashMainEntity mainEntity) {

        OutpReportMainVO main = new OutpReportMainVO();
        main.setTotalFlag(main.getTotalFlag());

        // 转换 List
        if (CollectionUtils.isNotEmpty(mainEntity.getSubs())) {
            List<OutpReportSubVO> subList = mainEntity.getSubs().stream().map(subEntity -> {
                OutpReportSubVO subVO = new OutpReportSubVO();
                BeanUtils.copyProperties(subEntity, subVO); // 这一步才是正确的：VO到VO，或Entity到VO
                return subVO;
            }).collect(Collectors.toList());
            main.setSubList(subList);
        }
        return main;
    }


    /**
     * 门诊报表数据视图转换实体类
     */
    private OutpCashMainEntity outpExchangeViewToDb(LocalDate reportDate, String totalFlag, String remark, List<OutpReportSubVO> vo) {

        OutpCashMainEntity main = new OutpCashMainEntity();

        main.setSerialNo(PrimaryKeyGenerator.generateKey());
        main.setReportDate(reportDate);
        main.setReportYear(reportDate.getYear());
        main.setTotalFlag(totalFlag);
        main.setValidFlag(Constant.YES);
        main.setRemark(remark);
        main.setCreateTime(LocalDateTime.now());

        OutpCashSubEntity sub = new OutpCashSubEntity();
        BeanUtils.copyProperties(vo, sub);
        main.setSubs(Collections.singletonList(sub));

        return main;
    }

    private List<OutpCashSubEntity> buildSubEntities(String pk, List<OutpReportSubVO> list) {
        List<OutpCashSubEntity> subList = new ArrayList<>();

        for (OutpReportSubVO vo : list) {
            if (vo.getOperatorName().contains("合计")) {
                continue;  // 跳过合计行
            }

            OutpCashSubEntity sub = new OutpCashSubEntity();
            BeanUtils.copyProperties(vo, sub);

            sub.setSerialNo(pk);
            sub.setDbUser(vo.getDbUser());
            sub.setHisOperatorName(vo.getOperatorName());
            sub.setRowNum(vo.getRowNum());
            sub.setAcctDate(vo.getAcctDate());
            sub.setAcctNo(vo.getAcctNo());
            sub.setRemarks(vo.getRemarks());

            subList.add(sub);
        }

        return subList;
    }


    /**
     * 获取住院报表数据
     *
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
                        if (inpResult == null) {
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
    private void computeOutpFields(OutpCashSubEntity dto, LocalDate currtDate, String holidayType,
                                   Set<LocalDate> holidaySet, Map<LocalDate, Map<String, OutpCashSubEntity>> cache) {

        handleOutpBacktrackLogic(dto, currtDate, holidayType, holidaySet, cache);

//        // 5.实收现金数 = 实收报表数 + 当日暂收款
//        dto.setActualCashAmount(getSafeBigDecimal(dto.getActualReportAmount()).add(getSafeBigDecimal(dto.getCurrentTemporaryReceipt())));
//
//        // 6.留存数差额 = 留存现金 - 备用金 + 实收报表数
//        dto.setRetainedDifference(getSafeBigDecimal(dto.getRetainedCash())
//                .subtract(getSafeBigDecimal(dto.getPettyCash()))
//                .add(getSafeBigDecimal(dto.getActualReportAmount())));
    }


    /**
     * 对住院现金统计主表实体类进行节假日汇总
     *
     * @param allMains   所有住院现金统计主表实体类列表
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
     *
     * @param date             日期
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
     *
     * @param currtDate        报表日期
     * @param holidayType      节假日类型
     * @param holidayTotalFlag 节假日汇总标志 0：非节假日汇总 1：节假日汇总
     * @return 住院现金统计
     */
    public InpCashMainEntity getInpReportData(LocalDate currtDate, String holidayType, String holidayTotalFlag) {
        try {
            LocalDate preDate = currtDate.minusDays(1);

            // 1. 获取所有必需的原始数据
            List<YQHolidayEntity> holidays = holidayService.findByYear(currtDate.getYear());
            List<YQOperatorEntity> operators = operatorService.findByCategory(Constant.TYPE_INP);
            List<YQCashRegRecordEntity> yqRecordList = cashService.findByDate(currtDate);

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
                    .collect(Collectors.toMap(HisInpIncomeResponseDTO::getDbUser, Function.identity(), (v1, v2) -> v1));

            Map<String, YQCashRegRecordEntity> cashMap = yqRecordList.stream()
                    .collect(Collectors.toMap(YQCashRegRecordEntity::getDbUser, Function.identity(), (v1, v2) -> v1));

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
                inpCashSub.setDbUser(operator.getDbUser());
                inpCashSub.setOperatorName(operator.getOperatorName());
                inpCashSub.setCreatedTime(LocalDateTime.now());

                // =========================================================================
                // 基础信息赋值区域
                // =========================================================================

                // 1. 获取当前操作员的 HIS 收入数据 (保持不变)
                HisInpIncomeResponseDTO hisInpIncomeResponseDTO = hisDataMap.get(operator.getDbUser());

                // 2. 从昨日数据 (preReport) 查找操作员的记录 (保持不变)
                InpCashSubEntity yesterdayOutpReportVO = preInpReportSub.stream()
                        .filter(r -> operator.getDbUser().equals(r.getDbUser()))
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

                YQCashRegRecordEntity cashRecord = cashMap.get(operator.getDbUser());

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
    public void buildInitInsertOutp(List<OutpReportSubVO> list, OutpReportRequestBody body) {
        try {


            insertWithTransaction(body.getReportDate(), list, body.getTotalFlag());
        } catch (Exception e) {
            log.error("初始化插入门诊现金主表数据失败，日期：{}", body.getReportDate(), e);
            throw new RuntimeException("初始化插入门诊现金主表数据失败" + e.getMessage());
        }
    }



    /**
     * 初始化插入住院现金主表数据
     *
     * @param main       住院现金主表实体
     * @param isInitFlag 是否初次写入标志 ，默认值为"1"，表示初次写入
     * @return 插入成功的记录数
     */
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

        //开启显式事务 不用注解事务
        return transactionTemplate.execute(status -> {
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

                log.info("{} {}  历史报表数据作废完成", Constant.REPORT_NAME_INP, main.getReportDate());

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
                log.info("{} {}  报表数据保存成功！", Constant.REPORT_NAME_INP, main.getReportDate());
                return Constant.SUCCESS;

            } catch (Exception e) {
                log.error("插入住院报表数据失败，日期：{}", main.getReportDate(), e);
                throw new RuntimeException("插入住院报表数据失败,已回滚" + e.getMessage());
            }
        });
    }


    private OutpReportSubVO calculateTotal(List<OutpReportSubVO> dtoList, LocalDate reportdate) {
        final BigDecimal ZERO = BigDecimal.ZERO;
        BinaryOperator<BigDecimal> sumOperator = BigDecimal::add;

        OutpReportSubVO total = new OutpReportSubVO();
        total.setOperatorNo("sum_total");
        total.setOperatorName("合计");

        Function<Function<OutpReportSubVO, BigDecimal>, BigDecimal> sumByField =
                getter -> dtoList.stream()
                        .map(getter)
                        .filter(Objects::nonNull)
                        .reduce(ZERO, sumOperator);

        total.setHisAdvancePayment(sumByField.apply(OutpReportSubVO::getHisAdvancePayment));
        total.setHisMedicalIncome(sumByField.apply(OutpReportSubVO::getHisMedicalIncome));
        total.setHisRegistrationIncome(sumByField.apply(OutpReportSubVO::getHisRegistrationIncome));
        total.setRetainedCash(sumByField.apply(OutpReportSubVO::getRetainedCash));
        total.setReportAmount(sumByField.apply(OutpReportSubVO::getReportAmount));
        total.setPreviousTemporaryReceipt(sumByField.apply(OutpReportSubVO::getPreviousTemporaryReceipt));
        total.setHolidayTemporaryReceipt(sumByField.apply(OutpReportSubVO::getHolidayTemporaryReceipt));

        total.setActualCashAmount(sumByField.apply(OutpReportSubVO::getActualCashAmount));
        total.setCurrentTemporaryReceipt(sumByField.apply(OutpReportSubVO::getCurrentTemporaryReceipt));
        total.setRetainedDifference(sumByField.apply(OutpReportSubVO::getRetainedDifference));
        total.setPettyCash(sumByField.apply(OutpReportSubVO::getPettyCash));

        total.setRemarks("合计行，不展示在报表中");
        total.setReportDate(reportdate);
        total.setCreateTime(LocalDateTime.now());

        return total;
    }


    /**
     * 住院前端界面保存数据时候，也需要做对应计算
     *
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
    public List<OutpCashSubEntity> exchangeOutpReportData(LocalDate currtDate, List<OutpCashSubEntity> list) {
        try {
            //查询日期类型
            String holidayType = holidayService.queryDateType(currtDate, Constant.TYPE_OUTP);
            Set<LocalDate> holidaySet = getHolidaySet(currtDate.getYear());
            Map<LocalDate, Map<String, OutpCashSubEntity>> historyCache = new HashMap<>();


            // 4. 以操作员为主，遍历构建报表数据
            for (OutpCashSubEntity dto : list) {
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
        BigDecimal backReportAmount = BigDecimal.ZERO;  // 应交报表数
        BigDecimal backPreviousTemporaryReceipt = BigDecimal.ZERO;   // 前日暂收款
    }

    /**
     * 获取门诊报表---优化了回溯查询
     * 1. 从各数据源获取数据。
     * 2. 以操作员为基准进行匹配和计算。
     * 3. 对周一进行特殊的回溯计算 (A = B - Sum(C) - D)。
     * 4. 对其他工作日进行正常计算 (A = B - C - D)。
     *
     * @param body 日期
     * @return 包含所有操作员计算结果的 ReportDTO 列表
     */
    public List<OutpCashSubEntity> getOutpReportData(OutpReportRequestBody body) {
        try {
            LocalDate currtDate = body.getReportDate();


            String holidayType = holidayService.queryDateType(body.getReportDate(), Constant.TYPE_OUTP);

            /*
            先判断是否是汇总  还是单独查询
            1、先判断是否是汇总标志
                1、1  如果是汇总标志，再判断是否是特殊节假日
                1、2  是汇总，但是不符合 特殊节假日  直接返回空列表

            */
            boolean isSpecialHoliday = false;
            if (Constant.YES.equals(body.getTotalFlag())) {
                if (Constant.HOLIDAY_AFTER.equals(holidayType) || Constant.HOLIDAY_MONTH_LASTDAY.equals(holidayType)) {
                    isSpecialHoliday = true;
                } else {
                    return Collections.emptyList();
                }
            }


            List<YQOperatorEntity> operators = operatorService.findByCategory(Constant.TYPE_OUTP);

            Set<LocalDate> holidaySet = holidayService.findByYear(currtDate.getYear()).stream()
                    .map(YQHolidayEntity::getHolidayDate).collect(Collectors.toSet());


            // 预加载 HIS 数据和现金记录
            Map<String, HisOutpIncomeResponseDTO> hisDataMap = hisdata.findByDateOutp(currtDate.toString()).stream()
                    .collect(Collectors.toMap(HisOutpIncomeResponseDTO::getDbUser, Function.identity(), (v1, v2) -> v1));
            Map<String, YQCashRegRecordEntity> cashMap = cashService.findByDate(currtDate).stream()
                    .collect(Collectors.toMap(YQCashRegRecordEntity::getDbUser, Function.identity(), (v1, v2) -> v1));

            // 获取历史数据（昨日）
            Map<String, OutpCashSubEntity> yesterdayMap = new HashMap<>();
            // 获取昨日数据对象
            OutpCashMainEntity yesterdayMain = outpReportService.findByDate(currtDate.minusDays(1));
            //  先判断 main 是否为 null
            if (yesterdayMain == null || yesterdayMain.getSubs() == null || yesterdayMain.getSubs().isEmpty()) {
                yesterdayMap = Collections.emptyMap();
            } else {
                // 只有确定不为空时，才进行 stream 操作
                yesterdayMap = yesterdayMain.getSubs().stream()
                        .collect(Collectors.toMap(
                                OutpCashSubEntity::getDbUser,
                                Function.identity(),
                                (v1, v2) -> v1
                        ));
            }

            // 增加历史日期查询缓存，避免 N+1 问题 ---
            Map<LocalDate, Map<String, OutpCashSubEntity>> historyCache = new HashMap<>();
            List<OutpCashSubEntity> resultList = new ArrayList<>();
            String batchPk = new PrimaryKeyGenerator().generateKey();

            for (YQOperatorEntity operator : operators) {
                OutpCashSubEntity dto = new OutpCashSubEntity();
                dto.setSerialNo(batchPk);
//                dto.setOperatorNo(operator.getOperatorNo());
                dto.setDbUser(operator.getDbUser());
                dto.setHisOperatorName(operator.getOperatorName());
                dto.setPettyCash(operator.getPettyCash());

                // 1. 基础 HIS 收入赋值
                HisOutpIncomeResponseDTO hisDto = hisDataMap.get(operator.getDbUser());
                if (hisDto != null) {
                    dto.setHisAdvancePayment(getSafeBigDecimal(hisDto.getHisAdvancePayment()));
                    dto.setHisMedicalIncome(getSafeBigDecimal(hisDto.getHisMedicalIncome()));
                    dto.setReportAmount(dto.getHisAdvancePayment().add(dto.getHisMedicalIncome()));
                    dto.setAcctNo(hisDto.getAcctNo());
                    dto.setAcctDate(hisDto.getAcctDate());
                } else {
                    dto.setReportAmount(BigDecimal.ZERO);
                }


                YQCashRegRecordEntity cashRec = cashMap.get(operator.getDbUser());
                dto.setRetainedCash(cashRec != null ? getSafeBigDecimal(cashRec.getRetainedCash()) : BigDecimal.ZERO);


                /*
                1、判断是否汇总
                    1.1、是汇总 ，且对应日期 是节假日 且是月末最后一天
                    1.2、是汇总，且对应日期 是节假日后工作日第一天
                */
                if (isSpecialHoliday) {
                    //统一计算核心 自动处理回溯缓存
                    /**
                      需要完善独立的方法，首先去判断找到回溯截止的日期，且回溯期间是否有日期中无数据的，那就回溯截止日期到对应无数据为准
                     */
                    computeOutpFields(dto, currtDate, holidayType, holidaySet, historyCache);

                } else {
                    //应交报表数  =  his预交金 + his医疗收入
                    dto.setReportAmount(dto.getHisAdvancePayment().add(dto.getHisMedicalIncome()));
                    //前日暂收款  =  前一天的 当日 暂收款
                    OutpCashSubEntity yest = yesterdayMap.get(operator.getDbUser());
                    dto.setPreviousTemporaryReceipt(yest != null ? getSafeBigDecimal(yest.getCurrentTemporaryReceipt()) : BigDecimal.ZERO);
                }


                // 实交报表数据 = 应交报表数 - 前日暂收款
                dto.setActualReportAmount(dto.getReportAmount().subtract(dto.getPreviousTemporaryReceipt()));

                // 5.实收现金数 = 实收报表数 + 当日暂收款
                dto.setActualCashAmount(getSafeBigDecimal(dto.getActualReportAmount()).add(getSafeBigDecimal(dto.getCurrentTemporaryReceipt())));

                // 6.留存数差额 = 留存现金 - 备用金 + 实收报表数
                dto.setRetainedDifference(getSafeBigDecimal(dto.getRetainedCash())
                        .subtract(getSafeBigDecimal(dto.getPettyCash()))
                        .add(getSafeBigDecimal(dto.getActualReportAmount())));


                resultList.add(dto);
            }
            return resultList;
        } catch (Exception e) {
            log.error("门诊报表生成失败", e);
            return Collections.emptyList();
        }
    }



    /**
     * 封装回溯逻辑，缓存减少数据库IO
     */
    private void handleOutpBacktrackLogic(OutpCashSubEntity dto, LocalDate targetDate, String holidayType,
                                          Set<LocalDate> holidaySet, Map<LocalDate, Map<String, OutpCashSubEntity>> cache) {

        if (Constant.HOLIDAY_AFTER.equals(holidayType)) {
            // 回溯计算 A = B - Sum(C) - D
            BacktrackResult res = executeBacktrack(dto.getDbUser(), targetDate, holidaySet, cache);

            dto.setReportAmount(res.backReportAmount);
            dto.setPreviousTemporaryReceipt(res.backPreviousTemporaryReceipt);

//        } else if (Constant.HOLIDAY_IS.equals(holidayType)) {
//            dto.setActualReportAmount(dto.getReportAmount().subtract(getSafeBigDecimal(dto.getHolidayTemporaryReceipt())));
//            // 节假日且是月末需特殊处理...
//        } else if (Constant.HOLIDAY_PRE.equals(holidayType)) {
//            dto.setActualReportAmount(dto.getReportAmount().subtract(dto.getPreviousTemporaryReceipt()).subtract(getSafeBigDecimal(dto.getCurrentTemporaryReceipt())));
//        } else {
//            dto.setActualReportAmount(dto.getReportAmount());
//            dto.setHolidayTemporaryReceipt(BigDecimal.ZERO);
        }
    }


    private BacktrackResult executeBacktrack(String opNo, LocalDate targetDate, Set<LocalDate> holidaySet,
                                             Map<LocalDate, Map<String, OutpCashSubEntity>> cache) {
        BacktrackResult result = new BacktrackResult();
        LocalDate current = targetDate.minusDays(1);

        while (targetDate.toEpochDay() - current.toEpochDay() <= 30 && current.getDayOfMonth() >= 1) {

            // 从缓存获取，没命中则查数据库
            // findByDate判断是否有数据，如果没有数据会抛异常，直接捕获异常并记录日志，跳出循环
            OutpCashMainEntity byDate = outpReportService.findByDate(current);
            if(byDate == null || byDate.getSubs() == null || byDate.getSubs().isEmpty()){
                log.warn("回溯到历史日期无数据，日期：{}", current);
                break;
            }

            Map<String, OutpCashSubEntity> dayData  = cache.computeIfAbsent(current,
                        date -> outpReportService.findByDate(date).getSubs().stream()
                                .collect(Collectors.toMap(OutpCashSubEntity::getDbUser, Function.identity(), (v1, v2) -> v1)));

            OutpCashSubEntity hist = dayData.get(opNo);
            BigDecimal c = hist != null ? getSafeBigDecimal(hist.getReportAmount()) : BigDecimal.ZERO;
            BigDecimal b = hist != null ? getSafeBigDecimal(hist.getPreviousTemporaryReceipt()) : BigDecimal.ZERO;

            //再跳出循环之前 都应该累加
            result.backReportAmount = result.backReportAmount.add(c);

            if (holidaySet.contains(current)) {
//                result.backReportAmount = result.backReportAmount.add(c);
                //月初1号退出循环
                if (current.getDayOfMonth() == 1) {
                    result.backPreviousTemporaryReceipt = b;
                    break;
                }
                current = current.minusDays(1);

            } else {
                // 找到工作日（周五）
//                result.backReportAmount = result.backReportAmount.add(c);
                result.backPreviousTemporaryReceipt = b;
                break;
            }
        }
        return result;
    }


    /**
     * 判断日期是否为节假日 (使用 Set 版本)
     */
    private Set<LocalDate> getHolidaySet(int year) {
        return holidayService.findByYear(year).stream()
                .map(YQHolidayEntity::getHolidayDate).collect(Collectors.toSet());
    }

    /**
     * 安全获取 BigDecimal 值，如果为 null 则返回 BigDecimal.ZERO
     */
    private BigDecimal getSafeBigDecimal(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }


}