package com.reports.service.impl;

import com.reports.annotation.DataSource;
import com.reports.dto.request.StatDataSummaryRequest;
import com.reports.dto.response.stat.StatDataSummaryResponse;
import com.reports.dto.response.stat.StatDataSummaryResponse.StatCard;
import com.reports.dto.response.stat.StatDataSummaryResponse.StatColumn;
import com.reports.mapper.StatDataSummaryMapper;
import com.reports.service.StatDataSummaryHisService;
import com.reports.service.StatDataSummaryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 运营数据统计服务实现(互医库+powerrsp跨用户, SQL在 StatDataSummaryMapper.xml)
 */
@Slf4j
@Service
@DataSource("yq_powerihsp")
public class StatDataSummaryServiceImpl implements StatDataSummaryService {

    private final StatDataSummaryMapper statDataSummaryMapper;
    private final StatDataSummaryHisService statDataSummaryHisService;

    @Autowired
    public StatDataSummaryServiceImpl(StatDataSummaryMapper statDataSummaryMapper,
                                      StatDataSummaryHisService statDataSummaryHisService) {
        this.statDataSummaryMapper = statDataSummaryMapper;
        this.statDataSummaryHisService = statDataSummaryHisService;
    }

    @Override
    public StatDataSummaryResponse query(StatDataSummaryRequest request, int page, int pageSize) {
        String tab = request.getTab() == null ? "" : request.getTab();
        log.info("[运营数据统计] 页签={} {}~{}", tab, request.getStartDate(), request.getEndDate());
        boolean noTime = "doctor-response".equals(tab) || "recheck-appt".equals(tab);
        String startDate = noTime ? null : fmt(rangeStart(request));
        String endDate = noTime ? null : fmt(rangeEnd(request));

        switch (tab) {
            case "special-express":
                return table(cols(col("itemDate", "日期"), col("expressCnt", "快递数量"), col("specialCnt", "特色制剂快递数量")),
                        statDataSummaryMapper.bt1SpecialExpress(startDate, endDate));
            case "no-accept-long":
                return table(cols(col("deptCode", "科室编码"), col("deptName", "科室"), col("doctorName", "医生"),
                                col("closeReason", "关闭原因"), col("cnt", "数量")),
                        statDataSummaryMapper.bt2NoAcceptLong(startDate, endDate));
            case "no-accept-24h":
                return table(cols(col("deptCode", "科室编码"), col("deptName", "科室"), col("doctorName", "医生"),
                                col("cancelReason", "关闭原因"), col("cnt", "数量")),
                        statDataSummaryMapper.bt3NoAccept24h(startDate, endDate));
            case "doctor-response":
                return table(cols(col("rankNum", "排名"), col("doctorCode", "医生工号"), col("doctorName", "医生"),
                                col("responseDuration", "响应时间(小时)")),
                        statDataSummaryMapper.bt4DoctorResponse());
            case "rx-audit-time": {
                Map<String, Object> overall = statDataSummaryMapper.bt5RxAuditOverall(startDate, endDate);
                StatDataSummaryResponse resp = table(
                        cols(col("itemDate", "日期"), col("cnt", "审核处方数"), col("avgMinutes", "平均审核时长(分钟)")),
                        statDataSummaryMapper.bt5RxAuditDaily(startDate, endDate));
                resp.setCards(cards(card("审核处方数", num(overall.get("CNT")), "张"),
                        card("平均审核时长", num(overall.get("AVGMINUTES")), "分钟")));
                return resp;
            }
            case "clinic-unit":
                return statDataSummaryHisService.queryClinicUnit(startDate, endDate);
            case "patient-eval": {
                Map<String, Object> overall = statDataSummaryMapper.bt7EvalOverall(startDate, endDate);
                StatDataSummaryResponse resp = table(
                        cols(col("itemDate", "日期"), col("totalCnt", "评价数"), col("lowCnt", "低于5星"),
                                col("contentCnt", "有评语"), col("contentLowCnt", "有评语低于5星")),
                        statDataSummaryMapper.bt7EvalDaily(startDate, endDate));
                resp.setCards(cards(card("评价总数", num(overall.get("TOTALCNT")), "条"),
                        card("低于5星", num(overall.get("LOWCNT")), "条"),
                        card("有评语总数", num(overall.get("CONTENTCNT")), "条"),
                        card("有评语低于5星", num(overall.get("CONTENTLOWCNT")), "条")));
                return resp;
            }
            case "cancel-reason":
                return paged(cols(col("deptName", "科室"), col("doctorName", "医生"), col("outCallDate", "出诊日期"),
                                col("createUser", "建档ID"), col("userName", "姓名"), col("cancelReason", "取消原因"),
                                col("sex", "性别"), col("birthday", "出生日期"), col("age", "年龄")),
                        statDataSummaryMapper.bt8CancelReasonPaged(startDate, endDate, startRow(page, pageSize), endRow(page, pageSize)),
                        statDataSummaryMapper.bt8CancelReasonCount(startDate, endDate), page, pageSize);
            case "return-no-reg":
                return paged(cols(col("userName", "姓名"), col("createUser", "建档ID"), col("outCallDate", "就诊日期"),
                                col("doctorName", "就诊医生"), col("lockOperator", "挂号员"), col("addFlag", "加号标志"),
                                col("cancelReason", "取消原因"), col("orderStatus", "状态")),
                        statDataSummaryMapper.bt9ReturnNoRegPaged(startDate, endDate, startRow(page, pageSize), endRow(page, pageSize)),
                        statDataSummaryMapper.bt9ReturnNoRegCount(startDate, endDate), page, pageSize);
            case "pay-return":
                return table(cols(col("deptCode", "科室编码"), col("deptName", "科室"), col("payType", "支付类型"),
                                col("payCnt", "支付人数"), col("returnCnt", "当日退号数")),
                        statDataSummaryMapper.bt10PayReturn(startDate, endDate));
            case "dept-wait-time":
                return table(cols(col("deptCode", "科室编码"), col("deptName", "科室"),
                                col("waitMinutes", "平均候诊时长(分钟)"), col("receptMinutes", "平均接诊时长(分钟)")),
                        statDataSummaryMapper.bt11DeptWaitTime(startDate, endDate));
            case "appt-success":
                return table(cols(col("outCallDate", "就诊日期"), col("deptCode", "科室编码"), col("deptName", "科室"),
                                col("cnt", "预约成功量")),
                        statDataSummaryMapper.bt12ApptSuccess(startDate, endDate));
            case "recheck-appt":
                return table(cols(col("deptCode", "科室编码"), col("deptName", "科室"), col("parId", "医生工号"),
                                col("doctorName", "医生"), col("createUser", "操作员"), col("cnt", "预约量")),
                        statDataSummaryMapper.bt13RecheckAppt());
            default:
                log.warn("[运营数据统计] 未知页签: {}", tab);
                return new StatDataSummaryResponse();
        }
    }

