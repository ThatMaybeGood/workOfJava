package com.reports.service.impl;

import com.reports.annotation.DataSource;
import com.reports.config.HisMidTableSwitchConfig;
import com.reports.config.ReportDataConfig;
import com.reports.dto.request.DischSettleCntRequest;
import com.reports.dto.response.cash.discharge.settlement.PersonCountItem;
import com.reports.mapper.DischSettleCntMapper;
import com.reports.service.DischSettleCntService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 出院结算人次统计服务实现
 * 直查 HIS 业务库（inp_settle_master），经 @DataSource("his") 切换数据源
 * HIS库为US7ASCII(实际存GBK字节)：SQL不下推中文，取原始值后转码翻译
 * 开关 reports.his-mid-table.disch-settle-cnt=true 时改走报表库中间表(见 DischSettleCntMidService)
 */
@Slf4j
@Service
@DataSource("his")
public class DischSettleCntServiceImpl implements DischSettleCntService {

    private final ReportDataConfig dataConfig;
    private final HisMidTableSwitchConfig midTableSwitch;
    private final DischSettleCntMidService dischSettleCntMidService;
    private final DischSettleCntMapper dischSettleCntMapper;

    @Autowired
    public DischSettleCntServiceImpl(ReportDataConfig dataConfig, HisMidTableSwitchConfig midTableSwitch,
                                     DischSettleCntMidService dischSettleCntMidService,
                                     DischSettleCntMapper dischSettleCntMapper) {
        this.dataConfig = dataConfig;
        this.midTableSwitch = midTableSwitch;
        this.dischSettleCntMidService = dischSettleCntMidService;
        this.dischSettleCntMapper = dischSettleCntMapper;
    }

    @Override
    public List<PersonCountItem> queryPersonCount(DischSettleCntRequest request) {
        String dimension = request.getDimension() == null ? "summary" : request.getDimension();
        String timeDimension = request.getTimeDimension() == null ? "day" : request.getTimeDimension();
        log.info("查询出院结算人次，dimension={}，timeDimension={}，mode={}，midTable={}",
                dimension, timeDimension, dataConfig.getMode(), midTableSwitch.isDischSettleCnt());
        if (dataConfig.isMock()) {
            return new ArrayList<>();
        }
        try {
            if (midTableSwitch.isDischSettleCnt()) {
                // 中间表模式: 查报表库 tr_disch_settle_cnt_d(口径与直查HIS一致, 见 MidService)
                return dischSettleCntMidService.queryPersonCount(request);
            }
            List<PersonCountItem> result;
            switch (dimension) {
                case "operator":
                    result = PersonCountMergeSupport.mergeByOperator(dischSettleCntMapper.queryByOperator(
                            sqlDay(request.getStartDate()), sqlDay(request.getEndDate()), timeDimension));
                    break;
                case "payType":
                    result = PersonCountMergeSupport.filterPayType(dischSettleCntMapper.queryByPayType(
                            sqlDay(request.getStartDate()), sqlDay(request.getEndDate()), timeDimension));
                    break;
                default:
                    result = PersonCountMergeSupport.mergeByFeeChannel(dischSettleCntMapper.queryByFeeChannel(
                            sqlDay(request.getStartDate()), sqlDay(request.getEndDate()), timeDimension));
                    break;
            }
            return PersonCountMergeSupport.sortByDisplayKeys(result);
        } catch (Exception e) {
            log.warn("查询出院结算人次失败", e);
            return new ArrayList<>();
        }
    }

    /** java.util.Date→java.sql.Date：绑定按DATE类型发送， 避免TIMESTAMP绑定触发的列隐式转换致索引失效 */
    private static java.sql.Date sqlDay(java.util.Date d) {
        return d == null ? null : new java.sql.Date(d.getTime());
    }
}
