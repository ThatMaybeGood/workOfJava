package com.mergedata.server.impl;

import com.mergedata.mapper.OperatorMapper;
import com.mergedata.model.entity.YQOperatorEntity;
import com.mergedata.server.YQOperatorService;
import com.mergedata.util.PrimaryKeyGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    @Override
    public Boolean insert(YQOperatorEntity yqOperatorsEntity) {
        int size = operatorMapper.selectAll().size();

        yqOperatorsEntity.setRowNum(size+1);

        PrimaryKeyGenerator pk = new PrimaryKeyGenerator();

        yqOperatorsEntity.setSerialNo(pk.generateKey());

        int insert = operatorMapper.insert(yqOperatorsEntity);

        return insert > 0;
    }

    @Override
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

    @Override
    public Boolean delete(YQOperatorEntity yqOperatorEntity) {
        //查询出id
        if ( yqOperatorEntity.getOperatorNo() == null) {
            return false;
        }
        return operatorMapper.delete(yqOperatorEntity.getOperatorNo()) > 0;
    }

    @Override
    public Boolean update(YQOperatorEntity yqOperatorEntity) {
        return null;
    }
}


