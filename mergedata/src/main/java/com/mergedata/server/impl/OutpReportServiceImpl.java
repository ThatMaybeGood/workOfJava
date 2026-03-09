package com.mergedata.server.impl;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
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

@Slf4j
@Service
public class OutpReportServiceImpl implements OutpReportService {

    @Override
    public OutpCashMainEntity findByRequestBody(OutpReportRequestBody requestBody) {

        OutpCashMainEntity main = Db.lambdaQuery(OutpCashMainEntity.class)
                .eq(OutpCashMainEntity::getReportDate, requestBody.getReportDate())
                .eq(OutpCashMainEntity::getTotalFlag, requestBody.getTotalFlag())
                .eq(OutpCashMainEntity::getValidFlag, Constant.YES)
                .one();

        fillSubs(main);
        return main;
    }

    @Override
    public OutpCashMainEntity findByDate(LocalDate date,String totalFlag) {
        OutpCashMainEntity main = Db.lambdaQuery(OutpCashMainEntity.class)
                .eq(OutpCashMainEntity::getReportDate, date)
                .eq(OutpCashMainEntity::getTotalFlag, totalFlag)
                .eq(OutpCashMainEntity::getValidFlag, Constant.YES)
                .one();

        fillSubs(main);

        return main;
    }

    @Override
    public Long countByDate(LocalDate date, String totalFlag) {

        return Db.lambdaQuery(OutpCashMainEntity.class)
                .eq(OutpCashMainEntity::getReportDate, date)
                .eq(OutpCashMainEntity::getTotalFlag, totalFlag)
                .eq(OutpCashMainEntity::getValidFlag, Constant.YES)
                .count();
    }

    @Override
    public List<OutpCashMainEntity> findBatchByDateRange(LocalDate startDate, LocalDate endDate) {
        return Collections.emptyList();
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
                .set(OutpCashMainEntity::getValidFlag, Constant.NO)
                .eq(OutpCashMainEntity::getReportDate, date)
                .eq(OutpCashMainEntity::getTotalFlag, totalFlag)
                .eq(OutpCashMainEntity::getValidFlag, Constant.YES)
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

}
