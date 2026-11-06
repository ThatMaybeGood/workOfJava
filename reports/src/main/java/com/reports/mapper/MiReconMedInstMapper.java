package com.reports.mapper;

import com.reports.dto.response.cash.mi.MiReconOrgDtlItem;
import com.reports.dto.response.cash.mi.MiReconPayCompItem;
import com.reports.dto.response.cash.mi.MiReconPayCrossItem;
import com.reports.dto.response.cash.mi.MiReconPayWarnItem;
import com.reports.dto.response.cash.mi.MiReconTableTotalItem;
import com.reports.dto.response.cash.mi.MiReconUnmatchedNegItem;
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

    /** 医保机构侧原始行(ips_dz_pay_bills自身三分类: 账平/医保负金额/医保正金额, 不聚合) */
    List<MiReconOrgDtlItem> queryMiOrgData(@Param("insurType") String insurType,
                                           @Param("startDate") String startDate,
                                           @Param("endDate") String endDate);

    /** 账平差异: biz_bills与pay_bills双边zp_flag=1按setl_id比对, 列对方没有的结算号 */
    List<MiReconZpDiffItem> queryZpDiff(@Param("insurType") String insurType,
                                        @Param("settleDate") String settleDate);

    /** 某日两表全量合计(不限zp_flag, 账平差异弹窗顶部对照) */
    List<MiReconTableTotalItem> queryDayTotals(@Param("insurType") String insurType,
                                               @Param("settleDate") String settleDate);

    /** 某日未匹配负交易: pay退费在biz表和pay表正交易都查不到对应正交易的结算号 */
    List<MiReconUnmatchedNegItem> queryUnmatchedNeg(@Param("insurType") String insurType,
                                                    @Param("settleDate") String settleDate,
                                                    @Param("startDate") String startDate,
                                                    @Param("endDate") String endDate);

    /** 支付构成 dim=sum: 按险种月合计(账平-退费+收费) */
    List<MiReconPayCompItem> queryPaySum(@Param("insurType") String insurType,
                                         @Param("startDate") String startDate,
                                         @Param("endDate") String endDate,
                                         @Param("prevStart") String prevStart);

    /** 支付构成 dim=cat: 按险种+类别(退费仅本月匹配到HIS正交易的部分) */
    List<MiReconPayCompItem> queryPayCat(@Param("insurType") String insurType,
                                         @Param("startDate") String startDate,
                                         @Param("endDate") String endDate,
                                         @Param("prevStart") String prevStart);

    /** 支付构成 dim=day: 某险种按日合计(三类汇总) */
    List<MiReconPayCompItem> queryPayDay(@Param("insurType") String insurType,
                                         @Param("startDate") String startDate,
                                         @Param("endDate") String endDate,
                                         @Param("prevStart") String prevStart);

    /** 支付构成 dim=dayCat: 某险种按日+类别 */
    List<MiReconPayCompItem> queryPayDayCat(@Param("insurType") String insurType,
                                            @Param("startDate") String startDate,
                                            @Param("endDate") String endDate,
                                            @Param("prevStart") String prevStart);

    /** 支付构成 dim=cross: 退费跨月配对明细(查找范围 上月月初~退费当天) */
    List<MiReconPayCrossItem> queryPayCross(@Param("insurType") String insurType,
                                            @Param("startDate") String startDate,
                                            @Param("endDate") String endDate,
                                            @Param("prevStart") String prevStart);

    /** 支付构成 dim=warn: 每险种当月退费匹配概况(主表警示列) */
    List<MiReconPayWarnItem> queryPayWarn(@Param("insurType") String insurType,
                                          @Param("startDate") String startDate,
                                          @Param("endDate") String endDate,
                                          @Param("prevStart") String prevStart);
}
