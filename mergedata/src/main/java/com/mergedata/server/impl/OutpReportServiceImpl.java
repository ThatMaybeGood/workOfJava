package com.mergedata.server.impl;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.mergedata.constants.Constant;
import com.mergedata.model.dto.OutpReportRequestBody;
import com.mergedata.model.entity.InpCashMainEntity;
import com.mergedata.model.entity.OutpCashMainEntity;
import com.mergedata.model.entity.OutpCashSubEntity;
import com.mergedata.model.vo.OutpReportMainVO;
import com.mergedata.model.vo.OutpReportSubVO;
import com.mergedata.server.OutpReportService;
import com.mergedata.util.PrimaryKeyGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class OutpReportServiceImpl implements OutpReportService {

    @Override
    public OutpCashMainEntity findByRequestBody(OutpReportRequestBody requestBody) {

        OutpCashMainEntity main = queryMainByDate(requestBody.getReportDate(), requestBody.getTotalFlag()).one();

        fillSubs(main);
        return main;
    }

    @Override
    public OutpCashMainEntity findByDate(LocalDate date, String totalFlag) {
        OutpCashMainEntity main = queryMainByDate(date, totalFlag).one();

        fillSubs(main);

        return main;
    }

    @Override
    public OutpCashMainEntity findByDateExclude(LocalDate date, String totalFlag) {
        OutpCashMainEntity main = queryMainByDate(date, totalFlag).one();

        fillSubsExclude(main);

        return main;
    }

    @Override
    public Long countByDate(LocalDate date, String totalFlag) {
        return queryMainByDate(date, totalFlag).count();
    }

    @Override
    public List<OutpCashMainEntity> findBatchByDateRangeAll(LocalDate startDate, LocalDate endDate, String totalFlag) {
        return findBatchByDateRangeCommon(startDate, endDate, totalFlag, 0);
    }

    @Override
    public List<OutpCashMainEntity> findBatchByDateRange(LocalDate startDate, LocalDate endDate, String isTotalFlag) {
        return findBatchByDateRangeCommon(startDate, endDate, isTotalFlag, 1);
    }
    /**
     * 查询时间范围内的下半部分数据,不包含合计之后的行
     */
    @Override
    public List<OutpCashMainEntity> findBatchByDateRangeForLowerValid  (LocalDate startDate, LocalDate endDate, String totalFlag) {
        return findBatchByDateRangeCommon(startDate, endDate, totalFlag, 2);
    }
    /**
     * 查询通用合并方法
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @param isTotalFlag 总结标志
     * @param validFlag 有效标志 1 上半有效 2 下半有效 0 全部
     * @return 主表实体列表
     */
    /**
     * 查询通用合并方法
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @param isTotalFlag 总结标志
     * @param validFlag 有效标志 1 上半有效 2 下半有效 0 全部
     * @return 主表实体列表
     */
    private List<OutpCashMainEntity> findBatchByDateRangeCommon(LocalDate startDate, LocalDate endDate, String isTotalFlag, long validFlag) {
        LambdaQueryChainWrapper<OutpCashMainEntity> query = Db.lambdaQuery(OutpCashMainEntity.class)
                .between(OutpCashMainEntity::getReportDate, startDate, endDate)
                .eq(OutpCashMainEntity::getValidFlag, Constant.YES);

        if ("1".equals(isTotalFlag)) {
            query.eq(OutpCashMainEntity::getTotalFlag, "1");
        } else {
            query.ne(OutpCashMainEntity::getTotalFlag, "1");
        }

        List<OutpCashMainEntity> mainList = query.list();
        if (CollectionUtils.isEmpty(mainList)) return mainList;

        // 1. 提取所有主表的 serialNo
        List<String> serialNos = mainList.stream()
                .map(OutpCashMainEntity::getSerialNo)
                .collect(Collectors.toList());

        // 2. 一次性查询所有相关从表记录
        List<OutpCashSubEntity> allSubs = Db.lambdaQuery(OutpCashSubEntity.class)
                .in(OutpCashSubEntity::getSerialNo, serialNos)
                .list();

        // 3. 过滤并分组从表
        final Map<String, List<OutpCashSubEntity>> subMap;
        if (validFlag == 0) {
            subMap = allSubs.stream()
                    .collect(Collectors.groupingBy(OutpCashSubEntity::getSerialNo));
        } else if (validFlag == 1) {
            subMap = allSubs.stream()
                    .filter(d -> d.getOperatorName() != null)
                    .filter(d -> !Constant.EXCLUDE_OPERATOR_NAMES.contains(d.getOperatorName()))
                    .collect(Collectors.groupingBy(OutpCashSubEntity::getSerialNo));
        } else {
            subMap = allSubs.stream()
                    .filter(d -> d.getOperatorName() != null)
                    .filter(d -> Constant.EXCLUDE_DOWN_NAMES.contains(d.getOperatorName()))
                    .collect(Collectors.groupingBy(OutpCashSubEntity::getSerialNo));
        }

        // 4. 如果是0，将月初2的两个字段累加到0
        if (!"1".equals(isTotalFlag)) {
            // 建立日期到serialNo的映射（只处理每月第一天）
            Map<LocalDate, String> dateToZeroSerial = mainList.stream()
                    .filter(m -> "0".equals(m.getTotalFlag()))
                    .filter(m -> m.getReportDate().getDayOfMonth() == 1)
                    .collect(Collectors.toMap(
                            OutpCashMainEntity::getReportDate,
                            OutpCashMainEntity::getSerialNo,
                            (k1, k2) -> k1
                    ));

            Map<LocalDate, String> dateToTwoSerial = mainList.stream()
                    .filter(m -> "2".equals(m.getTotalFlag()))
                    .filter(m -> m.getReportDate().getDayOfMonth() == 1)
                    .collect(Collectors.toMap(
                            OutpCashMainEntity::getReportDate,
                            OutpCashMainEntity::getSerialNo,
                            (k1, k2) -> k1
                    ));

            // 遍历有2的日期，累加两个字段到0
            for (Map.Entry<LocalDate, String> entry : dateToTwoSerial.entrySet()) {
                LocalDate date = entry.getKey();
                String twoSerial = entry.getValue();
                String zeroSerial = dateToZeroSerial.get(date);

                if (zeroSerial != null) {
                    // 获取0的从表（按操作员分组）
                    Map<String, OutpCashSubEntity> zeroSubMap = subMap.getOrDefault(zeroSerial, new ArrayList<>())
                            .stream()
                            .filter(s -> s.getOperatorName() != null)
                            .collect(Collectors.toMap(
                                    OutpCashSubEntity::getOperatorName,
                                    Function.identity(),
                                    (k1, k2) -> k1
                            ));

                    // 遍历2的从表，累加两个字段
                    for (OutpCashSubEntity twoSub : subMap.getOrDefault(twoSerial, new ArrayList<>())) {
                        String operatorName = twoSub.getOperatorName();
                        if (operatorName != null) {
                            OutpCashSubEntity zeroSub = zeroSubMap.get(operatorName);
                            if (zeroSub != null) {
                                // 相同操作员：累加两个字段
                                zeroSub.setCurrentTemporaryReceipt(
                                        (zeroSub.getCurrentTemporaryReceipt() == null ? BigDecimal.ZERO : zeroSub.getCurrentTemporaryReceipt())
                                                .add(twoSub.getCurrentTemporaryReceipt() == null ? BigDecimal.ZERO : twoSub.getCurrentTemporaryReceipt())
                                );
                                zeroSub.setHolidayTemporaryReceipt(
                                        (zeroSub.getHolidayTemporaryReceipt() == null ? BigDecimal.ZERO : zeroSub.getHolidayTemporaryReceipt())
                                                .add(twoSub.getHolidayTemporaryReceipt() == null ? BigDecimal.ZERO : twoSub.getHolidayTemporaryReceipt())
                                );
                            } else {
                                // 不同操作员：直接添加到0的从表
                                subMap.get(zeroSerial).add(twoSub);
                            }
                        }
                    }
                }
            }

            // 移除2的记录
            for (String twoSerial : dateToTwoSerial.values()) {
                subMap.remove(twoSerial);
            }
            mainList.removeIf(main -> "2".equals(main.getTotalFlag()));
        }

        // 5. 设置从表
        mainList.forEach(main -> main.setSubs(subMap.getOrDefault(main.getSerialNo(), Collections.emptyList())));
        return mainList;
    }
    //    /**
//     * 查询通用合并方法
//     *
//     * @param startDate 开始日期
//     * @param endDate   结束日期
//     * @param isTotalFlag 是否汇总
//     * @param validFlag 有效标志 1 上半有效 2 下半有效 0 全部
//     * @return 主表实体列表
//     */
//    private List<OutpCashMainEntity> findBatchByDateRangeCommon(LocalDate startDate, LocalDate endDate, String isTotalFlag, long validFlag) {
//
//        // 构建基础查询
//        LambdaQueryChainWrapper<OutpCashMainEntity> query = Db.lambdaQuery(OutpCashMainEntity.class)
//                .between(OutpCashMainEntity::getReportDate, startDate, endDate)
//                .eq(OutpCashMainEntity::getValidFlag, Constant.YES);
//
//        // 动态处理 totalFlag 查询条件
//        if ("1".equals(isTotalFlag)) {
//            // 查询汇总记录
//            query.eq(OutpCashMainEntity::getTotalFlag, "1");
//        } else {
//            // 查询非汇总记录（排他）
//            query.ne(OutpCashMainEntity::getTotalFlag, "1");
//        }
//        List<OutpCashMainEntity> mainList = query.list();
//
//        if (CollectionUtils.isEmpty(mainList)) return mainList;
//
//        // 1. 提取所有主表的 serialNo
//        List<String> serialNos = mainList.stream().map(OutpCashMainEntity::getSerialNo).collect(Collectors.toList());
//
//        // 2. 一次性查询所有相关从表记录
//        List<OutpCashSubEntity> allSubs = Db.lambdaQuery(OutpCashSubEntity.class)
//                .in(OutpCashSubEntity::getSerialNo, serialNos)
//                .list();
//
//        final Map<String, List<OutpCashSubEntity>> subMap;
//
//        if (validFlag == 0) {
//            // 不过滤，全部保留
//            subMap = allSubs.stream()
//                    .collect(Collectors.groupingBy(OutpCashSubEntity::getSerialNo));
//        } else if (validFlag == 1) {
//            // 上半有效：排除操作员
//            subMap = allSubs.stream()
//                    .filter(d -> d.getOperatorName() != null)
//                    .filter(d -> !Constant.EXCLUDE_OPERATOR_NAMES.contains(d.getOperatorName()))
//                    .collect(Collectors.groupingBy(OutpCashSubEntity::getSerialNo));
//        } else {
//            // 下半有效：只保留排除项
//            subMap = allSubs.stream()
//                    .filter(d -> d.getOperatorName() != null)
//                    .filter(d -> Constant.EXCLUDE_DOWN_NAMES.contains(d.getOperatorName()))
//                    .collect(Collectors.groupingBy(OutpCashSubEntity::getSerialNo));
//        }
//
//        mainList.forEach(main -> main.setSubs(subMap.getOrDefault(main.getSerialNo(), Collections.emptyList())));
//        return mainList;
//    }

    @Override
    public OutpCashMainEntity findByPk(String serialNo) {

        OutpCashMainEntity main = Db.lambdaQuery(OutpCashMainEntity.class)
                .eq(OutpCashMainEntity::getSerialNo, serialNo)
                .one();

        fillSubs(main);
        return main;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void insert(OutpCashMainEntity entity) {
        // 保存主表 (Order)
        Db.save(entity);

        // 保存从表 (OrderItem)
        if (CollectionUtils.isNotEmpty(entity.getSubs())) {
//            entity.getSubs().forEach(sub -> sub.setSerialNo(entity.getSerialNo()));
            Db.saveBatch(entity.getSubs());
        }

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean insertOrUpdate(OutpCashMainEntity entity) {
        //先修改  后写入
        updateByDate(entity.getReportDate(), entity.getTotalFlag());
        insert(entity);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean update(OutpCashMainEntity entity) {
        return Db.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateByDate(LocalDate date, String totalFlag) {
        return Db.lambdaUpdate(OutpCashMainEntity.class)
                .eq(OutpCashMainEntity::getReportDate, date)
                .eq(OutpCashMainEntity::getTotalFlag, totalFlag)
                .eq(OutpCashMainEntity::getValidFlag, Constant.YES)
                .set(OutpCashMainEntity::getValidFlag, Constant.NO)
                .update();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(OutpCashMainEntity entity) {
        // 先删从表，再删主表
        Db.lambdaUpdate(OutpCashSubEntity.class)
                .eq(OutpCashSubEntity::getSerialNo, entity.getSerialNo())
                .remove();
        return Db.removeById(entity);
    }

    @Override
    public OutpReportMainVO getAuditReport(OutpReportRequestBody body) {
        OutpCashMainEntity main = findByRequestBody(body);
        return outpExchangeDbToView(main);
    }


    @Override
    public void saveAuditReport(OutpReportMainVO mainVO) {
        LocalDate reportDate = mainVO.getSubList().get(0).getReportDate();
        if (reportDate == null) {
            reportDate = LocalDate.now();
        }

        String remark = "";
        for (OutpReportSubVO sub : mainVO.getSubList()) {
            if (sub.getOperatorName().equals("当日暂收款")) {
                remark = sub.getRemarks();
            }
        }
        OutpCashMainEntity main = outpExchangeViewToDb(reportDate, mainVO, remark);

        main.getSubs().forEach(sub -> {
            Db.lambdaUpdate(OutpCashSubEntity.class)
                    .eq(OutpCashSubEntity::getSerialSubNo, sub.getSerialSubNo())
                    .set(OutpCashSubEntity::getAuditFlag, sub.getAuditFlag())
                    .update();
        });
    }

    // 填充从表数据
    private void fillSubs(OutpCashMainEntity main) {
        if (main != null) {
            List<OutpCashSubEntity> items = Db.lambdaQuery(OutpCashSubEntity.class)
                    .eq(OutpCashSubEntity::getSerialNo, main.getSerialNo())
                    .orderByAsc(OutpCashSubEntity::getRowNum)
                    .orderByAsc(OutpCashSubEntity::getId)
                    .list();

            main.setSubs(items);
        }
    }

    // 填充从表数据,不包含合计之后的
    private void fillSubsExclude(OutpCashMainEntity main) {
        if (main != null) {
            List<OutpCashSubEntity> items = Db.lambdaQuery(OutpCashSubEntity.class)
                    .eq(OutpCashSubEntity::getSerialNo, main.getSerialNo())
                    .orderByAsc(OutpCashSubEntity::getRowNum)
                    .orderByAsc(OutpCashSubEntity::getId)
                    .list();

            // 过滤掉不是操作员的行
            items = items.stream()
                    .filter(d -> !Constant.EXCLUDE_OPERATOR_NAMES.contains(d.getOperatorName()))
                    .collect(Collectors.toList());

            main.setSubs(items);
        }
    }

    private LambdaQueryChainWrapper<OutpCashMainEntity> queryMainByDate(LocalDate date, String totalFlag) {
        return Db.lambdaQuery(OutpCashMainEntity.class)
                .eq(OutpCashMainEntity::getReportDate, date)
                .eq(OutpCashMainEntity::getTotalFlag, totalFlag)
                .eq(OutpCashMainEntity::getValidFlag, Constant.YES);
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


}
