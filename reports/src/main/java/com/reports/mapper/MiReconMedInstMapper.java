package com.reports.mapper;

import com.reports.dto.response.cash.mi.MiReconOrgDtlItem;
import com.reports.dto.response.cash.mi.MiReconZpDiffItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 医保对账-医疗机构侧 Mapper(HIS库, 占坑: SQL待补全)
 */
@Mapper
public interface MiReconMedInstMapper {

    /** 医疗机构侧原始行(不聚合, 聚合在Java代码里) */
    List<MiReconOrgDtlItem> queryOrgData(@Param("insurType") String insurType,
                                         @Param("startDate") String startDate,
                                         @Param("endDate") String endDate);

    /** 账平差异: biz_bills与pay_bills双边zp_flag=1按setl_id比对, 列对方没有的结算号 */
    List<MiReconZpDiffItem> queryZpDiff(@Param("insurType") String insurType,
                                        @Param("settleDate") String settleDate);
}
