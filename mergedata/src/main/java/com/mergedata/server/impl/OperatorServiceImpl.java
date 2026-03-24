package com.mergedata.server.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.mergedata.mapper.OperatorMapper;
import com.mergedata.model.dto.CommonRequestBody;
import com.mergedata.model.entity.InpCashSubEntity;
import com.mergedata.model.entity.YQOperatorEntity;
import com.mergedata.server.YQOperatorService;
import com.mergedata.util.PrimaryKeyGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class OperatorServiceImpl implements YQOperatorService {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Override
    public List<YQOperatorEntity> findAll() {
        return Db.lambdaQuery(YQOperatorEntity.class).orderByAsc(YQOperatorEntity::getRowNum).list();
    }

    @Override
    public List<YQOperatorEntity> findAllPlatform() {
        List<YQOperatorEntity> list = Db.lambdaQuery(YQOperatorEntity.class).orderByAsc(YQOperatorEntity::getRowNum).list();
//        list.forEach(this::convetSend);
        return list;
    }

    @Override
    public List<YQOperatorEntity> findByID(YQOperatorEntity operator) {

        return Db.lambdaQuery(YQOperatorEntity.class)
                .eq(YQOperatorEntity::getDbUser, operator.getDbUser())
                .eq(YQOperatorEntity::getCategory, operator.getCategory())
                .orderByAsc(YQOperatorEntity::getRowNum)
                .list();
    }

    @Override
    public List<YQOperatorEntity> findBySerialNo(YQOperatorEntity operator) {
        return Db.lambdaQuery(YQOperatorEntity.class)
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
     * 提供给平台查询的接口  类型  姓名或者ID
     *
     * @param body
     * @return
     */
    @Override
    public List<YQOperatorEntity> findByCategoryAndNameOrId(CommonRequestBody body) {
        String category = body.getExtendParams1() != null ? body.getExtendParams1() : "";
        String nameOrId = body.getExtendParams2() != null ? body.getExtendParams2() : "";


        List<YQOperatorEntity> list = Db.lambdaQuery(YQOperatorEntity.class)
                // A字段条件
                .eq(category != null && !category.isEmpty(), YQOperatorEntity::getCategory, category)
                // B和C字段的条件：b值可能匹配B或C
                .and(nameOrId != null && !nameOrId.isEmpty(), wrapper ->
                        wrapper.like(YQOperatorEntity::getOperatorName, nameOrId)
                                .or()
                                .eq(YQOperatorEntity::getDbUser, nameOrId)
                )
                .orderByAsc(YQOperatorEntity::getRowNum)
                .list();
        return list;
    }
    /**
     * 插入单条员工信息
     *
     * @param operator 员工实体
     * @return 是否成功
     */
    @Override
    public Boolean insert(YQOperatorEntity operator) {
        return Db.save(operator);
    }

    /*
     * 存在有数据则更新，无则写入
     */
    @Override
    public Boolean insertOrUpdate(YQOperatorEntity operator) {
        //需要判断是否有主键，来确定是修改还是写入
        if(operator.getSerialNo() == null || operator.getSerialNo().isEmpty()){
            operator.setSerialNo(PrimaryKeyGenerator.generateKey());
            //设置最大序号+ 1
            operator.setRowNum(queryMaxRownum());
        }
        //转换接收的清洗数据
//        convetRcv(operator);
        return Db.saveOrUpdate(operator);
    }

    /**
     * 批量插入员工信息
     *
     * @param entityList 员工实体列表
     * @return 是否成功
     */
    @Override
    @Transactional
    public Boolean batchInsert(List<YQOperatorEntity> entityList) {
        //查询出id
        for (YQOperatorEntity yqOperatorEntity : entityList) {


            if (findByID(yqOperatorEntity).size() > 0) {
                //移除这个id的
                entityList.remove(yqOperatorEntity);
            } else {
                PrimaryKeyGenerator pk = new PrimaryKeyGenerator();
                yqOperatorEntity.setSerialNo(pk.generateKey());
            }
        }

        return Db.saveBatch(entityList);
    }

    /**
     * 删除员工信息
     *
     * @param entity 员工实体
     * @return 是否成功
     */
    @Override
    public Boolean delete(YQOperatorEntity entity) {
        //查询出id
        if (entity.getSerialNo() == null) {
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
     *
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
                    .set(YQOperatorEntity::getCategory, entity.getCategory())
                    .set(YQOperatorEntity::getDbUser, entity.getDbUser())
                    .update();
        } else {
            insert(entity);
        }
    }

    /*
     * 查询出表中 序号最大的值
     */
    private Integer queryMaxRownum(){
        String sql = "SELECT NVL(MAX(row_num), 0) + 1 as nextVal FROM mpp_cash_reg_operator";
        Integer nextValue = jdbcTemplate.queryForObject(sql, Integer.class);
        return nextValue;
    }


    /*
     * 创建转换方法 ，转换平台传入来的时候的 写入操作员数据为 true和false 转为1 和 0
     */
    private void convetRcv(YQOperatorEntity operator) {
        if (operator.getInputFlag() != null) {
            operator.setInputFlag(operator.getInputFlag().equals("true") ? "1" : "0");
        }
        if (operator.getAtm() != null) {
            operator.setAtm(operator.getAtm().equals("true") ? "1" : "0");
        }
        if (operator.getInpWindow() != null) {
            operator.setInpWindow(operator.getInpWindow().equals("true") ? "1" : "0");
        }
    }
    /*
     * 创建转换方法 ，转换平台传出的时候的 操作员数据为  为1 和 0 转为 true和false
     */
    private void convetSend(YQOperatorEntity operator) {
        if (operator.getInputFlag() != null) {
            operator.setInputFlag(operator.getInputFlag().equals("1") ? "true" : "false");
        }
        if (operator.getAtm() != null) {
            operator.setAtm(operator.getAtm().equals("1") ? "true" : "false");
        }
        if (operator.getInpWindow() != null) {
            operator.setInpWindow(operator.getInpWindow().equals("1") ? "true" : "false");
        }
    }


}


