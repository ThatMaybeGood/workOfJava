package com.mergedata.mapper;

import com.mergedata.model.dto.ReportRequestBody;
import com.mergedata.model.entity.CashStatisticsSubEntity;
import com.mergedata.model.entity.CashStattisticsMainEntity;
import com.mergedata.model.dto.ReportDTO;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface ReportMapper {

    /* 查询：扁平化报表 */
    List<ReportDTO> selectByPk(String serialNo);

    /*
     * 新增查询：直接返回扁平化 Report 列表
     * 使用 ReportMap（无 <collection>）进行映射
     */
    List<ReportDTO> selectReportByDate(LocalDate reportDate);

    List<ReportDTO> findReport(ReportRequestBody body);

    /* 查询：主从嵌套报表 */
    List<CashStattisticsMainEntity> selectByDate(LocalDate reportDate);

    /* 写入：批量插入主表 */
    int insertMain(CashStattisticsMainEntity main);

    /* 写入：批量插入明细表 */
    int batchInsertList(List<CashStattisticsMainEntity> list);

    /* 写入：批量插入明细表 */
    int batchInsertSubList(List<CashStatisticsSubEntity> list);

    /* 更新：按日期 */
    int updateByDate(LocalDate reportDate);

    /* 更新：按主键 */
    int updateByPK(String serialNo);

 }