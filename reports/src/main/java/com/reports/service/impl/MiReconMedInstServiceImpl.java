package com.reports.service.impl;

import com.reports.annotation.DataSource;
import com.reports.dto.response.cash.mi.MiReconOrgDtlItem;
import com.reports.dto.response.cash.mi.MiReconTableTotalItem;
import com.reports.dto.response.cash.mi.MiReconZpDiffItem;
import com.reports.mapper.MiReconMedInstMapper;
import com.reports.service.MiReconMedInstService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 医疗机构侧对账数据服务实现(医保结算库powerips, SQL在 MiReconMedInstMapper.xml)
 * <p>
 * 原始行SQL按来源类型UNION ALL(账平已补: ips_dz_biz_bills zp_flag=1;
 * 医保负金额/医保正金额两个块待用户补全, XML内有TODO骨架)。
 */
@Slf4j
@Service
@DataSource("powerips")
public class MiReconMedInstServiceImpl implements MiReconMedInstService {

    private final MiReconMedInstMapper miReconMedInstMapper;

    @Autowired
    public MiReconMedInstServiceImpl(MiReconMedInstMapper miReconMedInstMapper) {
        this.miReconMedInstMapper = miReconMedInstMapper;
    }

    @Override
    public List<MiReconOrgDtlItem> queryOrgData(String insurType, String startDate, String endDate) {
        log.info("[医疗机构对账数据] powerips查询: insurType={} {}~{}", insurType, startDate, endDate);
        return miReconMedInstMapper.queryOrgData(insurType, startDate, endDate);
    }

    @Override
    public List<MiReconZpDiffItem> queryZpDiff(String insurType, String settleDate) {
        log.info("[账平差异] powerips查询: insurType={} settleDate={}", insurType, settleDate);
        return miReconMedInstMapper.queryZpDiff(insurType, settleDate);
    }

    @Override
    public List<MiReconOrgDtlItem> queryMiOrgData(String insurType, String startDate, String endDate) {
        log.info("[医保机构对账数据] powerips查询: insurType={} {}~{}", insurType, startDate, endDate);
        return miReconMedInstMapper.queryMiOrgData(insurType, startDate, endDate);
    }

    @Override
    public List<MiReconTableTotalItem> queryDayTotals(String insurType, String settleDate) {
        log.info("[两表全量合计] powerips查询: insurType={} settleDate={}", insurType, settleDate);
        return miReconMedInstMapper.queryDayTotals(insurType, settleDate);
    }
}
