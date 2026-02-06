package com.mergedata.mapper;

import com.mergedata.model.entity.YQOperatorEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.cache.annotation.Cacheable;

import java.util.List;

@Mapper
public interface OperatorMapper {

    /**
     * 查询所有员工信息
     * @return 所有员工列表
     */
    List<YQOperatorEntity> selectAll();


    /**
     * 根据类型查询员工列表
     * @param type 员工类型 门诊/住院 0/1
     * @return 符合条件的员工列表
     * 缓存策略：根据类型查询员工列表，缓存到 "operators" 缓存中，缓存键为 type 参数值
     */
    @Cacheable(value = "operators", key = "#type")
    List<YQOperatorEntity> selectByCategory(@Param("category") String type);

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
