package com.mergedata.mapper;

import com.mergedata.model.CashStattisticsMain;
import com.mergedata.model.CashStatisticsSub;
import com.mergedata.model.Report;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface ReportMapper {

    /* 查询：扁平化报表 */
    List<Report> selectByPk(String serialNo);

    /*
     * 新增查询：直接返回扁平化 Report 列表
     * 使用 ReportMap（无 <collection>）进行映射
     */
    List<Report> selectReportByDate(LocalDate reportDate);


    /* 查询：主从嵌套报表 */
    List<CashStattisticsMain> selectByDate(LocalDate reportDate);

    /* 写入：批量插入主表 */
    int batchInsertList(List<CashStattisticsMain> list);

    /* 写入：批量插入明细表 */
    int batchInsertSubList(List<CashStatisticsSub> list);

    /* 更新：按日期 */
    int updateByDate(LocalDate reportDate);

    /* 更新：按主键 */
    int updateByPK(String serialNo);

 }