package com.reports.mapper;

import com.reports.entity.PreHospOperEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 院前操作员维护 Mapper(报表库)
 */
@Mapper
public interface PreHospOperMapper {

    /** 维护列表(姓名本表直读, 不再回HIS回显) */
    List<PreHospOperEntity> queryList();

    /** 有效操作员工号列表(统计范围+下拉) */
    List<String> queryValidOperNos();

    /** 有效操作员(oper_no+oper_name, 统计结果姓名直读本表用) */
    List<PreHospOperEntity> queryValidOpers();

    /** 全部已维护工号(候选搜索排除用) */
    List<String> queryAllOperNos();

    /** 已维护人数(防重复添加) */
    int countByOperNo(@Param("operNo") String operNo);

    int insertOper(@Param("operNo") String operNo, @Param("dbuser") String dbuser, @Param("operName") String operName);

    int deleteById(@Param("id") Long id);

    int updateValid(@Param("id") Long id, @Param("validFlag") Integer validFlag);
}
