package com.mergedata.server.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.mergedata.mapper.OperatorMapper;
import com.mergedata.model.entity.InpCashSubEntity;
import com.mergedata.model.entity.YQOperatorEntity;
import com.mergedata.server.YQOperatorService;
import com.mergedata.util.PrimaryKeyGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
public class OperatorServiceImpl implements YQOperatorService {


    @Autowired
    OperatorMapper operatorMapper;


    @Override
    public List<YQOperatorEntity> findAll() {

        return  Db.lambdaQuery(YQOperatorEntity.class).orderByAsc(YQOperatorEntity::getRowNum).list();
//                Db.list(new LambdaQueryWrapper<>(YQOperatorEntity.class));

     }

    @Override
    public List<YQOperatorEntity> findByID(YQOperatorEntity operator) {

        return  Db.lambdaQuery(YQOperatorEntity.class)
                .eq(YQOperatorEntity::getDbUser, operator.getDbUser())
                .orderByAsc(YQOperatorEntity::getRowNum)
                .list();
    }

    @Override
    public List<YQOperatorEntity> findBySerialNo(YQOperatorEntity operator) {
        return  Db.lambdaQuery(YQOperatorEntity.class)
                .eq(YQOperatorEntity::getSerialNo, operator.getSerialNo())
                .orderByAsc(YQOperatorEntity::getRowNum)
                .list();
    }

    @Override
    public List<YQOperatorEntity> findByCategory(String category) {

        List<YQOperatorEntity> subs = Db.lambdaQuery(YQOperatorEntity.class)
                .eq(YQOperatorEntity::getCategory, category)
                .orderByAsc(YQOperatorEntity::getRowNum)
                .list();

        return subs;
    }

    /**
      * 插入单条员工信息
      * @param entity 员工实体
      * @return 是否成功
      */
    @Override
    public Boolean insert(YQOperatorEntity entity) {
        long size = Db.lambdaQuery(YQOperatorEntity.class).count();

        List<YQOperatorEntity> list =  findBySerialNo(entity);

        if (!list.isEmpty() || list.size() != 0 ) {
           return update(entity);
        }

        PrimaryKeyGenerator pk = new PrimaryKeyGenerator();
        entity.setSerialNo(pk.generateKey());
        entity.setRowNum((int) (size+1));

        // 增加判断兼容 atm 和inpWindow 兼容 true和false传入 转换为 1 和 0
        if (entity.getAtm().equals("true")){
            entity.setAtm("1");
        }
        if (entity.getInpWindow().equals("true")){
            entity.setInpWindow("1");
        }

        return Db.save(entity);
    }

     /**
      * 批量插入员工信息
      * @param entityList 员工实体列表
      * @return 是否成功
      */
    @Override
    @Transactional
    public Boolean batchInsert(List<YQOperatorEntity> entityList) {
        //查询出id
        for (YQOperatorEntity yqOperatorEntity : entityList) {


            if (findByID(yqOperatorEntity).size() > 0){
                //移除这个id的
                entityList.remove(yqOperatorEntity);
            }else {
                PrimaryKeyGenerator pk = new PrimaryKeyGenerator();
                yqOperatorEntity.setSerialNo(pk.generateKey());
            }


        }

        return Db.saveBatch(entityList);
    }
     /**
      * 删除员工信息
      * @param entity 员工实体
      * @return 是否成功
      */
    @Override
    public Boolean delete(YQOperatorEntity entity) {
        //查询出id
        if ( entity.getSerialNo() == null) {
            return false;
        }
        //通过流水号删除  removebyid必须实体类指定主键
        return Db.removeById(entity.getSerialNo(), YQOperatorEntity.class);


//        /**
//         * 根据条件删除
//         */
//            return Db.lambdaUpdate(YQOperatorEntity.class)
//                    .eq(YQOperatorEntity::getSerialNo, yqOperatorEntity.getSerialNo())
//                    .remove();

    }
     /**
      * 更新员工信息
      * @param entity 员工实体
      * @return 是否成功
      */
    @Override
    public Boolean update(YQOperatorEntity entity) {
        return Db.updateById(entity);
    }

    /**
     * 同步his更新员工信息
     */
    public void syncUpdate(YQOperatorEntity entity) {
        List<YQOperatorEntity> list = Db.lambdaQuery(YQOperatorEntity.class)
                .eq(YQOperatorEntity::getOperatorNo, entity.getOperatorNo())
                .list();
        if (list.size() > 0) {
            Db.lambdaUpdate(YQOperatorEntity.class)
                    .eq(YQOperatorEntity::getOperatorNo, entity.getOperatorNo())
                    .set(YQOperatorEntity::getDbUser, entity.getDbUser())
                    .update();
        } else {
            insert(entity);
        }
    }

    }


