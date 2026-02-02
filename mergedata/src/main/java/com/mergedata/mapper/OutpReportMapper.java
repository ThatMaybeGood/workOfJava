package com.mergedata.mapper;

import com.mergedata.model.dto.OutpReportRequestBody;
import com.mergedata.model.entity.OutpCashSubEntity;
import com.mergedata.model.entity.OutpCashMainEntity;
import com.mergedata.model.vo.OutpReportVO;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDate;
import java.util.List;
/**
 * 门诊报表映射器
 */
@Mapper
public interface OutpReportMapper {

    List<OutpReportVO> selectByPk(String serialNo);

    /*
     * 新增查询：直接返回扁平化 Report 列表
     * 使用 ReportMap（无 <collection>）进行映射
     */
    List<OutpReportVO> selectReportByDate(LocalDate reportDate);

    /**
     * 查询：根据日期范围查询门诊报表数据
     */
    List<OutpReportVO> findReport(OutpReportRequestBody body);

    /* 查询：主从嵌套报表 */
    List<OutpCashMainEntity> selectByDate(LocalDate reportDate);

    /* 写入：批量插入主表 */
    int insertMain(OutpCashMainEntity main);

    /* 写入：批量插入明细表 */
    int batchInsertList(List<OutpCashMainEntity> list);

    /* 写入：批量插入明细表 */
    int batchInsertSubList(List<OutpCashSubEntity> list);

    /* 更新：按日期 */
    int updateByDate(LocalDate reportDate);

    /* 更新：按主键 */
    int updateByPK(String serialNo);

 }