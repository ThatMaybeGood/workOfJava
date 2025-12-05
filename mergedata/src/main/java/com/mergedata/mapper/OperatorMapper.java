package com.mergedata.mapper;

import com.mergedata.model.YQOperator;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface OperatorMapper {

    List<YQOperator> selectAll();

    /*
     * 根据员工ID查询员工信息
     * @Param 指定参数名为 operatorNo，XML中将使用 #{operatorNo}
     */
    List<YQOperator> selectByID(@Param("operatorNo")String operatorNo);

    /*
     * 插入单条员工信息
     */
    int insert(YQOperator yqOperators);
    /*
     * 批量插入员工列表
     */
    int batchInsertList(List<YQOperator> yqOperators);


    int update(String serialNo);


}
