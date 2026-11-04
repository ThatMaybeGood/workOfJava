package com.reports.mapper;

import com.reports.dto.response.cash.mi.MiReconOrgDtlItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 医保对账-医疗机构侧 Mapper(HIS库, 占坑: SQL待补全)
 */
@Mapper
public interface MiReconMedInstMapper {

    /** 医疗机构侧原始行(不聚合, 聚合在Java代码里), 占坑返回空 */
    List<MiReconOrgDtlItem> queryOrgData(@Param("insurType") String insurType,
                                         @Param("startDate") String startDate,
                                         @Param("endDate") String endDate);
}
