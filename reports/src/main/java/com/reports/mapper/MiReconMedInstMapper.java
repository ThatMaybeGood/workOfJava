package com.reports.mapper;

import com.reports.dto.response.cash.mi.MiReconCompareItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 医保对账-医疗机构侧 Mapper(HIS库, 占坑: SQL待补全)
 */
@Mapper
public interface MiReconMedInstMapper {

    /** 医疗机构侧按结算日期汇总: 数量/医疗总费用, 占坑返回空 */
    List<MiReconCompareItem> queryOrgData(@Param("insurType") String insurType,
                                          @Param("startDate") String startDate,
                                          @Param("endDate") String endDate);
}
