package com.mergedata.server.impl;

import com.mergedata.mapper.OperatorMapper;
import com.mergedata.model.entity.YQOperatorEntity;
import com.mergedata.server.YQOperatorService;
import com.mergedata.util.PrimaryKeyGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class OperatorServiceImpl implements YQOperatorService {


    @Autowired
    OperatorMapper operatorMapper;


    @Override
    public List<YQOperatorEntity> findAll() {
        return  operatorMapper.selectAll();
     }

    @Override
    public List<YQOperatorEntity> findByID(YQOperatorEntity operator) {

        return operatorMapper.selectByID(operator.getOperatorNo());
    }

     /**
      * 插入单条员工信息
      * @param yqOperatorsEntity 员工实体
      * @return 是否成功
      */
    @Override
    public Boolean insert(YQOperatorEntity yqOperatorsEntity) {
        int size = operatorMapper.selectAll().size();

        yqOperatorsEntity.setRowNum(size+1);

        PrimaryKeyGenerator pk = new PrimaryKeyGenerator();

        yqOperatorsEntity.setSerialNo(pk.generateKey());

        int insert = operatorMapper.insert(yqOperatorsEntity);

        return insert > 0;
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

            if (operatorMapper.selectByID(yqOperatorEntity.getOperatorNo()).size() > 0){
                //移除这个id的
                yqOperatorEntities.remove(yqOperatorEntity);
            }
        }

        int i = operatorMapper.batchInsertList(yqOperatorEntities);
        return i > 0;
    }
     /**
      * 删除员工信息
      * @param yqOperatorEntity 员工实体
      * @return 是否成功
      */
    @Override
    public Boolean delete(YQOperatorEntity yqOperatorEntity) {
        //查询出id
        if ( yqOperatorEntity.getOperatorNo() == null) {
            return false;
        }
        return operatorMapper.delete(yqOperatorEntity.getOperatorNo()) > 0;
    }
     /**
      * 更新员工信息
      * @param yqOperatorEntity 员工实体
      * @return 是否成功
      */
    @Override
    public Boolean update(YQOperatorEntity yqOperatorEntity) {
        return null;
    }
}


