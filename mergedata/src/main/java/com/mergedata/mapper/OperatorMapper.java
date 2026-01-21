package com.mergedata.mapper;

import com.mergedata.model.entity.YQOperatorEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface OperatorMapper {

    List<YQOperatorEntity> selectAll();

    /*
     * 根据员工ID查询员工信息
     * @Param 指定参数名为 operatorNo，XML中将使用 #{operatorNo}
     */
    List<YQOperatorEntity> selectByID(@Param("operatorNo")String operatorNo);

    /*
     * 插入单条员工信息
     */
    int insert(YQOperatorEntity yqOperatorsEntity);
    /*
     * 批量插入员工列表
     */
    int batchInsertList(List<YQOperatorEntity> yqOperatorEntities);


    int delete(String operatorNo);


}
