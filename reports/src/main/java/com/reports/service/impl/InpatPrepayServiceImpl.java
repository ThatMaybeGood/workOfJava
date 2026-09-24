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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 住院预交金统计服务实现。
 *
 * <p>数据来自交易流水表 {@code TR_INPAT_PREPAY_RCPT} 实时聚合,口径:
 * <ul>
 *   <li>退项 = transact_type '结算'(金额为负,展示取绝对值),其余 = 进项</li>
 *   <li>渠道按操作员区分:9111 自助机,其余窗口</li>
 *   <li>对比 = 同比(本期 vs 去年同日期范围),由后端推去年同期区间,SQL 一次查出两段</li>
 *   <li>人次 = 交易笔数(COUNT 流水行)</li>
 * </ul>
 */
@Slf4j
@Service
public class InpatPrepayServiceImpl implements InpatPrepayService {

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
        if (!dataConfig.isMybatisPlus() || !hasDateRange(request)) {
            return result;
        }
        try {
            String scope = scopeOf(request.getType(), "INCOME");
            Date[] lastRange = lastYearRange(request);
            InpatPrepayOvEntity e = inpatPrepayMapper.queryOverview(
                    day(request.getStartDate()), day(request.getEndDate()), lastRange[0], lastRange[1], scope);
            if (e != null) {
                boolean refund = "REFUND".equals(scope);
                double amountCurrent = amountOf(e.getAmountCurrent(), refund);
                double amountLast = amountOf(e.getAmountLast(), refund);
                result.put("prepaymentCount", nvl(e.getCountCurrent()));
                result.put("prepaymentCountCompare", comparePct(nvl(e.getCountCurrent()), nvl(e.getCountLast())));
                result.put("prepaymentAmount", amountCurrent);
                result.put("prepaymentAmountCompare", comparePct(amountCurrent, amountLast));
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
        if (dataConfig.isMybatisPlus() && hasDateRange(request)) {
            try {
                boolean refund = "REFUND".equals(dataType);
                boolean month = isMonthDimension(request);
                Date[] lastRange = lastYearRange(request);
                List<InpatPrepayDtlEntity> rows = inpatPrepayMapper.queryDaily(
                        day(request.getStartDate()), day(request.getEndDate()), lastRange[0], lastRange[1], dataType, month);
                for (InpatPrepayDtlEntity r : rows) {
                    Map<String, Object> item = new LinkedHashMap<String, Object>();
                    // Date 序列化会成 ISO 字符串,先格式化;按月聚合时格式为 yyyy-MM
                    item.put("date", r.getItemDate() == null ? null : formatDate(r.getItemDate(), month));
                    item.put("countLast", nvl(r.getCountLast()));
                    item.put("countCurrent", nvl(r.getCountCurrent()));
                    item.put("countCompare", comparePct(nvl(r.getCountCurrent()), nvl(r.getCountLast())));
                    double amountLast = amountOf(r.getAmountLast(), refund);
                    double amountCurrent = amountOf(r.getAmountCurrent(), refund);
                    item.put("amountLast", amountLast);
                    item.put("amountCurrent", amountCurrent);
                    item.put("amountCompare", comparePct(amountCurrent, amountLast));
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
        boolean byAmount = type.endsWith("amount");
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("type", type);
        result.put("title", (byAmount ? "缴费金额" : "缴费人次") + "趋势");
        result.put("legend", Collections.unmodifiableList(
                java.util.Arrays.asList("本期", "同期")));

        List<String> categories = new ArrayList<String>();
        List<Integer> currentData = new ArrayList<Integer>();
        List<Integer> lastData = new ArrayList<Integer>();
        if (dataConfig.isMybatisPlus() && hasDateRange(request)) {
            try {
                String scope = scopeOf(type, "SUMMARY");
                boolean refund = "REFUND".equals(scope);
                boolean month = isMonthDimension(request);
                Date[] lastRange = lastYearRange(request);
                List<InpatPrepayDtlEntity> rows = inpatPrepayMapper.queryDaily(
                        day(request.getStartDate()), day(request.getEndDate()), lastRange[0], lastRange[1], scope, month);
                for (InpatPrepayDtlEntity r : rows) {
                    categories.add(r.getItemDate() == null ? "" : formatDate(r.getItemDate(), month));
                    if (byAmount) {
                        currentData.add((int) Math.round(amountOf(r.getAmountCurrent(), refund)));
                        lastData.add((int) Math.round(amountOf(r.getAmountLast(), refund)));
                    } else {
                        currentData.add(nvl(r.getCountCurrent()));
                        lastData.add(nvl(r.getCountLast()));
                    }
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
        boolean byAmount = type.endsWith("amount");
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("type", type);

        Map<String, long[]> byChannel = new LinkedHashMap<String, long[]>();
        Map<String, long[]> byPayType = new LinkedHashMap<String, long[]>();
        Map<String, Map<String, Long[]>> cross = new LinkedHashMap<String, Map<String, Long[]>>();
        Set<String> payTypes = new LinkedHashSet<String>();

        if (dataConfig.isMybatisPlus() && hasDateRange(request)) {
            try {
                String scope = scopeOf(type, "SUMMARY");
                Date[] lastRange = lastYearRange(request);
                List<InpatPrepayChtEntity> rows = inpatPrepayMapper.queryChannel(
                        day(request.getStartDate()), day(request.getEndDate()), lastRange[0], lastRange[1], scope);
                for (InpatPrepayChtEntity r : rows) {
                    String channel = r.getChannel() == null ? "未知" : r.getChannel();
                    String payType = r.getPayWay() == null ? "未知" : r.getPayWay();
                    long cur = metricOf(r, byAmount, false);
                    long last = metricOf(r, byAmount, true);
                    accumulate(byChannel, channel, cur, last);
                    accumulate(byPayType, payType, cur, last);
                    payTypes.add(payType);
                    Map<String, Long[]> line = cross.get(channel);
                    if (line == null) {
                        line = new LinkedHashMap<String, Long[]>();
                        cross.put(channel, line);
                    }
                    Long[] cell = line.get(payType);
                    if (cell == null) {
                        line.put(payType, new Long[]{Long.valueOf(cur), Long.valueOf(last)});
                    } else {
                        cell[0] = Long.valueOf(cell[0].longValue() + cur);
                        cell[1] = Long.valueOf(cell[1].longValue() + last);
                    }
                }
            } catch (Exception e) {
                log.warn("查询住院预交金渠道分析失败", e);
            }
        }

        result.put("channelAnalysis", toNameValueList(byChannel));
        result.put("payTypeAnalysis", toNameValueList(byPayType));

        List<String> payTypeOrder = new ArrayList<String>(payTypes);
        List<Map<String, Object>> series = new ArrayList<Map<String, Object>>();
        for (Map.Entry<String, Map<String, Long[]>> e : cross.entrySet()) {
            List<Long> data = new ArrayList<Long>();
            for (String pt : payTypeOrder) {
                Long[] cell = e.getValue().get(pt);
                data.add(Long.valueOf(cell == null ? 0L : cell[0].longValue()));
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
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        Map<String, long[]> byPayType = new LinkedHashMap<String, long[]>();
        if (dataConfig.isMybatisPlus() && hasDateRange(request)) {
            try {
                Date[] lastRange = lastYearRange(request);
                List<InpatPrepayChtEntity> rows = inpatPrepayMapper.queryChannel(
                        day(request.getStartDate()), day(request.getEndDate()), lastRange[0], lastRange[1], "REFUND");
                for (InpatPrepayChtEntity r : rows) {
                    accumulate(byPayType, r.getPayWay() == null ? "未知" : r.getPayWay(),
                            metricOf(r, true, false), metricOf(r, true, true));
                }
            } catch (Exception e) {
                log.warn("查询住院预交金支付方式分析失败", e);
            }
        }
        result.put("payTypeAnalysis", toNameValueList(byPayType));
        return result;
    }

    // ==================== 工具方法 ====================

    /** 同比区间:本期日期整体减一年(9-01~9-03 → 去年9-01~9-03) */
    private static Date[] lastYearRange(InpatPrepayRequest request) {
        LocalDate start = toLocalDate(request.getStartDate()).minusYears(1);
        LocalDate end = toLocalDate(request.getEndDate()).minusYears(1);
        return new Date[]{toDate(start), toDate(end)};
    }

    /** 按月统计:dimension=month 时表格/趋势按月分桶 */
    private static boolean isMonthDimension(InpatPrepayRequest request) {
        return request != null && "month".equals(request.getDimension());
    }

    /** 格式化日期轴:按天 yyyy-MM-dd,按月 yyyy-MM */
    private static String formatDate(Date date, boolean month) {
        LocalDate d = toLocalDate(date);
        return month ? String.format("%04d-%02d", d.getYear(), d.getMonthValue()) : d.toString();
    }

    /** type → 数据范围:refund开头=退项,summary开头=汇总,其余=进项 */
    private static String scopeOf(String type, String def) {
        if (type == null || type.trim().isEmpty()) {
            return def;
        }
        String t = type.trim().toLowerCase();
        if (t.startsWith("refund")) {
            return "REFUND";
        }
        if (t.startsWith("summary")) {
            return "SUMMARY";
        }
        if (t.startsWith("income")) {
            return "INCOME";
        }
        return def;
    }

    private static String typeOf(InpatPrepayRequest request, String def) {
        String t = request == null ? null : request.getType();
        return (t == null || t.trim().isEmpty()) ? def : t;
    }

    private static boolean hasDateRange(InpatPrepayRequest request) {
        return request != null && request.getStartDate() != null && request.getEndDate() != null;
    }

    /** 退项金额取绝对值(流水里结算为负数) */
    private static double amountOf(BigDecimal v, boolean refund) {
        double d = v == null ? 0d : v.doubleValue();
        return refund ? Math.abs(d) : d;
    }

    /** 行内取数:金额或笔数,本期或同期 */
    private static long metricOf(InpatPrepayChtEntity r, boolean byAmount, boolean last) {
        if (byAmount) {
            BigDecimal v = last ? r.getAmountLast() : r.getAmountCurrent();
            return v == null ? 0L : Math.abs(Math.round(v.doubleValue()));
        }
        Integer v = last ? r.getCountLast() : r.getCountCurrent();
        return v == null ? 0L : v.longValue();
    }

    /** 同比百分比:同期为0返回0 */
    private static int comparePct(double current, double last) {
        if (last == 0d) {
            return 0;
        }
        return (int) Math.round((current - last) * 100d / last);
    }

    /** name -> {本期, 同期} 累加 */
    private static void accumulate(Map<String, long[]> target, String name, long cur, long last) {
        long[] cell = target.get(name);
        if (cell == null) {
            target.put(name, new long[]{cur, last});
        } else {
            cell[0] += cur;
            cell[1] += last;
        }
    }

    private static List<Map<String, Object>> toNameValueList(Map<String, long[]> src) {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        for (Map.Entry<String, long[]> e : src.entrySet()) {
            Map<String, Object> item = new LinkedHashMap<String, Object>();
            item.put("name", e.getKey());
            item.put("value", Long.valueOf(e.getValue()[0]));
            item.put("compare", Integer.valueOf(comparePct(e.getValue()[0], e.getValue()[1])));
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

    private static LocalDate toLocalDate(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private static Date toDate(LocalDate day) {
        return java.sql.Date.valueOf(day);
    }

    /** 归一成java.sql.Date:与同期参数类型一致,避免DATE列绑定TIMESTAMP触发隐式转换 */
    private static Date day(Date date) {
        return toDate(toLocalDate(date));
    }

    private static int nvl(Integer v) {
        return v == null ? 0 : v.intValue();
    }
}
