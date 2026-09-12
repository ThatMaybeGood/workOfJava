package com.reports.service.impl;

import com.reports.config.ReportDataConfig;
import com.reports.dto.request.InpatPrepayRequest;
import com.reports.entity.InpatPrepayChtEntity;
import com.reports.entity.InpatPrepayDtlEntity;
import com.reports.entity.InpatPrepayOvEntity;
import com.reports.mapper.InpatPrepayMapper;
import com.reports.service.InpatPrepayService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 住院预交金统计服务实现。
 *
 * <p>数据来自三张表：
 * <ul>
 *   <li>{@code TR_INPAT_PREPAY_OV} —— 概览快照（1 行）</li>
 *   <li>{@code TR_INPAT_PREPAY_DTL} —— 按天的明细，用 {@code data_type} 区分汇总/进项/退项</li>
 *   <li>{@code TR_INPAT_PREPAY_CHT} —— 图表数据，用 {@code chart_type} 区分趋势/渠道/支付方式；
 *       渠道图把 {@code category} 当渠道、{@code series_name} 当支付方式，一行同时供三种图使用</li>
 * </ul>
 */
@Slf4j
@Service
public class InpatPrepayServiceImpl implements InpatPrepayService {

    /** SimpleDateFormat 非线程安全，按线程各持一份。 */
    private static final ThreadLocal<java.text.SimpleDateFormat> DAY_FORMAT =
            new ThreadLocal<java.text.SimpleDateFormat>() {
                @Override
                protected java.text.SimpleDateFormat initialValue() {
                    return new java.text.SimpleDateFormat("yyyy-MM-dd");
                }
            };

    private final ReportDataConfig dataConfig;
    private final InpatPrepayMapper inpatPrepayMapper;

    @Autowired
    public InpatPrepayServiceImpl(ReportDataConfig dataConfig, InpatPrepayMapper inpatPrepayMapper) {
        this.dataConfig = dataConfig;
        this.inpatPrepayMapper = inpatPrepayMapper;
    }

    // ==================== 概览 ====================

    @Override
    public Map<String, Object> queryOverview(InpatPrepayRequest request) {
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        if (!dataConfig.isMybatisPlus()) {
            return result;
        }
        try {
            InpatPrepayOvEntity e =
                    inpatPrepayMapper.queryOverview(request.getStartDate(), request.getEndDate());
            if (e != null) {
                result.put("prepaymentCount", nvl(e.getPrepaymentCount()));
                result.put("prepaymentCountCompare", nvl(e.getPrepaymentCountCompare()));
                result.put("prepaymentAmount", e.getPrepaymentAmount() == null
                        ? Double.valueOf(0d) : e.getPrepaymentAmount());
                result.put("prepaymentAmountCompare", nvl(e.getPrepaymentAmountCompare()));
            }
        } catch (Exception e) {
            log.warn("查询住院预交金概览失败", e);
        }
        return result;
    }

    // ==================== 明细表 ====================

    @Override
    public Map<String, Object> queryTable(InpatPrepayRequest request, String dataType) {
        List<Map<String, Object>> all = new ArrayList<Map<String, Object>>();
        if (dataConfig.isMybatisPlus()) {
            try {
                List<InpatPrepayDtlEntity> rows = inpatPrepayMapper.queryDetail(
                        request.getStartDate(), request.getEndDate(), dataType);
                for (InpatPrepayDtlEntity r : rows) {
                    Map<String, Object> item = new LinkedHashMap<String, Object>();
                    // 直接返回 Date 会被序列化成 ISO（2026-08-12T16:00:00.000+00:00）原样显示在表格里，
                    // 这里先格式化成 yyyy-MM-dd
                    item.put("date", r.getItemDate() == null ? null : DAY_FORMAT.get().format(r.getItemDate()));
                    item.put("countLast", nvl(r.getCountLast()));
                    item.put("countCurrent", nvl(r.getCountCurrent()));
                    item.put("countCompare", nvl(r.getCountCompare()));
                    item.put("amountLast", r.getAmountLast());
                    item.put("amountCurrent", r.getAmountCurrent());
                    item.put("amountCompare", nvl(r.getAmountCompare()));
                    all.add(item);
                }
            } catch (Exception e) {
                log.warn("查询住院预交金明细失败, dataType={}", dataType, e);
            }
        }
        return page(all, request.getPage(), request.getPageSize());
    }

    // ==================== 趋势图 ====================

    @Override
    public Map<String, Object> queryTrendChart(InpatPrepayRequest request) {
        String type = typeOf(request, "summary_count");
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("type", type);
        result.put("title", titlePrefix(type) + "趋势");
        result.put("legend", Collections.unmodifiableList(
                java.util.Arrays.asList("本期", "上期")));

        List<String> categories = new ArrayList<String>();
        List<Integer> currentData = new ArrayList<Integer>();
        List<Integer> lastData = new ArrayList<Integer>();
        if (dataConfig.isMybatisPlus()) {
            try {
                List<InpatPrepayChtEntity> rows = inpatPrepayMapper.queryChart(
                        request.getStartDate(), request.getEndDate(), "TREND");
                for (InpatPrepayChtEntity r : rows) {
                    categories.add(r.getCategory());
                    currentData.add(nvl(r.getDataValue()));
                    // TREND 行的 compare_value 存的是上期值（不是差值），直接作为上期系列
                    lastData.add(nvl(r.getCompareValue()));
                }
            } catch (Exception e) {
                log.warn("查询住院预交金趋势失败", e);
            }
        }
        result.put("categories", categories);
        result.put("currentData", currentData);
        result.put("lastData", lastData);
        return result;
    }

