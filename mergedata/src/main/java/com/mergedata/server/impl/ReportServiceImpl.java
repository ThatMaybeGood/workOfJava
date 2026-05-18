package com.mergedata.server.impl;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.mergedata.constants.Constant;
import com.mergedata.exception.BusinessException;
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
import java.time.temporal.ChronoUnit;
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
    OutpReportService outpReportService;


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
            OutpCashMainEntity main = new OutpCashMainEntity();

            Long count = outpReportService.countByDate(body.getReportDate(), body.getTotalFlag());


            int type = holidayService.isSpecialHolidaySum(body.getReportDate(), body.getTotalFlag());


            List<OutpReportSubVO> subList = new ArrayList<>();

            //接收ExtendParams1为true时，即初始化报表
            Boolean isInitFlag = "true".equalsIgnoreCase(body.getExtendParams1());

            /*
            当表中存在报表，需要对初始化的只获取his更新 其余的不改变
            */
            if (isInitFlag && count > 0 && !Constant.HOLIDAY_MONTH_FIRST.equals(body.getTotalFlag())) {

                main = isInitOutpReportData(body, type);

                if (main == null) {
                    return null;
                }

                outpReportService.insertOrUpdate(main);
            }

            // 判断结果集，判断是否平台有无数据，有则查询出返回，无则调用接口获取数据并返回
            if (count == 0) {
                if (Constant.HOLIDAY_MONTH_FIRST.equals(body.getTotalFlag())) {   //判断是否月初数据并不报存到数据库
                    main = getOutpReportMonthStartData(body);
                } else {
                    main = getOutpReportData(body, type);
                    //无效查询，返回空列表
                    if (main == null) {
                        return null;
                    }
                    outpReportService.insertOrUpdate(main);
                }
            } else {
                main = outpReportService.findByDate(body.getReportDate(), body.getTotalFlag());
            }

            OutpReportMainVO mainVO = outpExchangeDbToView(main);
            mainVO.setTotalFlag(body.getTotalFlag());

            if (main.getSubs() == null || main.getSubs().isEmpty()) {
                return mainVO;
            }

            subList = mainVO.getSubList().stream()
                    .sorted(Comparator.comparing(OutpReportSubVO::getRowNum, Comparator.nullsLast(Comparator.naturalOrder()))
                            .thenComparing(OutpReportSubVO::getId, Comparator.nullsLast(Comparator.naturalOrder())))
                    .filter(r -> (body.getInpWindow() == null || !body.getInpWindow().equals(1) || Integer.valueOf(1).equals(r.getInpWindow())))
                    .filter(r -> (body.getAtm() == null || !body.getAtm().equals(1) || Integer.valueOf(1).equals(r.getAtm())))
                    .collect(Collectors.toList());
            mainVO.setSubList(subList);

            // 进行筛选
            return mainVO;

        } catch (Exception e) {
            log.error("获取报表数据异常", e);
            throw new RuntimeException("获取报表数据异常");
        }
    }

    /*
     * 是否初始化查询报表，如果是有对应数据，则只刷新his获取数据源
     */
    private OutpCashMainEntity isInitOutpReportData(OutpReportRequestBody body, int calculationType) {
        try {

            LocalDate currtDate = body.getReportDate();

            String pk = PrimaryKeyGenerator.generateKey();
            //查询出具体的类型
            String type = holidayService.queryDateType(body.getReportDate(), Constant.TYPE_OUTP);
            List<YQOperatorEntity> operators = operatorService.findByCategory(Constant.TYPE_OUTP);

            //提取当前已经有的报表数据
            OutpCashMainEntity main = outpReportService.findByDate(body.getReportDate(), body.getTotalFlag());
            // ⚠️ BUG: findByDateExclude可能返回null（当数据库无记录时），此处直接.setSerialNo(pk)会NPE
            // 建议：增加null判断，若返回null则新建对象或直接抛出异常
            main.setSerialNo(pk);


            //构建特殊情况，返回子对象展示单个列表
            OutpCashSubEntity sub = new OutpCashSubEntity();
            sub.setSerialNo(pk);
            sub.setSerialSubNo(PrimaryKeyGenerator.generateKey());
            sub.setOperatorName("当日暂收款");
            sub.setOperatorNo("当日暂收款");
            //如果是汇总查询，但是日期不符合特殊日期情况，直接返回空
            if (calculationType == 2) {
                String s = "初始化查询汇总，单对应的日期 [" + currtDate.toString() + "] 不符合特殊日期情况";
                sub.setRemarks(s);
                main.setSubs(Collections.singletonList(sub));
                main.setRemark(s);
                return main;
            }

//                List<YQOperatorEntity> operators = operatorService.findByCategory(Constant.TYPE_OUTP);

            // 只构建 inputFlag 为 "0" 的 operatorMap
            Map<String, YQOperatorEntity> operatorMap = operatorService.findByCategory(Constant.TYPE_OUTP).stream()
                    .filter(op -> "0".equals(op.getInputFlag()))
                    .collect(Collectors.toMap(YQOperatorEntity::getDbUser, Function.identity(), (v1, v2) -> v1));


            // 将 reportDate 转换为当天起始时间的 LocalDateTime
            LocalDateTime reportDateTime = body.getReportDate().atStartOfDay();
            // 只构建提取his标志的操作员键值对  reportDate <= updateTime 等价于 updateTime 大于等于 reportDateTime
            Map<String, YQOperatorEntity> operatorExtractHisMap = operatorService.findByCategory(Constant.TYPE_OUTP).stream()
                    .filter(op -> "1".equals(op.getExtractHisFlag())
                            || ("0".equals(op.getExtractHisFlag())
                            && op.getUpdateTime() != null
                            && op.getUpdateTime().compareTo(reportDateTime) >= 0))
                    .collect(Collectors.toMap(YQOperatorEntity::getDbUser, Function.identity(), (v1, v2) -> v1));

            List<OutpCashSubEntity> subsUp = splitDetailData(main.getSubs(), "1");
            Map<String, OutpCashSubEntity> subsMap = subsUp.stream()
                    .collect(Collectors.toMap(OutpCashSubEntity::getDbUser, Function.identity(), (v1, v2) -> v1));

            // 预加载 HIS 数据和现金记录
            Map<String, HisOutpIncomeResponseDTO> hisDataMap = hisdata.findByDateOutp(currtDate.toString()).stream()
                    .collect(Collectors.toMap(HisOutpIncomeResponseDTO::getDbUser, Function.identity(), (v1, v2) -> v1));
            Map<String, YQCashRegRecordEntity> cashMap = cashService.findByDate(currtDate).stream()
                    .collect(Collectors.toMap(YQCashRegRecordEntity::getDbUser, Function.identity(), (v1, v2) -> v1));

            // 获取历史数据（昨日）
            Map<String, OutpCashSubEntity> yesterdayMap = new HashMap<>();
            OutpCashMainEntity yesterdayMain = new OutpCashMainEntity();

            if (currtDate.getDayOfMonth() == 1) {
                yesterdayMain = outpReportService.findByDateExclude(currtDate, Constant.HOLIDAY_MONTH_FIRST);   //月初情况，查询当月的当天数据
            } else if (calculationType == 0 && holidayService.isSpecialHolidaySum(currtDate, Constant.HOLIDAY_TOTAL) == 1) {
                //判断是否周二，周二情况不能取值周一的正常的前日暂收款，应该取其汇总的站收款
                yesterdayMain = outpReportService.findByDateExclude(currtDate, Constant.HOLIDAY_TOTAL);
            } else {
                yesterdayMain = outpReportService.findByDateExclude(currtDate.minusDays(1), body.getTotalFlag());
            }

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


            LocalDate minDate = LocalDate.of(1900, 1, 1);
            Map<LocalDate, OutpCashMainEntity> historyMap = new HashMap<>();
            if (calculationType == 1) {
                // 获取昨日数据对象
                // 定位回溯的最远日期
                minDate = holidayService.findMinBacktrackDate(currtDate, body.getTotalFlag());

                List<OutpCashMainEntity> historyMains = outpReportService.findBatchByDateRange(minDate, currtDate.minusDays(1), "0");
                if (Constant.HOLIDAY_MONTH_LASTDAY.equals(type)) {
                    historyMains = outpReportService.findBatchByDateRange(minDate, currtDate, "0");
                }

                historyMap = historyMains.stream()
                        .collect(Collectors.toMap(OutpCashMainEntity::getReportDate, Function.identity()));

                long totalDays = ChronoUnit.DAYS.between(minDate, currtDate);

                if (historyMap.size() < totalDays) {
                    // 找出第一个缺失的日期，
                    for (LocalDate d = minDate; d.isBefore(currtDate); d = d.plusDays(1)) {
                        if (!historyMap.containsKey(d)) {
                            String s = "回溯数据不完整：报表日期 [" + d.toString() + "] 数据缺失，无法进行回溯计算。";
                            sub.setRemarks(s);

                            main.setRemark(s);
                            main.setSubs(Collections.singletonList(sub));
                            return main;
                        }
                    }
                }

            }


            //查询当前已有报表数据
            List<OutpCashSubEntity> resultList = new ArrayList<>();

            for (YQOperatorEntity operator : operators) {
                //标识判断是否新增的操作员
                String addFlag = "0";

                OutpCashSubEntity dto = subsMap.get(operator.getDbUser());

                if (dto == null) {
                    addFlag = "1";
                    dto = new OutpCashSubEntity();
                }


                dto.setOperatorNo(operator.getDbUser());
                dto.setOperatorName(operator.getOperatorName());
                dto.setDbUser(operator.getDbUser());
                dto.setRowNum(operator.getRowNum());

                dto.setSerialNo(pk);
                dto.setSerialSubNo(PrimaryKeyGenerator.generateKey());

                //添加对有his标志的人进行赋值
                if (operatorMap.containsKey(operator.getDbUser()) && operatorExtractHisMap.containsKey(operator.getDbUser())) {
                    // 1. 基础 HIS 收入赋值
                    HisOutpIncomeResponseDTO hisDto = hisDataMap.get(operator.getDbUser());
                    if (hisDto != null) {
                        dto.setHisAdvancePayment(getSafeBigDecimal(hisDto.getHisAdvancePayment()));
                        dto.setHisMedicalIncome(getSafeBigDecimal(hisDto.getHisMedicalIncome()));
                        dto.setReportAmount(dto.getHisAdvancePayment().add(dto.getHisMedicalIncome()));
                        dto.setAcctNo(null);
                        dto.setAcctDate(null);
                        if (calculationType == 0) {
                            dto.setAcctNo(hisDto.getAcctNo());
                            dto.setAcctDate(hisDto.getAcctDate());
                        }

                    }
                }


                    /*
                    1、判断是否汇总
                        1.1、是汇总 ，且对应日期 是节假日 且是月末最后一天
                        1.2、是汇总，且对应日期 是节假日后工作日第一天
                    */
                if (calculationType == 1 && addFlag.equals("0")) {
                    /**
                     需要完善独立的方法，首先去判断找到回溯截止的日期，且回溯期间是否有日期中无数据的，那就回溯截止日期到对应无数据为准
                     */
                    handleOutpBacktrackLogic(dto, currtDate, minDate, historyMap, type);
                }

                if (calculationType == 0) {
                    //前日暂收款  =  前一天的 当日 暂收款
                    OutpCashSubEntity yest = yesterdayMap.get(operator.getDbUser());
                    dto.setPreviousTemporaryReceipt(yest != null ? getSafeBigDecimal(yest.getCurrentTemporaryReceipt()) : BigDecimal.ZERO);
                }

                //应交报表数  =  his预交金 + his医疗收入
                dto.setReportAmount(dto.getHisAdvancePayment().add(dto.getHisMedicalIncome()));

                //汇总情况下，对应公式应该是  月末最后一天且非节假日也是如此
//                    if (calculationType == 1 || Constant.HOLIDAY_NOT_MONTH_LASTDAY.equals(type)) {
//                        // 实交报表数据 = 应交报表数 - 前日暂收款 - 节假日暂收款
//                        dto.setActualReportAmount(dto.getReportAmount().subtract(dto.getPreviousTemporaryReceipt()).subtract(dto.getHolidayTemporaryReceipt()));
//                    } else {
//                        // 实交报表数据 = 应交报表数 - 前日暂收款
//                        dto.setActualReportAmount(dto.getReportAmount().subtract(dto.getPreviousTemporaryReceipt()));
//                    }

                //2026.04.23 要求修改 实交报表数据 = 应交报表数 - 前日暂收款 - 节假日暂收款
                dto.setActualReportAmount(dto.getReportAmount()
                        .subtract(dto.getPreviousTemporaryReceipt())
                        .subtract(dto.getHolidayTemporaryReceipt()));

                // 5.实收现金数 = 实收报表数 + 当日暂收款
                dto.setActualCashAmount(getSafeBigDecimal(dto.getActualReportAmount()).add(getSafeBigDecimal(dto.getCurrentTemporaryReceipt())));

                // 6.留存数差额 = 留存现金 - 备用金 + 实收报表数
                dto.setRetainedDifference(getSafeBigDecimal(dto.getRetainedCash())
                        .subtract(getSafeBigDecimal(dto.getPettyCash()))
                        .add(getSafeBigDecimal(dto.getActualReportAmount())));

                resultList.add(dto);

            }


//            main.setSubs(resultList);

            List<OutpCashSubEntity> subsDown = splitDetailData(main.getSubs(), "2");
            //判断是否合计后金额替换组装
            setSpecialRowsValues(main, resultList, subsDown,body.getTotalFlag(),type);


            return main;
        } catch (Exception e) {
            log.error("门诊报表初始化生成失败", e);
            return null;
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
        if (reportDate == null) {
            reportDate = LocalDate.now();
        }

        //查询出具体的类型
        String type = holidayService.queryDateType(reportDate, Constant.TYPE_OUTP);

        String remark = "";
        for (OutpReportSubVO sub : mainVO.getSubList()) {
            if ("当日暂收款".equals(sub.getOperatorName())) {
                remark = sub.getRemarks();
            }
        }


        //计算合计
//        OutpReportSubVO calculateTotal = calculateTotal(mainVO.getSubList(), reportDate, Constant.EXCLUDE_OPERATOR_NAMES);


        OutpCashMainEntity main = outpExchangeViewToDb(reportDate, mainVO, remark);

        // 剔除合计以后得sub明细
        //拆分明细数据
        List<OutpCashSubEntity> subsUp = splitDetailData(main.getSubs(), "1");
        List<OutpCashSubEntity> subsDown = splitDetailData(main.getSubs(), "2");


        if (!(Constant.HOLIDAY_MONTH_FIRST.equals(mainVO.getTotalFlag()) && reportDate.getDayOfMonth() == 1)) {  //非月初报表数据需要校验转换
            //转换为实体类的数据值需要验证，防止写入的数据有非修改的 而改动 校验方法
            //明细数据校验方法
            //判断是否符合特殊节假日需要进行回溯汇总计算
            int calculationType = holidayService.isSpecialHolidaySum(reportDate, mainVO.getTotalFlag());
            //如果是汇总查询，但是日期不符合特殊日期情况，不处理校验
            if (calculationType != 2) {
                //用于校验对应数据修改的
                ouptInsertVerityDetailData(reportDate, calculationType, main.getTotalFlag(), subsUp);

                //判断是否合计后金额替换组装
                setSpecialRowsValues(main, subsUp, subsDown,mainVO.getTotalFlag(),type);
            }
        }

        try {
            outpReportService.insertOrUpdate(main);
        } catch (Exception e) {
            log.error("保存门诊报表数据异常", e);
            throw new RuntimeException("保存门诊报表数据异常", e);
        }
        return Constant.SUCCESS;
    }

    /*
     * 拆分明细数据
     * @param subs 门诊报表明细数据
     * @param upOrDown 1:上 2:下
     * @return 分明细数据
     */
    private List<OutpCashSubEntity> splitDetailData(List<OutpCashSubEntity> subs, String upOrDown) {
        if (upOrDown.equals("1")) {
            return subs.stream()
                    .filter(dto -> dto.getOperatorName() != null)
                    .filter(dto -> !Constant.EXCLUDE_OPERATOR_NAMES.contains(dto.getOperatorName()))
                    .collect(Collectors.toList());
        } else {
            return subs.stream()
                    .filter(dto -> dto.getOperatorName() != null)
                    .filter(dto -> Constant.EXCLUDE_OPERATOR_NAMES.contains(dto.getOperatorName()))
                    .collect(Collectors.toList());
        }
    }

    /*
     * EXCLUDE_OPERATOR_NAMES 构建空的合计数据
     */
    private List<OutpCashSubEntity> buildTotalData(String pk) {
        List<OutpCashSubEntity> resultList = new ArrayList<>();
        for (String name : Constant.EXCLUDE_OPERATOR_NAMES) {
            OutpCashSubEntity dto = new OutpCashSubEntity();
            dto.setSerialNo(pk);
            dto.setOperatorNo(name);
            dto.setOperatorName(name);
            resultList.add(dto);
        }
        return resultList;
    }


    /*
     * 用于门诊插入数据时候 合计之后的数据需要填写组装
     *
     */
    private void setSpecialRowsValues(OutpCashMainEntity main, List<OutpCashSubEntity> subsUpList, List<OutpCashSubEntity> subsDownList,String totalFlag,String type) {


        OutpCashSubEntity calc = calculateTotal(subsUpList);


        // 先获取公式需要的原始值
        BigDecimal 原当日暂收款 = calc.getCurrentTemporaryReceipt(); //获取当日暂收款合计
        BigDecimal 原日报表数 = calc.getReportAmount();  //获取实交报表数合计
        BigDecimal 原合计存款金额 = getHisAdvancePaymentByName(subsDownList, "合计存款金额");
        BigDecimal 原住院部当日回款 = getHisAdvancePaymentByName(subsDownList, "住院部当日回款");
        BigDecimal 原门诊当日回款 = getHisAdvancePaymentByName(subsDownList, "门诊当日回款");
        BigDecimal 原门诊当日借款 = getHisAdvancePaymentByName(subsDownList, "门诊当日借款");
        BigDecimal 原住院部当日借款 = getHisAdvancePaymentByName(subsDownList, "住院部当日借款");

        if (totalFlag.equals("0")&&(type.equals("1")||type.equals("4"))){
             原当日暂收款 = calc.getHolidayTemporaryReceipt(); //获取节假日暂收款合计
             原日报表数 = BigDecimal.ZERO;  //获取实交报表数合计
        }


        // 计算公式值    门诊当日实存金额=当日暂收款+日报表数+合计存款金额+住院部当日回款+门诊当日回款-门诊当日借款-住院部当日借款
        BigDecimal 门诊当日实存金额 = 原当日暂收款.add(原日报表数)
                .add(原合计存款金额)
                .add(原住院部当日回款)
                .add(原门诊当日回款)
                .subtract(原门诊当日借款)
                .subtract(原住院部当日借款);

        // 设置新值
        for (OutpCashSubEntity dto : subsDownList) {
            String name = dto.getOperatorName();
            if (name == null) continue;

            dto.setSerialSubNo(PrimaryKeyGenerator.generateKey());
            dto.setSerialNo(main.getSerialNo());

            switch (name) {
                case "合计":
                    dto.resetAllAmountsToZero(); //初始化为0 为了合计重新使用计算的

                    dto.setHisAdvancePayment(calc.getHisAdvancePayment());
                    dto.setHisMedicalIncome(calc.getHisMedicalIncome());
                    dto.setHisRegistrationIncome(calc.getHisRegistrationIncome());
                    dto.setReportAmount(calc.getReportAmount());

                    dto.setPreviousTemporaryReceipt(calc.getPreviousTemporaryReceipt());
                    dto.setHolidayTemporaryReceipt(calc.getHolidayTemporaryReceipt());
                    dto.setActualReportAmount(calc.getActualReportAmount());
                    dto.setCurrentTemporaryReceipt(calc.getCurrentTemporaryReceipt());
                    dto.setActualCashAmount(calc.getActualCashAmount());
                    dto.setRetainedDifference(calc.getRetainedDifference());
                    dto.setRetainedCash(calc.getRetainedCash());
                    dto.setPettyCash(calc.getPettyCash());

                    break;
                case "当日暂收款":
                    dto.setHisAdvancePayment(calc.getCurrentTemporaryReceipt());
                    break;
                case "日报表数":
                    dto.setHisAdvancePayment(calc.getActualReportAmount());
                    break;
                case "门诊当日实存金额":
                    dto.setHisAdvancePayment(门诊当日实存金额);
                    break;
            }
        }
        List<OutpCashSubEntity> mergedList = new ArrayList<>();
        mergedList.addAll(subsUpList);
        mergedList.addAll(subsDownList);
        main.setSubs(mergedList);
    }

    private BigDecimal getHisAdvancePaymentByName(List<OutpCashSubEntity> dtoList, String name) {
        return dtoList.stream()
                .filter(dto -> name.equals(dto.getOperatorName()))
                .findFirst()
                .map(OutpCashSubEntity::getHisAdvancePayment)
                .filter(Objects::nonNull)
                .orElse(BigDecimal.ZERO);
    }


    /**
     * 门诊明细数据写入前校验方法,确保有些不能修改的字段值，修改后保存
     */

    private void ouptInsertVerityDetailData(LocalDate currtDate, int calculationType, String totalFlag, List<OutpCashSubEntity> subList) {
        try {
            Map<String, YQOperatorEntity> operatorMap = operatorService.findByCategory(Constant.TYPE_OUTP).stream()
                    .collect(Collectors.toMap(YQOperatorEntity::getDbUser, Function.identity(), (v1, v2) -> v1));

            // 预加载 HIS 数据和现金记录
            Map<String, HisOutpIncomeResponseDTO> hisDataMap = hisdata.findByDateOutp(currtDate.toString()).stream()
                    .collect(Collectors.toMap(HisOutpIncomeResponseDTO::getDbUser, Function.identity(), (v1, v2) -> v1));
            Map<String, YQCashRegRecordEntity> cashMap = cashService.findByDate(currtDate).stream()
                    .collect(Collectors.toMap(YQCashRegRecordEntity::getDbUser, Function.identity(), (v1, v2) -> v1));

            //查询出具体的类型
            String type = holidayService.queryDateType(currtDate, Constant.TYPE_OUTP);

            // 获取历史数据（昨日）
            Map<String, OutpCashSubEntity> yesterdayMap = new HashMap<>();

            OutpCashMainEntity yesterdayMain = new OutpCashMainEntity();
            // ⚠️ BUG: new空对象导致下方的null判断(第569行)永远不会为true，findByDateExclude返回null时会被空对象替代
            // 建议：初始化为null，让后续逻辑统一处理null情况
            if (currtDate.getDayOfMonth() == 1 && Constant.HOLIDAY_MONTH_FIRST.equals(totalFlag)) {
                yesterdayMain = outpReportService.findByDateExclude(currtDate, Constant.HOLIDAY_MONTH_FIRST);
            }else if (calculationType == 0 && holidayService.isSpecialHolidaySum(currtDate, Constant.HOLIDAY_TOTAL) == 1) {
                //判断是否周二，周二情况不能取值周一的正常的前日暂收款，应该取其汇总的站收款
                yesterdayMain = outpReportService.findByDateExclude(currtDate, Constant.HOLIDAY_TOTAL);
            } else {
                yesterdayMain = outpReportService.findByDateExclude(currtDate.minusDays(1), totalFlag);
            }

            if (yesterdayMain == null || yesterdayMain.getSubs() == null || yesterdayMain.getSubs().isEmpty()) {
                yesterdayMap = Collections.emptyMap();
            } else {
                yesterdayMap = yesterdayMain.getSubs().stream()
                        .collect(Collectors.toMap(
                                OutpCashSubEntity::getDbUser,
                                Function.identity(),
                                (v1, v2) -> v1
                        ));
            }

            LocalDate minDate = LocalDate.of(1900, 1, 1);
            Map<LocalDate, OutpCashMainEntity> historyMap = new HashMap<>();
            if (calculationType == 1) {
                minDate = holidayService.findMinBacktrackDate(currtDate, totalFlag);
                List<OutpCashMainEntity> historyMains = outpReportService.findBatchByDateRange(minDate, currtDate.minusDays(1), "0");
                if (Constant.HOLIDAY_MONTH_LASTDAY.equals(type)) {
                    historyMains = outpReportService.findBatchByDateRange(minDate, currtDate, "0");
                }
                historyMap = historyMains.stream()
                        .collect(Collectors.toMap(OutpCashMainEntity::getReportDate, Function.identity()));
            }


            for (OutpCashSubEntity dto : subList) {
                YQOperatorEntity operator = operatorMap.get(dto.getDbUser());

                if (operator == null) {
                    log.error("操作员不存在, dbUser: {}", dto.getDbUser());
                    dto.setPettyCash(BigDecimal.ZERO);  // 设置默认值
                } else {
                    dto.setPettyCash(operator.getPettyCash());
                    //增加合计情况不
                    if (calculationType != 1) {
                        // 如果是不可以输入的人，需要提取 his 数据
                        if (!"1".equals(operator.getInputFlag())) {
                            HisOutpIncomeResponseDTO hisDto = hisDataMap.get(dto.getDbUser());
                            if (hisDto != null) {
                                dto.setHisAdvancePayment(getSafeBigDecimal(hisDto.getHisAdvancePayment()));
                                dto.setHisMedicalIncome(getSafeBigDecimal(hisDto.getHisMedicalIncome()));
                                dto.setAcctNo(hisDto.getAcctNo());
                                dto.setAcctDate(hisDto.getAcctDate());
                            } else {
                                dto.setHisAdvancePayment(BigDecimal.ZERO);
                                dto.setHisMedicalIncome(BigDecimal.ZERO);
                                dto.setAcctNo(null);
                                dto.setAcctDate(null);
                            }
                        }
                    }
                }

                // 4. 计算 reportAmount
                dto.setReportAmount(dto.getHisAdvancePayment().add(dto.getHisMedicalIncome()));

                // 5. 设置留存现金
                YQCashRegRecordEntity cashRec = cashMap.get(dto.getDbUser());
                dto.setRetainedCash(cashRec != null ? getSafeBigDecimal(cashRec.getRetainedCash()) : BigDecimal.ZERO);

                //保存数据时候取消汇总 2025。05.07
//                // 6. 汇总回溯处理
//                if (calculationType == 1) {
//                    handleOutpBacktrackLogic(dto, currtDate, minDate, historyMap, type);
//                }

                // 7. 非汇总处理
                if (calculationType == 0) {
                    dto.setReportAmount(dto.getHisAdvancePayment().add(dto.getHisMedicalIncome()));
                    OutpCashSubEntity yest = yesterdayMap.get(dto.getDbUser());
                    dto.setPreviousTemporaryReceipt(yest != null ? getSafeBigDecimal(yest.getCurrentTemporaryReceipt()) : BigDecimal.ZERO);
                }

//                // 8. 实交报表数据计算
//                if (calculationType == 1 || Constant.HOLIDAY_NOT_MONTH_LASTDAY.equals(type)) {
//                    dto.setActualReportAmount(dto.getReportAmount()
//                            .subtract(dto.getPreviousTemporaryReceipt())
//                            .subtract(dto.getHolidayTemporaryReceipt()));
//                } else {
//                    dto.setActualReportAmount(dto.getReportAmount()
//                            .subtract(dto.getPreviousTemporaryReceipt()));
//                }
                //2026.04.23 要求修改 实交报表数据 = 应交报表数 - 前日暂收款 - 节假日暂收款
                dto.setActualReportAmount(dto.getReportAmount()
                        .subtract(dto.getPreviousTemporaryReceipt())
                        .subtract(dto.getHolidayTemporaryReceipt()));

                // 9. 实收现金数
                dto.setActualCashAmount(getSafeBigDecimal(dto.getActualReportAmount())
                        .add(getSafeBigDecimal(dto.getCurrentTemporaryReceipt())));

                // 10. 留存数差额
                dto.setRetainedDifference(getSafeBigDecimal(dto.getRetainedCash())
                        .subtract(getSafeBigDecimal(dto.getPettyCash()))
                        .add(getSafeBigDecimal(dto.getActualReportAmount())));
            }

        } catch (Exception e) {
            log.error("门诊报表保存时，对于不需要修改的数据校验失败", e);
            throw new BusinessException("数据校验失败：" + e.getMessage(), e);
        }
    }

    /**
     * 门诊报表数据实体转换视图
     */
    private OutpReportMainVO outpExchangeDbToView(OutpCashMainEntity mainEntity) {

        OutpReportMainVO mainVO = new OutpReportMainVO();
        mainVO.setTotalFlag(mainEntity.getTotalFlag());

        // 转换 List
        if (CollectionUtils.isNotEmpty(mainEntity.getSubs())) {
            List<OutpReportSubVO> subList = mainEntity.getSubs().stream().map(subEntity -> {
                OutpReportSubVO subVO = new OutpReportSubVO();
                // 复制除 acctDate 外的所有字段
                String[] ignoreProperties = {"acctDate"};
                BeanUtils.copyProperties(subEntity, subVO, ignoreProperties);

                // 手动处理 LocalDateTime 到 LocalDate 的转换
                if (subEntity.getAcctDate() != null) {
                    subVO.setAcctDate(subEntity.getAcctDate().toLocalDate());
                }

                subVO.setReportDate(mainEntity.getReportDate());
                subVO.setReportYear(mainEntity.getReportYear());
                subVO.setCreateTime(mainEntity.getCreateTime());

                return subVO;
            }).collect(Collectors.toList());

            mainVO.setSubList(subList);
        }
        return mainVO;
    }


    /**
     * 门诊报表数据视图转换实体类
     */
    private OutpCashMainEntity outpExchangeViewToDb(LocalDate reportDate, OutpReportMainVO mainVO, String remark) {

        OutpCashMainEntity mainEntity = new OutpCashMainEntity();
        String pk = PrimaryKeyGenerator.generateKey();
        mainEntity.setSerialNo(pk);
        mainEntity.setReportDate(reportDate);
        mainEntity.setReportYear(reportDate.getYear());
        mainEntity.setTotalFlag(mainVO.getTotalFlag());
        mainEntity.setValidFlag(Constant.YES);
        mainEntity.setRemark(remark);
        mainEntity.setCreateTime(LocalDateTime.now());

        // 转换 List
        if (CollectionUtils.isNotEmpty(mainVO.getSubList())) {
            List<OutpCashSubEntity> subList = mainVO.getSubList().stream().map(subVO -> {
                OutpCashSubEntity subEntity = new OutpCashSubEntity();

                // 复制除 acctDate 外的所有字段
                String[] ignoreProperties = {"acctDate"};
                BeanUtils.copyProperties(subVO, subEntity, ignoreProperties);
                // 手动处理 LocalDateTime 到 LocalDate 的转换
                if (subEntity.getAcctDate() != null) {
                    subEntity.setAcctDate(subVO.getAcctDate().atStartOfDay());
                }

                subEntity.setSerialNo(pk);
                subEntity.setSerialSubNo(PrimaryKeyGenerator.generateKey());

                return subEntity;
            }).collect(Collectors.toList());

            mainEntity.setSubs(subList);
        }

        return mainEntity;
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


            // 3. 构建结果集
            InpCashMainEntity resultVo = new InpCashMainEntity();
            List<InpCashSubEntity> inpCashSubList = new ArrayList<>();

            PrimaryKeyGenerator pks = new PrimaryKeyGenerator();
            String pk = pks.generateKey();
            // ⚠️ 注意: PrimaryKeyGenerator.generateKey()是静态方法，建议直接使用静态调用
            // 且getOutpReportData方法中大量代码与isInitOutpReportData重复，约200行近乎相同的逻辑

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
        // ⚠️ 注意: PrimaryKeyGenerator.generateKey()是静态方法，建议统一使用静态调用

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

    /*
     * 用于计算写入时候的操作员的数据合计
     */
    private OutpCashSubEntity calculateTotal(List<OutpCashSubEntity> dtoList) {
        final BigDecimal ZERO = BigDecimal.ZERO;
        BinaryOperator<BigDecimal> sumOperator = BigDecimal::add;


        // 创建新的合计对象
        OutpCashSubEntity total = new OutpCashSubEntity();
        total.setOperatorNo(null);
        total.setOperatorName("合计");

        // 定义求和函数
        Function<Function<OutpCashSubEntity, BigDecimal>, BigDecimal> sumByField =
                getter -> dtoList.stream()
                        .map(getter)
                        .filter(Objects::nonNull)
                        .reduce(ZERO, sumOperator);

        // 更新合计行的各个金额字段
        total.setHisAdvancePayment(sumByField.apply(OutpCashSubEntity::getHisAdvancePayment));
        total.setHisMedicalIncome(sumByField.apply(OutpCashSubEntity::getHisMedicalIncome));
        total.setHisRegistrationIncome(sumByField.apply(OutpCashSubEntity::getHisRegistrationIncome));

        total.setReportAmount(sumByField.apply(OutpCashSubEntity::getReportAmount));
        total.setPreviousTemporaryReceipt(sumByField.apply(OutpCashSubEntity::getPreviousTemporaryReceipt));
        total.setHolidayTemporaryReceipt(sumByField.apply(OutpCashSubEntity::getHolidayTemporaryReceipt));
        total.setActualReportAmount(sumByField.apply(OutpCashSubEntity::getActualReportAmount));
        total.setCurrentTemporaryReceipt(sumByField.apply(OutpCashSubEntity::getCurrentTemporaryReceipt));
        total.setActualCashAmount(sumByField.apply(OutpCashSubEntity::getActualCashAmount));
        total.setRetainedDifference(sumByField.apply(OutpCashSubEntity::getRetainedDifference));
        total.setRetainedCash(sumByField.apply(OutpCashSubEntity::getRetainedCash));
        total.setPettyCash(sumByField.apply(OutpCashSubEntity::getPettyCash));

        total.setRemarks("合计行，不展示在报表中");
//        total.setReportDate(reportdate);
//        total.setCreateTime(LocalDateTime.now());

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
        BigDecimal backHisAdvancePayment = BigDecimal.ZERO; //his预交金
        BigDecimal backHisMedicalIncome = BigDecimal.ZERO; //his医疗收入
        BigDecimal backReportAmount = BigDecimal.ZERO;  // 应交报表数
        BigDecimal backPreviousTemporaryReceipt = BigDecimal.ZERO;   // 前日暂收款
        BigDecimal backHolidayTemporaryReceipt = BigDecimal.ZERO; // 节假日暂收款
    }

    /**
     * 获取门诊报表---优化了回溯查询
     * 1. 从各数据源获取数据。
     * 2. 以操作员为基准进行匹配和计算。
     * 3. 对周一进行特殊的回溯计算 (A = B - Sum(C) - D)。
     * 4. 对其他工作日进行正常计算 (A = B - C - D)。
     *
     * @param body            日期
     * @param calculationType 计算类型 0：正常计算 1：特殊回溯计算 ,2 直接明细设空
     * @return 包含所有操作员计算结果的 ReportDTO 列表
     */
    public OutpCashMainEntity getOutpReportData(OutpReportRequestBody body, int calculationType) {
        try {
            LocalDate currtDate = body.getReportDate();
            String pk = PrimaryKeyGenerator.generateKey();
            //查询出具体的类型
            String type = holidayService.queryDateType(body.getReportDate(), Constant.TYPE_OUTP);


            OutpCashMainEntity main = new OutpCashMainEntity();
            main.setSerialNo(pk);
            main.setReportDate(currtDate);
            main.setReportYear(body.getReportDate().getYear());
            main.setTotalFlag(body.getTotalFlag());
            main.setValidFlag("1");
            main.setCreateTime(LocalDateTime.now());

            //构建特殊情况，返回子对象展示单个列表
            OutpCashSubEntity sub = new OutpCashSubEntity();
            sub.setSerialNo(pk);
            sub.setSerialSubNo(PrimaryKeyGenerator.generateKey());
            sub.setOperatorName("当日暂收款");
            sub.setOperatorNo("当日暂收款");


            //如果是汇总查询，但是日期不符合特殊日期情况，直接返回空
            if (calculationType == 2) {
                String s = "查询汇总，单对应的日期 [" + currtDate.toString() + "] 不符合特殊日期情况";
                sub.setRemarks(s);
                main.setSubs(Collections.singletonList(sub));
                main.setRemark(s);
                return main;
            }

            List<YQOperatorEntity> operators = operatorService.findByCategory(Constant.TYPE_OUTP);

            // 将 reportDate 转换为当天起始时间的 LocalDateTime
            LocalDateTime reportDateTime = body.getReportDate().atStartOfDay();
            // 只构建提取his标志的操作员键值对  reportDate <= updateTime 等价于 updateTime 大于等于 reportDateTime
            Map<String, YQOperatorEntity> operatorExtractHisMap = operatorService.findByCategory(Constant.TYPE_OUTP).stream()
                    .filter(op -> "1".equals(op.getExtractHisFlag())
                            || ("0".equals(op.getExtractHisFlag())
                            && op.getUpdateTime() != null
                            && op.getUpdateTime().compareTo(reportDateTime) >= 0))
                    .collect(Collectors.toMap(YQOperatorEntity::getDbUser, Function.identity(), (v1, v2) -> v1));


            // 预加载 HIS 数据和现金记录
            Map<String, HisOutpIncomeResponseDTO> hisDataMap = hisdata.findByDateOutp(currtDate.toString()).stream()
                    .collect(Collectors.toMap(HisOutpIncomeResponseDTO::getDbUser, Function.identity(), (v1, v2) -> v1));

            Map<String, YQCashRegRecordEntity> cashMap = cashService.findByDate(currtDate).stream()
                    .collect(Collectors.toMap(YQCashRegRecordEntity::getDbUser, Function.identity(), (v1, v2) -> v1));

            // 获取历史数据（昨日）
            Map<String, OutpCashSubEntity> yesterdayMap = new HashMap<>();
            OutpCashMainEntity yesterdayMain = new OutpCashMainEntity();

            if (currtDate.getDayOfMonth() == 1) {
                yesterdayMain = outpReportService.findByDateExclude(currtDate, Constant.HOLIDAY_MONTH_FIRST);   //月初情况，查询当月的当天数据
            } else if (calculationType == 0 && holidayService.isSpecialHolidaySum(currtDate, Constant.HOLIDAY_TOTAL) == 1) {
                //判断是否周二，周二情况不能取值周一的正常的前日暂收款，应该取其汇总的站收款
                yesterdayMain = outpReportService.findByDateExclude(currtDate, Constant.HOLIDAY_TOTAL);
            } else {
                yesterdayMain = outpReportService.findByDateExclude(currtDate.minusDays(1), body.getTotalFlag());
            }

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


            LocalDate minDate = LocalDate.of(1900, 1, 1);
            Map<LocalDate, OutpCashMainEntity> historyMap = new HashMap<>();
            if (calculationType == 1) {
                // 获取昨日数据对象
                // 定位回溯的最远日期
                minDate = holidayService.findMinBacktrackDate(currtDate, body.getTotalFlag());
                //一次性查询范围内的所有报表（包含 Subs 明细）
                // WHERE report_date >= minDate AND report_date < currtDate
                List<OutpCashMainEntity> historyMains = outpReportService.findBatchByDateRange(minDate, currtDate.minusDays(1), "0");
                //月末情况需要包含当天的数据
                if (Constant.HOLIDAY_MONTH_LASTDAY.equals(type)) {
                    historyMains = outpReportService.findBatchByDateRange(minDate, currtDate, "0");
                }
                //转换为 Map 以便内存快速回溯
                historyMap = historyMains.stream()
                        .collect(Collectors.toMap(OutpCashMainEntity::getReportDate, Function.identity()));

                // 1. 计算需要校验的日期范围 ,首先判断数据是否连贯性完整，如果不完整就抛出对应异常或者设置为空
                long totalDays = ChronoUnit.DAYS.between(minDate, currtDate); // 不包含 currtDate
                // 2. 验证 Map 大小与天数是否一致
                if (historyMap.size() < totalDays) {
                    // 找出第一个缺失的日期，
                    for (LocalDate d = minDate; d.isBefore(currtDate); d = d.plusDays(1)) {
                        if (!historyMap.containsKey(d)) {
                            String s = "回溯数据不完整：报表日期 [" + d.toString() + "] 数据缺失，无法进行回溯计算。";
                            sub.setRemarks(s);

                            main.setRemark(s);
                            main.setSubs(Collections.singletonList(sub));
                            return main;
//                            throw new RuntimeException("数据不完整：报表日期 " + d + " 数据缺失，无法进行回溯计算。");
                        }
                    }
                }

            }


            // 增加历史日期查询缓存，避免 N+1 问题 ---
            List<OutpCashSubEntity> resultList = new ArrayList<>();

            for (YQOperatorEntity operator : operators) {
                OutpCashSubEntity dto = new OutpCashSubEntity();
                dto.setSerialNo(pk);
                dto.setSerialSubNo(PrimaryKeyGenerator.generateKey());  //每一条生成唯一的编号
                dto.setOperatorNo(operator.getOperatorNo());
                dto.setDbUser(operator.getDbUser());
                dto.setOperatorName(operator.getOperatorName());
                dto.setPettyCash(operator.getPettyCash());
                dto.setInpWindow(operator.getInpWindow());
                dto.setAtm(operator.getAtm());
                dto.setRowNum(operator.getRowNum());

                //添加05.08  添加对应his标志且修改之前的数据可以提取his数据
                if (operatorExtractHisMap.containsKey(operator.getDbUser())) {
                    // 1. 基础 HIS 收入赋值
                    HisOutpIncomeResponseDTO hisDto = hisDataMap.get(operator.getDbUser());
                    if (hisDto != null) {
                        dto.setHisAdvancePayment(getSafeBigDecimal(hisDto.getHisAdvancePayment()));
                        dto.setHisMedicalIncome(getSafeBigDecimal(hisDto.getHisMedicalIncome()));
                        dto.setReportAmount(dto.getHisAdvancePayment().add(dto.getHisMedicalIncome()));
                        dto.setAcctNo(null);
                        dto.setAcctDate(null);
                        if (calculationType == 0) {
                            dto.setAcctNo(hisDto.getAcctNo());
                            dto.setAcctDate(hisDto.getAcctDate());
                        }
                    } else {
                        dto.setReportAmount(BigDecimal.ZERO);
                    }
                }

                YQCashRegRecordEntity cashRec = cashMap.get(operator.getDbUser());
                dto.setRetainedCash(cashRec != null ? getSafeBigDecimal(cashRec.getRetainedCash()) : BigDecimal.ZERO);


                /*
                1、判断是否汇总
                    1.1、是汇总 ，且对应日期 是节假日 且是月末最后一天
                    1.2、是汇总，且对应日期 是节假日后工作日第一天
                */
                if (calculationType == 1) {
                    /**
                     需要完善独立的方法，首先去判断找到回溯截止的日期，且回溯期间是否有日期中无数据的，那就回溯截止日期到对应无数据为准
                     */
                    handleOutpBacktrackLogic(dto, currtDate.minusDays(1), minDate, historyMap, type);
                }

                if (calculationType == 0) {
                    //前日暂收款  =  前一天的 当日 暂收款
                    OutpCashSubEntity yest = yesterdayMap.get(operator.getDbUser());
                    dto.setPreviousTemporaryReceipt(yest != null ? getSafeBigDecimal(yest.getCurrentTemporaryReceipt()) : BigDecimal.ZERO);
                }

                //应交报表数  =  his预交金 + his医疗收入
                dto.setReportAmount(dto.getHisAdvancePayment().add(dto.getHisMedicalIncome()));

                //汇总情况下，对应公式应该是  月末最后一天且非节假日也是如此
//                if (calculationType == 1 || Constant.HOLIDAY_NOT_MONTH_LASTDAY.equals(type)) {
//                    // 实交报表数据 = 应交报表数 - 前日暂收款 - 节假日暂收款
//                    dto.setActualReportAmount(dto.getReportAmount().subtract(dto.getPreviousTemporaryReceipt()).subtract(dto.getHolidayTemporaryReceipt()));
//                } else {
//                    // 实交报表数据 = 应交报表数 - 前日暂收款
//                    dto.setActualReportAmount(dto.getReportAmount().subtract(dto.getPreviousTemporaryReceipt()));
//                }

                //2026.04.23 要求修改 实交报表数据 = 应交报表数 - 前日暂收款 - 节假日暂收款
                dto.setActualReportAmount(dto.getReportAmount()
                        .subtract(dto.getPreviousTemporaryReceipt())
                        .subtract(dto.getHolidayTemporaryReceipt()));

                // 5.实收现金数 = 实收报表数 + 当日暂收款
                dto.setActualCashAmount(getSafeBigDecimal(dto.getActualReportAmount()).add(getSafeBigDecimal(dto.getCurrentTemporaryReceipt())));

                // 6.留存数差额 = 留存现金 - 备用金 + 实收报表数
                dto.setRetainedDifference(getSafeBigDecimal(dto.getRetainedCash())
                        .subtract(getSafeBigDecimal(dto.getPettyCash()))
                        .add(getSafeBigDecimal(dto.getActualReportAmount())));


                resultList.add(dto);
            }


            main.setSubs(resultList);


            return main;
        } catch (Exception e) {
            log.error("门诊报表生成失败", e);
            return null;
        }
    }

    /**
     * 获取月初月初数据
     *
     * @param body 报表请求体
     * @return 包含所有操作员计算结果的 ReportDTO 列表
     */
    public OutpCashMainEntity getOutpReportMonthStartData(OutpReportRequestBody body) {

        try {
            LocalDate currtDate = body.getReportDate();
            String pk = PrimaryKeyGenerator.generateKey();
            OutpCashMainEntity main = new OutpCashMainEntity();
            main.setSerialNo(pk);
            main.setReportDate(currtDate);
            main.setReportYear(body.getReportDate().getYear());
            main.setTotalFlag(Constant.HOLIDAY_MONTH_FIRST);
            main.setValidFlag("1");
            main.setCreateTime(LocalDateTime.now());
            List<YQOperatorEntity> operators = operatorService.findByCategory(Constant.TYPE_OUTP);
            // 增加历史日期查询缓存，避免 N+1 问题 ---
            List<OutpCashSubEntity> resultList = new ArrayList<>();

            //判断是否是月初统计报表
            if (body.getReportDate().getDayOfMonth() != 1) {
                //构建特殊情况，返回子对象展示单个列表
                OutpCashSubEntity sub = new OutpCashSubEntity();
                sub.setSerialNo(pk);
                sub.setSerialSubNo(PrimaryKeyGenerator.generateKey());
                sub.setOperatorName("当日暂收款");
                sub.setOperatorNo("当日暂收款");
                sub.setRemarks("查询月初统计报表，对应的日期 [" + currtDate.toString() + "] 不符合月初条件");
                resultList.add(sub);
            } else {
                for (YQOperatorEntity operator : operators) {
                    OutpCashSubEntity dto = new OutpCashSubEntity();
                    dto.setSerialNo(pk);
                    dto.setSerialSubNo(PrimaryKeyGenerator.generateKey());  //每一条生成唯一的编号
                    dto.setOperatorNo(operator.getOperatorNo());
                    dto.setDbUser(operator.getDbUser());
                    dto.setOperatorName(operator.getOperatorName());
                    dto.setPettyCash(operator.getPettyCash());
                    dto.setInpWindow(operator.getInpWindow());
                    dto.setAtm(operator.getAtm());
                    dto.setRowNum(operator.getRowNum());
                    resultList.add(dto);
                }
            }
            main.setSubs(resultList);
            return main;
        } catch (Exception e) {
            log.error("月初门诊报表生成失败", e);
            return null;
        }
    }


    /**
     * 封装回溯逻辑，缓存减少数据库IO
     */
    private void handleOutpBacktrackLogic(OutpCashSubEntity dto, LocalDate targetDate,
                                          LocalDate minDate, Map<LocalDate, OutpCashMainEntity> historyMap, String type) {

        BacktrackResult res = executeBacktrack(dto.getDbUser(), targetDate, minDate, historyMap, type);


        // 应交报表数  =  回溯结果的 周五+周六+周末 应交报表数
        dto.setHisAdvancePayment(res.backHisAdvancePayment);
        dto.setHisMedicalIncome(res.backHisMedicalIncome);

//        dto.setReportAmount(res.backReportAmount);
        // 前日暂收款  =  回溯结果的 周五的当日暂收款
        dto.setPreviousTemporaryReceipt(res.backPreviousTemporaryReceipt);
        // 节假日暂收款  =  回溯结果的 周六+周末 的 节假日暂收款录入
        dto.setHolidayTemporaryReceipt(res.backHolidayTemporaryReceipt);
    }


    private BacktrackResult executeBacktrack(String opNo, LocalDate targetDate, LocalDate minDate,
                                             Map<LocalDate, OutpCashMainEntity> historyMap, String type) {
        BacktrackResult result = new BacktrackResult();
        LocalDate current = targetDate.minusDays(1);

        //如果是月末最后一天回溯，需要包含当日的数据
        if (Constant.HOLIDAY_MONTH_LASTDAY.equals(type)) {
            current = targetDate;
        }

        // 没到 minDate 且 historyMap 有数据，就继续回溯
        while (!current.isBefore(minDate)) {


            OutpCashMainEntity dayMain = historyMap.get(current);

            if (dayMain == null) {
                log.error("回溯在 {} 发生数据断档: dayMain is null", current);
                break;
            }

            List<OutpCashSubEntity> subs = dayMain.getSubs();
            if (subs == null || subs.isEmpty()) {
                log.error("回溯在 {} 发生数据断档: subs is null or empty", current);
                break;
            }

            OutpCashSubEntity hist = subs.stream()
                    .filter(s -> opNo.equals(s.getDbUser()))
                    .findFirst()
                    .orElse(null);

            // 累加计算
            BigDecimal c1 = hist != null ? getSafeBigDecimal(hist.getHisAdvancePayment()) : BigDecimal.ZERO;
            BigDecimal c2 = hist != null ? getSafeBigDecimal(hist.getHisMedicalIncome()) : BigDecimal.ZERO;

            //节假日暂收款 = 周六+周末 节假日暂收款录入
            BigDecimal b = hist != null ? getSafeBigDecimal(hist.getHolidayTemporaryReceipt()) : BigDecimal.ZERO;


            // 2. 边界判定：如果这就是我们定位到的最小日期
            if (current.isEqual(minDate)) {
                // --- 边界特殊处理 ---   取值周五的当日暂收款（4）
                result.backPreviousTemporaryReceipt = hist != null ?
                        getSafeBigDecimal(hist.getCurrentTemporaryReceipt()) : BigDecimal.ZERO;

                //如果最小日期不是月初第一天，则需要对应节假日暂收款置为0
                if (minDate.getDayOfMonth() != 1) {
                    b = BigDecimal.ZERO;
                } else {
                    result.backHisAdvancePayment = result.backHisAdvancePayment.add(c1);
                    result.backHisMedicalIncome = result.backHisMedicalIncome.add(c2);
                    result.backHolidayTemporaryReceipt = result.backHolidayTemporaryReceipt.add(b);
                }

            } else {
                    result.backHisAdvancePayment = result.backHisAdvancePayment.add(c1);
                    result.backHisMedicalIncome = result.backHisMedicalIncome.add(c2);
                    result.backHolidayTemporaryReceipt = result.backHolidayTemporaryReceipt.add(b);
            }


            current = current.minusDays(1);

        }
        return result;
    }


    /**
     * 安全获取 BigDecimal 值，如果为 null 则返回 BigDecimal.ZERO
     */
    private BigDecimal getSafeBigDecimal(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }


}