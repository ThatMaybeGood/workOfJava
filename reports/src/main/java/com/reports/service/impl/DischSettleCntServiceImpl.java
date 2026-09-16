package com.reports.service.impl;

import com.reports.annotation.DataSource;
import com.reports.config.ReportDataConfig;
import com.reports.dto.request.DischSettleCntRequest;
import com.reports.dto.response.cash.discharge.settlement.PersonCountItem;
import com.reports.mapper.DischSettleCntMapper;
import com.reports.service.DischSettleCntService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 出院结算人次统计服务实现
 * 直查 HIS 业务库（inp_settle_master），经 @DataSource("his") 切换数据源
 * HIS库为US7ASCII(实际存GBK字节)：SQL不下推中文，取原始值后转码翻译
 */
@Slf4j
@Service
@DataSource("his")
public class DischSettleCntServiceImpl implements DischSettleCntService {

    private static final String MILITARY = "军队医改";
    private static final String NORMAL = "普通患者";
    private static final String SELF = "SELF";
    private static final String CHANNEL_SELF = "自助结算";
    private static final String CHANNEL_WINDOW = "窗口结算";

    private final ReportDataConfig dataConfig;
    private final DischSettleCntMapper dischSettleCntMapper;

    @Autowired
    public DischSettleCntServiceImpl(ReportDataConfig dataConfig, DischSettleCntMapper dischSettleCntMapper) {
        this.dataConfig = dataConfig;
        this.dischSettleCntMapper = dischSettleCntMapper;
    }

    @Override
    public List<PersonCountItem> queryPersonCount(DischSettleCntRequest request) {
        String dimension = request.getDimension() == null ? "summary" : request.getDimension();
        String timeDimension = request.getTimeDimension() == null ? "day" : request.getTimeDimension();
        log.info("查询出院结算人次，dimension={}，timeDimension={}，mode={}",
                dimension, timeDimension, dataConfig.getMode());
        if (dataConfig.isMock()) {
            return new ArrayList<>();
        }
        try {
            List<PersonCountItem> result;
            switch (dimension) {
                case "operator":
                    result = mergeByOperator(dischSettleCntMapper.queryByOperator(
                            request.getStartDate(), request.getEndDate(), timeDimension));
                    break;
                case "payType":
                    result = filterPayType(dischSettleCntMapper.queryByPayType(
                            request.getStartDate(), request.getEndDate(), timeDimension));
                    break;
                default:
                    result = mergeByFeeChannel(dischSettleCntMapper.queryByFeeChannel(
                            request.getStartDate(), request.getEndDate(), timeDimension));
                    break;
            }
            return sortByDisplayKeys(result);
        } catch (Exception e) {
            log.warn("查询出院结算人次失败", e);
            return new ArrayList<>();
        }
    }

    /**
     * 归并后按展示值重排：SQL按RAWTOHEX(CHARGE_TYPE)排序，同费别的多原始值归并后不相邻，
     * 前端合并单元格依赖相邻行，故按解码后的日期/费别/结算类别/(操作员/支付类别)稳定排序
     */
    private static List<PersonCountItem> sortByDisplayKeys(List<PersonCountItem> list) {
        list.sort(Comparator.comparing(PersonCountItem::getItemDate, Comparator.nullsLast(String::compareTo))
                .thenComparing(PersonCountItem::getFeeType, Comparator.nullsLast(String::compareTo))
                .thenComparing(PersonCountItem::getSettleChannel, Comparator.nullsLast(String::compareTo))
                .thenComparing(PersonCountItem::getOperatorNo, Comparator.nullsLast(String::compareTo))
                .thenComparing(PersonCountItem::getPayType, Comparator.nullsLast(String::compareTo)));
        return list;
    }

    /** RAWTOHEX读回的hex还原为字节按GBK解码 */
    private static String hexGbk(String hex) {
        if (hex == null) {
            return null;
        }
        try {
            byte[] bytes = new byte[hex.length() / 2];
            for (int i = 0; i < bytes.length; i++) {
                bytes[i] = (byte) Integer.parseInt(hex.substring(i * 2, i * 2 + 2), 16);
            }
            return new String(bytes, "GBK").trim();
        } catch (Exception e) {
            return hex;
        }
    }

    private static String feeTypeOf(String rawChargeTypeHex) {
        return MILITARY.equals(hexGbk(rawChargeTypeHex)) ? MILITARY : NORMAL;
    }

    private static String channelOf(String code) {
        return SELF.equals(code) ? CHANNEL_SELF : CHANNEL_WINDOW;
    }

    /** 费别按原始CHARGE_TYPE分组，映射后按(日期+费别+结算类别)合并人次 */
    private static List<PersonCountItem> mergeByFeeChannel(List<PersonCountItem> raw) {
        Map<String, PersonCountItem> merged = new LinkedHashMap<>();
        for (PersonCountItem r : raw) {
            String feeType = feeTypeOf(r.getFeeType());
            String channel = channelOf(r.getSettleChannel());
            String key = r.getItemDate() + "|" + feeType + "|" + channel;
            PersonCountItem item = merged.computeIfAbsent(key, k -> {
                PersonCountItem it = new PersonCountItem();
                it.setItemDate(r.getItemDate());
                it.setFeeType(feeType);
                it.setSettleChannel(channel);
                it.setCnt(0);
                return it;
            });
            item.setCnt(item.getCnt() + (r.getCnt() == null ? 0 : r.getCnt()));
        }
        return new ArrayList<>(merged.values());
    }

    /** 同上，合并键含操作员 */
    private static List<PersonCountItem> mergeByOperator(List<PersonCountItem> raw) {
        Map<String, PersonCountItem> merged = new LinkedHashMap<>();
        for (PersonCountItem r : raw) {
            String feeType = feeTypeOf(r.getFeeType());
            String channel = channelOf(r.getSettleChannel());
            String operatorName = hexGbk(r.getOperatorName());
            String key = r.getItemDate() + "|" + feeType + "|" + channel + "|" + r.getOperatorNo();
            PersonCountItem item = merged.computeIfAbsent(key, k -> {
                PersonCountItem it = new PersonCountItem();
                it.setItemDate(r.getItemDate());
                it.setFeeType(feeType);
                it.setSettleChannel(channel);
                it.setOperatorNo(r.getOperatorNo());
                it.setOperatorName(operatorName);
                it.setCnt(0);
                return it;
            });
            item.setCnt(item.getCnt() + (r.getCnt() == null ? 0 : r.getCnt()));
        }
        return new ArrayList<>(merged.values());
    }

    /** 仅普通患者：过滤军队医改行，payType转码，feeType置普通患者 */
    private static List<PersonCountItem> filterPayType(List<PersonCountItem> raw) {
        List<PersonCountItem> result = new ArrayList<>();
        for (PersonCountItem r : raw) {
            String payType = hexGbk(r.getPayType());
            if (MILITARY.equals(payType)) {
                continue;
            }
            PersonCountItem item = new PersonCountItem();
            item.setItemDate(r.getItemDate());
            item.setFeeType(NORMAL);
            item.setSettleChannel(channelOf(r.getSettleChannel()));
            item.setPayType(payType);
            item.setCnt(r.getCnt());
            result.add(item);
        }
        return result;
    }
}
