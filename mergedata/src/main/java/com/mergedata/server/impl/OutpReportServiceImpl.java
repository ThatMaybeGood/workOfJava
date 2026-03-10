package com.mergedata.server.impl;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.mergedata.constants.Constant;
import com.mergedata.model.dto.OutpReportRequestBody;
import com.mergedata.model.entity.OutpCashMainEntity;
import com.mergedata.model.entity.OutpCashSubEntity;
import com.mergedata.server.OutpReportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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
    public OutpCashMainEntity findByDate(LocalDate date,String totalFlag) {
        OutpCashMainEntity main = queryMainByDate(date, totalFlag).one();

        fillSubs(main);

        return main;
    }

    @Override
    public Long countByDate(LocalDate date, String totalFlag) {

        return queryMainByDate(date, totalFlag).count();
    }

    @Override
    public List<OutpCashMainEntity> findBatchByDateRange(LocalDate startDate, LocalDate endDate,String totalFlag) {

        List<OutpCashMainEntity> mainList = Db.lambdaQuery(OutpCashMainEntity.class)
                .between(OutpCashMainEntity::getReportDate, startDate, endDate)
                .eq(OutpCashMainEntity::getTotalFlag, totalFlag)   //只查询不是汇总标志的
                .eq(OutpCashMainEntity::getValidFlag, Constant.YES)
                .list();

        if (CollectionUtils.isEmpty(mainList)) return mainList;

        // 1. 提取所有主表的 serialNo
        List<String> serialNos = mainList.stream().map(OutpCashMainEntity::getSerialNo).collect(Collectors.toList());

        // 2. 一次性查询所有相关从表记录
        List<OutpCashSubEntity> allSubs = Db.lambdaQuery(OutpCashSubEntity.class)
                .in(OutpCashSubEntity::getSerialNo, serialNos)
                .list();

        // 3. 在内存中进行分组并填充 (使用 Map 提高查找效率)
        Map<String, List<OutpCashSubEntity>> subMap = allSubs.stream()
                .collect(Collectors.groupingBy(OutpCashSubEntity::getSerialNo));

        mainList.forEach(main -> main.setSubs(subMap.getOrDefault(main.getSerialNo(), Collections.emptyList())));

        return mainList;
    }

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

    // 填充从表数据
    private void fillSubs(OutpCashMainEntity main) {
        if (main != null) {
            List<OutpCashSubEntity> items = Db.lambdaQuery(OutpCashSubEntity.class)
                    .eq(OutpCashSubEntity::getSerialNo, main.getSerialNo())
                    .orderByAsc(OutpCashSubEntity::getId)
                    .list();
            main.setSubs(items);
        }
    }

    private LambdaQueryChainWrapper<OutpCashMainEntity> queryMainByDate(LocalDate date, String totalFlag) {
        return Db.lambdaQuery(OutpCashMainEntity.class)
                .eq(OutpCashMainEntity::getReportDate, date)
                .eq(OutpCashMainEntity::getTotalFlag, totalFlag)
                .eq(OutpCashMainEntity::getValidFlag, Constant.YES);
    }

}
