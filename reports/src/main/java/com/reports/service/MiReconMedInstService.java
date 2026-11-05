package com.reports.service;

import com.reports.dto.response.cash.mi.MiReconOrgDtlItem;
import com.reports.dto.response.cash.mi.MiReconTableTotalItem;
import com.reports.dto.response.cash.mi.MiReconUnmatchedNegItem;
import com.reports.dto.response.cash.mi.MiReconZpDiffItem;

import java.util.List;

/**
 * 医疗机构侧对账数据服务(占坑: SQL在 MiReconMedInstMapper.xml 补全)
 * <p>
 * HIS侧出原始行(6字段, 不聚合), 按医保类型+日期范围返回,
 * 与医保上传数据(tr_mi_recon_detail)做同类型同日期的金额对比, 聚合在调用方代码里做。
 */
public interface MiReconMedInstService {

    /**
     * 医疗机构侧原始行: 日期/数量/医疗总金额/类型代码/类型名称/来源类型
     */
    List<MiReconOrgDtlItem> queryOrgData(String insurType, String startDate, String endDate);

    /**
     * 账平差异明细: 同一结算日两表账平应一致, 列对方没有的结算号
     */
    List<MiReconZpDiffItem> queryZpDiff(String insurType, String settleDate);

    /**
     * 医保机构侧原始行: pay_bills自身按账平/医保负金额/医保正金额三分类
     */
    List<MiReconOrgDtlItem> queryMiOrgData(String insurType, String startDate, String endDate);

    /**
     * 某日两表全量合计(不限zp_flag, 账平差异弹窗顶部对照)
     */
    List<MiReconTableTotalItem> queryDayTotals(String insurType, String settleDate);

    /**
     * 某日未匹配负交易(找不到对应正交易的退费, 明细弹窗展示差异来源用)
     */
    List<MiReconUnmatchedNegItem> queryUnmatchedNeg(String insurType, String settleDate,
                                                    String startDate, String endDate);
}
