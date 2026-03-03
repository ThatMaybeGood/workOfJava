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
      * @param yqOperatorsEntity 员工实体
      * @return 是否成功
      */
    @Override
    public Boolean insert(YQOperatorEntity yqOperatorsEntity) {
        long size = Db.lambdaQuery(YQOperatorEntity.class).count();

        List<YQOperatorEntity> list =  findBySerialNo(yqOperatorsEntity);

        if (!list.isEmpty() || list.size() != 0 ) {
           return update(yqOperatorsEntity);
        }

        PrimaryKeyGenerator pk = new PrimaryKeyGenerator();
        yqOperatorsEntity.setSerialNo(pk.generateKey());
        yqOperatorsEntity.setRowNum((int) (size+1));

        // 增加判断兼容 atm 和inpWindow 兼容 true和false传入 转换为 1 和 0
        if (yqOperatorsEntity.getAtm().equals("true")){
            yqOperatorsEntity.setAtm("1");
        }
        if (yqOperatorsEntity.getInpWindow().equals("true")){
            yqOperatorsEntity.setInpWindow("1");
        }

        return Db.save(yqOperatorsEntity);
    }

     /**
      * 批量插入员工信息
      * @param yqOperatorEntities 员工实体列表
      * @return 是否成功
      */
    @Override
    @Transactional
    public Boolean batchInsert(List<YQOperatorEntity> yqOperatorEntities) {
        //查询出id
        for (YQOperatorEntity yqOperatorEntity : yqOperatorEntities) {
            PrimaryKeyGenerator pk = new PrimaryKeyGenerator();

            yqOperatorEntity.setSerialNo(pk.generateKey());

            if (findByID(yqOperatorEntity).size() > 0){
                //移除这个id的
                yqOperatorEntities.remove(yqOperatorEntity);
            }
        }

        return Db.saveBatch(yqOperatorEntities);
    }
     /**
      * 删除员工信息
      * @param yqOperatorEntity 员工实体
      * @return 是否成功
      */
    @Override
    public Boolean delete(YQOperatorEntity yqOperatorEntity) {
        //查询出id
        if ( yqOperatorEntity.getSerialNo() == null) {
            return false;
        }
        //通过流水号删除  removebyid必须实体类指定主键
        return Db.removeById(yqOperatorEntity.getSerialNo(), YQOperatorEntity.class);


//        /**
//         * 根据条件删除
//         */
//            return Db.lambdaUpdate(YQOperatorEntity.class)
//                    .eq(YQOperatorEntity::getSerialNo, yqOperatorEntity.getSerialNo())
//                    .remove();

    }
     /**
      * 更新员工信息
      * @param yqOperatorEntity 员工实体
      * @return 是否成功
      */
    @Override
    public Boolean update(YQOperatorEntity yqOperatorEntity) {
        return Db.updateById(yqOperatorEntity);
    }
}