    // ==================== 渠道分析 ====================

    @Override
    public Map<String, Object> queryChannelChart(InpatPrepayRequest request) {
        String type = typeOf(request, "summary_count");
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("type", type);

        Map<String, int[]> byChannel = new LinkedHashMap<String, int[]>();
        Map<String, int[]> byPayType = new LinkedHashMap<String, int[]>();
        Map<String, Map<String, Integer>> cross = new LinkedHashMap<String, Map<String, Integer>>();
        Set<String> payTypes = new LinkedHashSet<String>();

        if (dataConfig.isMybatisPlus()) {
            try {
                List<InpatPrepayChtEntity> rows = inpatPrepayMapper.queryChart(
                        request.getStartDate(), request.getEndDate(), "CHANNEL");
                for (InpatPrepayChtEntity r : rows) {
                    String channel = r.getCategory() == null ? "未知" : r.getCategory();
                    String payType = r.getSeriesName() == null ? "未知" : r.getSeriesName();
                    int v = nvl(r.getDataValue());
                    int c = nvl(r.getCompareValue());
                    accumulate(byChannel, channel, v, c);
                    accumulate(byPayType, payType, v, c);
                    payTypes.add(payType);
                    Map<String, Integer> line = cross.get(channel);
                    if (line == null) {
                        line = new LinkedHashMap<String, Integer>();
                        cross.put(channel, line);
                    }
                    Integer old = line.get(payType);
                    line.put(payType, Integer.valueOf(old == null ? v : old.intValue() + v));
                }
            } catch (Exception e) {
                log.warn("查询住院预交金渠道分析失败", e);
            }
        }

        result.put("channelAnalysis", toNameValueList(byChannel));
        result.put("payTypeAnalysis", toNameValueList(byPayType));

        List<String> payTypeOrder = new ArrayList<String>(payTypes);
        List<Map<String, Object>> series = new ArrayList<Map<String, Object>>();
        for (Map.Entry<String, Map<String, Integer>> e : cross.entrySet()) {
            List<Integer> data = new ArrayList<Integer>();
            for (String pt : payTypeOrder) {
                Integer v = e.getValue().get(pt);
                data.add(Integer.valueOf(v == null ? 0 : v.intValue()));
            }
            Map<String, Object> s = new LinkedHashMap<String, Object>();
            s.put("name", e.getKey());
            s.put("data", data);
            series.add(s);
        }
        Map<String, Object> stack = new LinkedHashMap<String, Object>();
        stack.put("categories", payTypeOrder);
        stack.put("series", series);
        result.put("channelPayTypeAnalysis", stack);
        return result;
    }

    // ==================== 支付方式分析（退项） ====================

    @Override
    public Map<String, Object> queryPayTypeChart(InpatPrepayRequest request) {
        String type = typeOf(request, "refund_count");
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("type", type);

        Map<String, int[]> byPayType = new LinkedHashMap<String, int[]>();
        if (dataConfig.isMybatisPlus()) {
            try {
                List<InpatPrepayChtEntity> rows = inpatPrepayMapper.queryChart(
                        request.getStartDate(), request.getEndDate(), "PAY_TYPE");
                for (InpatPrepayChtEntity r : rows) {
                    accumulate(byPayType, r.getCategory() == null ? "未知" : r.getCategory(),
                            nvl(r.getDataValue()), nvl(r.getCompareValue()));
                }
            } catch (Exception e) {
                log.warn("查询住院预交金支付方式分析失败", e);
            }
        }
        result.put("payTypeAnalysis", toNameValueList(byPayType));
        return result;
    }

    // ==================== 工具方法 ====================

    private static String typeOf(InpatPrepayRequest request, String def) {
        String t = request == null ? null : request.getType();
        return (t == null || t.trim().isEmpty()) ? def : t;
    }

    private static String titlePrefix(String type) {
        return type.endsWith("amount") ? "缴费金额" : "缴费人次";
    }

    /** name -> {value, compare} 累加。 */
    private static void accumulate(Map<String, int[]> target, String name, int value, int compare) {
        int[] cell = target.get(name);
        if (cell == null) {
            target.put(name, new int[]{value, compare});
        } else {
            cell[0] += value;
            cell[1] += compare;
        }
    }

    private static List<Map<String, Object>> toNameValueList(Map<String, int[]> src) {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        for (Map.Entry<String, int[]> e : src.entrySet()) {
            Map<String, Object> item = new LinkedHashMap<String, Object>();
            item.put("name", e.getKey());
            item.put("value", Integer.valueOf(e.getValue()[0]));
            item.put("compare", Integer.valueOf(e.getValue()[1]));
            list.add(item);
        }
        return list;
    }

    private static Map<String, Object> page(List<Map<String, Object>> all, Integer pageNo,
                                            Integer pageSizeNo) {
        int page = pageNo == null || pageNo.intValue() < 1 ? 1 : pageNo.intValue();
        int size = pageSizeNo == null || pageSizeNo.intValue() < 1 ? 10 : pageSizeNo.intValue();
        int total = all.size();
        int from = Math.min((page - 1) * size, total);
        int to = Math.min(from + size, total);
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("list", new ArrayList<Map<String, Object>>(all.subList(from, to)));
        result.put("total", Integer.valueOf(total));
        result.put("page", Integer.valueOf(page));
        result.put("pageSize", Integer.valueOf(size));
        return result;
    }

    private static int nvl(Integer v) {
        return v == null ? 0 : v.intValue();
    }
}
