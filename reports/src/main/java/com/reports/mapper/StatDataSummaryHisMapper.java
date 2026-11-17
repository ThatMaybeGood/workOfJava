package com.reports.mapper;

import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 运营数据统计-HIS出诊单元(US7ASCII中文走GBK hex, SQL在 StatDataSummaryHisMapper.xml)
 */
public interface StatDataSummaryHisMapper {

    /** bt6 分析tab： 医生出诊单元(汇总) */
    Map<String, Object> bt6ClinicUnitOverall(@Param("startDate") String startDate, @Param("endDate") String endDate,
                                             @Param("olNurseHex") String olNurseHex, @Param("olPharmHex") String olPharmHex,
                                             @Param("olTeamHex") String olTeamHex, @Param("revisitHex") String revisitHex,
                                             @Param("quankeHex") String quankeHex);

    /** bt6 分析tab： 医生出诊单元(按日) */
    List<Map<String, Object>> bt6ClinicUnitDaily(@Param("startDate") String startDate, @Param("endDate") String endDate,
                                                 @Param("olNurseHex") String olNurseHex, @Param("olPharmHex") String olPharmHex,
                                                 @Param("olTeamHex") String olTeamHex, @Param("revisitHex") String revisitHex,
                                                 @Param("quankeHex") String quankeHex);

}
