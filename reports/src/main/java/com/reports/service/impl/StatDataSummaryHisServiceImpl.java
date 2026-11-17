package com.reports.service.impl;

import com.reports.annotation.DataSource;
import com.reports.dto.response.stat.StatDataSummaryResponse;
import com.reports.dto.response.stat.StatDataSummaryResponse.StatCard;
import com.reports.dto.response.stat.StatDataSummaryResponse.StatColumn;
import com.reports.mapper.StatDataSummaryHisMapper;
import com.reports.service.StatDataSummaryHisService;
import com.reports.util.GbkHexUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 运营数据统计-HIS出诊单元服务实现(his库, SQL在 StatDataSummaryHisMapper.xml)
 */
@Slf4j
@Service
@DataSource("his")
public class StatDataSummaryHisServiceImpl implements StatDataSummaryHisService {

    private final StatDataSummaryHisMapper statDataSummaryHisMapper;

    @Autowired
    public StatDataSummaryHisServiceImpl(StatDataSummaryHisMapper statDataSummaryHisMapper) {
        this.statDataSummaryHisMapper = statDataSummaryHisMapper;
    }

    @Override
    public StatDataSummaryResponse queryClinicUnit(String startDate, String endDate) {
        log.info("[医生出诊单元] his查询: {}~{}", startDate, endDate);
        // 排除标签转GBK hex, RAWTOHEX(列) LIKE 匹配, 纯ASCII避开US7ASCII驱动丢高字节
        String olNurseHex = GbkHexUtil.encodeHex("OL护理") + "%";
        String olPharmHex = GbkHexUtil.encodeHex("OL药学") + "%";
        String olTeamHex = GbkHexUtil.encodeHex("OL团队") + "%";
        String revisitHex = "%" + GbkHexUtil.encodeHex("O") + "%" + GbkHexUtil.encodeHex("回诊号") + "%";
        String quankeHex = GbkHexUtil.encodeHex("全科医学普号凤互");

        Map<String, Object> overall = statDataSummaryHisMapper.bt6ClinicUnitOverall(
                startDate, endDate, olNurseHex, olPharmHex, olTeamHex, revisitHex, quankeHex);
        List<Map<String, Object>> dailyRaw = statDataSummaryHisMapper.bt6ClinicUnitDaily(
                startDate, endDate, olNurseHex, olPharmHex, olTeamHex, revisitHex, quankeHex);

        List<StatColumn> columns = new ArrayList<>();
        columns.add(new StatColumn("itemDate", "日期"));
        columns.add(new StatColumn("unitCnt", "出诊单元数"));
        columns.add(new StatColumn("labelCnt", "出诊标签数"));

        StatDataSummaryResponse resp = new StatDataSummaryResponse();
        resp.setColumns(columns);
        resp.setList(rows(dailyRaw, columns));
        List<StatCard> cards = new ArrayList<>();
        cards.add(new StatCard("出诊单元数", num(overall.get("UNITCNT")), "个"));
        cards.add(new StatCard("出诊标签数", num(overall.get("LABELCNT")), "个"));
        resp.setCards(cards);
        return resp;
    }

    private List<Map<String, Object>> rows(List<Map<String, Object>> raw, List<StatColumn> columns) {
        List<Map<String, Object>> out = new ArrayList<>();
        if (raw == null) {
            return out;
        }
        for (Map<String, Object> r : raw) {
            Map<String, Object> row = new LinkedHashMap<>();
            for (StatColumn c : columns) {
                Object v = pick(r, c.getKey());
                if (v instanceof Date) {
                    v = new SimpleDateFormat("yyyy-MM-dd").format((Date) v);
                }
                row.put(c.getKey(), v == null ? "" : v);
            }
            out.add(row);
        }
        return out;
    }

    private Object pick(Map<String, Object> r, String key) {
        for (Map.Entry<String, Object> e : r.entrySet()) {
            if (e.getKey().equalsIgnoreCase(key)) {
                return e.getValue();
            }
        }
        return null;
    }

    private String num(Object v) {
        if (v == null) {
            return "0";
        }
        if (v instanceof BigDecimal) {
            return ((BigDecimal) v).stripTrailingZeros().toPlainString();
        }
        return String.valueOf(v);
    }

}
