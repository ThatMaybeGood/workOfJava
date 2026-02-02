package com.mergedata.mapper;

import com.mergedata.model.dto.OutpReportRequestBody;
import com.mergedata.model.entity.OutpCashSubEntity;
import com.mergedata.model.entity.OutpCashMainEntity;
import com.mergedata.model.vo.OutpReportVO;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDate;
import java.util.List;

/**
 * 住院报表映射器
 */
@Mapper
public interface InpReportMapper {

    /* 查询：扁平化报表 */
    List<OutpReportVO> selectByPk(String serialNo);

    /*
     * 新增查询：直接返回扁平化 Report 列表
     * 使用 ReportMap（无 <collection>）进行映射
     */
    List<OutpReportVO> selectReportByDate(LocalDate reportDate);

    List<OutpReportVO> findReport(OutpReportRequestBody body);

    /* 查询：主从嵌套报表 */
    List<OutpCashMainEntity> selectByDate(LocalDate reportDate);

    /* 写入：批量插入明细表 */
    int batchInsertList(List<OutpCashMainEntity> list);

    /* 写入：批量插入明细表 */
    int batchInsertSubList(List<OutpCashSubEntity> list);

    /* 更新：按日期 */
    int updateByDate(LocalDate reportDate);

 }