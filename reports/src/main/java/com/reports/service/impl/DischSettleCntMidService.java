package com.reports.service.impl;

import com.reports.annotation.DataSource;
import com.reports.dto.request.DischSettleCntRequest;
import com.reports.dto.response.cash.discharge.settlement.PersonCountItem;
import com.reports.mapper.DischSettleCntMidMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 出院结算人次统计-中间表模式
 * 查报表库 tr_disch_settle_cnt_d(由HIS侧SP按日/按范围抽数灌入),
 * SQL口径与直查HIS同款(行粒度保留charge_type/操作员姓名hex, 发票数挂行上), 后处理共用 PersonCountMergeSupport
 */
@Slf4j
@Service
@DataSource("master")
public class DischSettleCntMidService {

    private final DischSettleCntMidMapper midMapper;

    @Autowired
    public DischSettleCntMidService(DischSettleCntMidMapper midMapper) {
        this.midMapper = midMapper;
    }

    public List<PersonCountItem> queryPersonCount(DischSettleCntRequest request) {
        String dimension = request.getDimension() == null ? "summary" : request.getDimension();
        String timeDimension = request.getTimeDimension() == null ? "day" : request.getTimeDimension();
        log.info("查询出院结算人次(中间表)，dimension={}，timeDimension={}", dimension, timeDimension);
        List<PersonCountItem> result;
        switch (dimension) {
            case "operator":
                result = PersonCountMergeSupport.mergeByOperator(midMapper.queryByOperator(
                        sqlDay(request.getStartDate()), sqlDay(request.getEndDate()), timeDimension));
                break;
            case "payType":
                result = PersonCountMergeSupport.filterPayType(midMapper.queryByPayType(
                        sqlDay(request.getStartDate()), sqlDay(request.getEndDate()), timeDimension));
                break;
            default:
                result = PersonCountMergeSupport.mergeByFeeChannel(midMapper.queryByFeeChannel(
                        sqlDay(request.getStartDate()), sqlDay(request.getEndDate()), timeDimension));
                break;
        }
        return PersonCountMergeSupport.sortByDisplayKeys(result);
    }

    /** java.util.Date→java.sql.Date：绑定按DATE类型发送， 避免TIMESTAMP绑定触发的列隐式转换致索引失效 */
    private static java.sql.Date sqlDay(java.util.Date d) {
        return d == null ? null : new java.sql.Date(d.getTime());
    }
}
