package com.reports.service.impl;

import com.reports.annotation.DataSource;
import com.reports.dto.response.cash.mi.MiReconCompareItem;
import com.reports.mapper.MiReconMedInstMapper;
import com.reports.service.MiReconMedInstService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 医疗机构侧对账数据服务实现(HIS库, 占坑: SQL在 MiReconMedInstMapper.xml 补全)
 * <p>
 * TODO: 补全 mapper/cash/MiReconMedInstMapper.xml 的 queryOrgData 汇总SQL
 * (按费别310/390/3101/3901汇总数量/医疗总费用/结算日期), XML内已留示例与占位语句。
 * 目前占位语句恒返回空, 对比页医疗机构侧显示为空并按不平标红。
 */
@Slf4j
@Service
@DataSource("his")
public class MiReconMedInstServiceImpl implements MiReconMedInstService {

    private final MiReconMedInstMapper miReconMedInstMapper;

    @Autowired
    public MiReconMedInstServiceImpl(MiReconMedInstMapper miReconMedInstMapper) {
        this.miReconMedInstMapper = miReconMedInstMapper;
    }

    @Override
    public List<MiReconCompareItem> queryOrgData(String insurType, String startDate, String endDate) {
        log.info("[医疗机构对账数据] 占坑SQL未补全, 返回空: insurType={} {}~{}", insurType, startDate, endDate);
        return miReconMedInstMapper.queryOrgData(insurType, startDate, endDate);
    }
}
