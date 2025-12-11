package com.mergedata.server.impl;

import com.mergedata.mapper.OperatorMapper;
import com.mergedata.model.YQOperator;
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
    public List<YQOperator> findAll() {
        return  operatorMapper.selectAll();
     }

    @Override
    public List<YQOperator> findByID(YQOperator operator) {

        return operatorMapper.selectByID(operator.getOperatorNo());
    }

    @Override
    public Boolean insert(YQOperator yqOperators) {
        PrimaryKeyGenerator pk = new PrimaryKeyGenerator();

        yqOperators.setSerialNo(pk.generateKey());

        int insert = operatorMapper.insert(yqOperators);

        return insert > 0;
    }

    @Override
    public Boolean batchInsert(List<YQOperator> yqOperators) {
        //查询出id
        for (YQOperator yqOperator : yqOperators) {
            PrimaryKeyGenerator pk = new PrimaryKeyGenerator();

            yqOperator.setSerialNo(pk.generateKey());

            if (operatorMapper.selectByID(yqOperator.getOperatorNo()).size() > 0){
                //移除这个id的
                yqOperators.remove(yqOperator);
            }
        }

        int i = operatorMapper.batchInsertList(yqOperators);
        return i > 0;
    }

    @Override
    public Boolean delete(YQOperator yqOperator) {
        //查询出id
        if ( yqOperator.getOperatorNo() == null) {
            return false;
        }
        return operatorMapper.delete(yqOperator.getOperatorNo()) > 0;
    }

    @Override
    public Boolean update(YQOperator yqOperator) {
        return null;
    }
}


