package com.reports.service.impl;

import com.reports.config.ReportDataConfig;
import com.reports.dto.request.PreHospTransRequest;
import com.reports.dto.response.cash.prehosp.PreHospTransItem;
import com.reports.dto.response.cash.prehosp.PreHospUserItem;
import com.reports.mapper.PreHospOperMapper;
import com.reports.service.PreHospTransHisService;
import com.reports.service.PreHospTransService;
import com.reports.util.GbkHexUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 院前转住院统计服务实现(报表库侧)
 * 维护表/人员字典在报表库, 统计在HIS(经PreHospTransHisService切换), 只统计维护的有效操作员
 */
@Slf4j
@Service
public class PreHospTransServiceImpl implements PreHospTransService {

    private final ReportDataConfig dataConfig;
    private final PreHospOperMapper preHospOperMapper;
    private final PreHospTransHisService preHospTransHisService;

    @Autowired
    public PreHospTransServiceImpl(ReportDataConfig dataConfig, PreHospOperMapper preHospOperMapper,
                                   PreHospTransHisService preHospTransHisService) {
        this.dataConfig = dataConfig;
        this.preHospOperMapper = preHospOperMapper;
        this.preHospTransHisService = preHospTransHisService;
    }

    @Override
    public List<PreHospTransItem> queryStats(PreHospTransRequest request) {
        log.info("查询院前转住院统计，mode={}", dataConfig.getMode());
        if (request.getStartDate() == null || request.getEndDate() == null) {
            return new ArrayList<>();
        }
        try {
            List<String> operatorNos = preHospOperMapper.queryValidOperNos();
            String operatorNo = request.getOperatorNo() == null ? "" : request.getOperatorNo().trim();
            if (operatorNos.isEmpty() || (!operatorNo.isEmpty() && !operatorNos.contains(operatorNo))) {
                return new ArrayList<>();
            }
            String timeDimension = request.getTimeDimension() == null ? "day" : request.getTimeDimension();
            List<PreHospTransItem> rows = preHospTransHisService.queryStats(
                    sqlDay(request.getStartDate()), sqlDay(request.getEndDate()),
                    timeDimension, operatorNos, operatorNo);
            fillOperName(rows);
            return rows;
        } catch (Exception e) {
            log.warn("查询院前转住院统计失败", e);
            return new ArrayList<>();
        }
    }

    /** 操作员姓名回显: HIS users批量取(US7ASCII RAWTOHEX待GBK解码), 查不到回退工号 */
    private void fillOperName(List<PreHospTransItem> rows) {
        if (rows.isEmpty()) {
            return;
        }
        Map<String, String> names = new HashMap<>();
        try {
            List<String> codes = new ArrayList<>();
            for (PreHospTransItem r : rows) {
                if (r.getOperNo() != null && !codes.contains(r.getOperNo())) {
                    codes.add(r.getOperNo());
                }
            }
            if (!codes.isEmpty()) {
                for (PreHospUserItem u : preHospTransHisService.queryUsersByIds(codes)) {
                    String name = GbkHexUtil.decode(u.getOperNameHex());
                    names.putIfAbsent(u.getOperNo(),
                            name == null || name.isEmpty() ? u.getOperNo() : name);
                }
            }
        } catch (Exception e) {
            log.warn("回显操作员姓名失败", e);
        }
        for (PreHospTransItem r : rows) {
            r.setOperName(names.getOrDefault(r.getOperNo(), r.getOperNo()));
        }
    }

    /** java.util.Date→java.sql.Date：绑定按DATE类型发送， 避免TIMESTAMP绑定触发的列隐式转换致索引失效 */
    private static java.sql.Date sqlDay(java.util.Date d) {
        return d == null ? null : new java.sql.Date(d.getTime());
    }
}
