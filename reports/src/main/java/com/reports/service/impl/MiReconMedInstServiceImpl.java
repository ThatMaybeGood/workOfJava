package com.reports.service.impl;

import com.reports.dto.response.cash.mi.MiReconCompareItem;
import com.reports.service.MiReconMedInstService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 医疗机构侧对账数据服务实现(占坑: SQL待补全)
 * <p>
 * TODO: 用户后续补全HIS汇总SQL(按费别310/390/3101/3901汇总数量/医疗总费用/结算日期),
 * 目前返回空列表, 对比页医疗机构侧显示为空并按不平标红。
 */
@Slf4j
@Service
public class MiReconMedInstServiceImpl implements MiReconMedInstService {

    @Override
    public List<MiReconCompareItem> queryOrgData(String insurType, String startDate, String endDate) {
        log.info("[医疗机构对账数据] 占坑未实现, 返回空: insurType={} {}~{}", insurType, startDate, endDate);
        return new ArrayList<>();
    }
}
