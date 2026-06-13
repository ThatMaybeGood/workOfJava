package com.reports.service.impl;

import com.reports.dto.common.PageResult;
import com.reports.dto.request.OutpatientOperationRequest;
import com.reports.dto.response.*;
import com.reports.service.OutpatientOperationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 门诊运行数据统计服务实现（Mock数据）
 */
@Slf4j
@Service
public class OutpatientOperationServiceImpl implements OutpatientOperationService {

    @Override
    public OverviewData queryOverview(OutpatientOperationRequest request) {
        log.info("查询门诊运行概览数据，参数: startDate={}, endDate={}", request.getStartDate(), request.getEndDate());

        OverviewData overview = new OverviewData();
        overview.setTotalVisits(12536);
        overview.setAppointmentRate("83.10%");
        overview.setVisitCount(112);

        VisitCountDetail visitCountDetail = new VisitCountDetail();
        visitCountDetail.setFamousExpert(112);
        visitCountDetail.setSpecialExpert(112);
        visitCountDetail.setKnownExpert(112);
        visitCountDetail.setExpertA(112);
        visitCountDetail.setExpertB(112);
        visitCountDetail.setOrdinary(112);
        overview.setVisitCountDetail(visitCountDetail);

        overview.setExamRate("56.50%");
        overview.setEfficiency(27.5);
        overview.setEffectiveUnits(112);
        overview.setTotalUnits(251);

        UnitDetail unitDetail = new UnitDetail();
        unitDetail.setFamousExpert(newUnitDetailItem(52, 112));
        unitDetail.setSpecialExpert(newUnitDetailItem(52, 112));
        unitDetail.setKnownExpert(newUnitDetailItem(52, 112));
        unitDetail.setExpertA(newUnitDetailItem(52, 112));
        unitDetail.setExpertB(newUnitDetailItem(52, 112));
        unitDetail.setOrdinary(newUnitDetailItem(52, 112));
        overview.setUnitDetail(unitDetail);

        return overview;
    }

    @Override
    public PageResult<TableItem> queryTable(OutpatientOperationRequest request, Integer page, Integer pageSize) {
        log.info("查询门诊运行表格数据，参数: page={}, pageSize={}", page, pageSize);

        // Mock 数据
        List<TableItem> list = new ArrayList<>();
        for (int i = 0; i < pageSize; i++) {
            TableItem item = new TableItem();
            item.setDeptName("心血管内科门诊" + (i + 1));
            item.setVisits(350 + i * 10);
            item.setAppointmentRate("70.00%");
            item.setExamRate("75.00%");
            item.setEfficiency(25.00 + i);
            item.setVisitCount(30 + i);
            item.setFamousExpert(2 + i);
            item.setSpecialExpert(3 + i);
            item.setKnownExpert(2 + i);
            item.setExpertA(5 + i);
            item.setExpertB(4 + i);
            item.setOrdinary(2 + i);

            UnitDetailItem effectiveUnitsTotal = new UnitDetailItem();
            effectiveUnitsTotal.setEffective(45 + i);
            effectiveUnitsTotal.setTotal(50 + i);
            item.setEffectiveUnitsTotal(effectiveUnitsTotal);

            UnitDetail unitDetail = new UnitDetail();
            unitDetail.setFamousExpert(newUnitDetailItem(2 + i, 4 + i));
            unitDetail.setSpecialExpert(newUnitDetailItem(2 + i, 4 + i));
            unitDetail.setKnownExpert(newUnitDetailItem(2 + i, 4 + i));
            unitDetail.setExpertA(newUnitDetailItem(2 + i, 4 + i));
            unitDetail.setExpertB(newUnitDetailItem(2 + i, 4 + i));
            unitDetail.setOrdinary(newUnitDetailItem(2 + i, 4 + i));
            item.setUnitDetail(unitDetail);

            list.add(item);
        }

        // 模拟总记录数 55
        return PageResult.of(list, 55L, page, pageSize);
    }

    private UnitDetailItem newUnitDetailItem(int effective, int total) {
        UnitDetailItem item = new UnitDetailItem();
        item.setEffective(effective);
        item.setTotal(total);
        return item;
    }

}