    /** 未传日期默认上月1号 */
    private Date rangeStart(StatDataSummaryRequest request) {
        if (request.getStartDate() != null) {
            return request.getStartDate();
        }
        Calendar c = Calendar.getInstance();
        c.add(Calendar.MONTH, -1);
        c.set(Calendar.DAY_OF_MONTH, 1);
        return c.getTime();
    }

    /** 未传日期默认上月末 */
    private Date rangeEnd(StatDataSummaryRequest request) {
        if (request.getEndDate() != null) {
            return request.getEndDate();
        }
        Calendar c = Calendar.getInstance();
        c.set(Calendar.DAY_OF_MONTH, 1);
        c.add(Calendar.DATE, -1);
        return c.getTime();
    }

    private String fmt(Date d) {
        return d == null ? null : new SimpleDateFormat("yyyy-MM-dd").format(d);
    }

    private int startRow(int page, int pageSize) {
        return (page - 1) * pageSize + 1;
    }

    private int endRow(int page, int pageSize) {
        return page * pageSize;
    }

    private StatColumn col(String key, String label) {
        return new StatColumn(key, label);
    }

    private List<StatColumn> cols(StatColumn... columns) {
        List<StatColumn> list = new ArrayList<>();
        for (StatColumn c : columns) {
            list.add(c);
        }
        return list;
    }

    private StatCard card(String label, String value, String unit) {
        return new StatCard(label, value, unit);
    }

    private List<StatCard> cards(StatCard... cards) {
        List<StatCard> list = new ArrayList<>();
        for (StatCard c : cards) {
            list.add(c);
        }
        return list;
    }

    private StatDataSummaryResponse table(List<StatColumn> columns, List<Map<String, Object>> raw) {
        StatDataSummaryResponse resp = new StatDataSummaryResponse();
        resp.setColumns(columns);
        resp.setList(rows(raw, columns));
        return resp;
    }

    private StatDataSummaryResponse paged(List<StatColumn> columns, List<Map<String, Object>> raw, long total,
                                          int page, int pageSize) {
        StatDataSummaryResponse resp = table(columns, raw);
        resp.setPaged(true);
        resp.setTotal(total);
        resp.setPage(page);
        resp.setPageSize(pageSize);
        return resp;
    }

    /** Oracle返回的列键统一大写, 按列定义忽略大小写取值, 日期格式化, 空值置空串 */
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
                    v = fmt((Date) v);
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

    /** 数值去尾零 */
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
