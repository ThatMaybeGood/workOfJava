package com.reports.service.impl;

import com.reports.dto.response.cash.discharge.settlement.PersonCountItem;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 出院结算人次统计行后处理(直查HIS与中间表模式共用)
 */
final class PersonCountMergeSupport {

    static final String MILITARY = "军队医改";
    static final String NORMAL = "普通患者";
    static final String SELF = "SELF";
    static final String CHANNEL_SELF = "自助结算";
    static final String CHANNEL_WINDOW = "窗口结算";

    private PersonCountMergeSupport() {
    }

    /**
     * 归并后按展示值重排：SQL按RAWTOHEX(CHARGE_TYPE)排序，同费别的多原始值归并后不相邻，
     * 前端合并单元格依赖相邻行，故按解码后的日期/费别/结算类别/(操作员/支付类别)稳定排序
     */
    static List<PersonCountItem> sortByDisplayKeys(List<PersonCountItem> list) {
        list.sort(Comparator.comparing(PersonCountItem::getItemDate, Comparator.nullsLast(String::compareTo))
                .thenComparing(PersonCountItem::getFeeType, Comparator.nullsLast(String::compareTo))
                .thenComparing(PersonCountItem::getSettleChannel, Comparator.nullsLast(String::compareTo))
                .thenComparing(PersonCountItem::getOperatorNo, Comparator.nullsLast(String::compareTo))
                .thenComparing(PersonCountItem::getPayType, Comparator.nullsLast(String::compareTo)));
        return list;
    }

    /** RAWTOHEX读回的hex还原为字节按GBK解码 */
    static String hexGbk(String hex) {
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

    static String feeTypeOf(String rawChargeTypeHex) {
        return MILITARY.equals(hexGbk(rawChargeTypeHex)) ? MILITARY : NORMAL;
    }

    static String channelOf(String code) {
        return SELF.equals(code) ? CHANNEL_SELF : CHANNEL_WINDOW;
    }

    /** 费别按原始CHARGE_TYPE分组，映射后按(日期+费别+结算类别)合并收据量/人次 */
    static List<PersonCountItem> mergeByFeeChannel(List<PersonCountItem> raw) {
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
                it.setPersonCnt(0);
                it.setInvoicePrintedTotal(0);
                return it;
            });
            item.setCnt(item.getCnt() + (r.getCnt() == null ? 0 : r.getCnt()));
            // 人次=各原始组(person_id+visit_id去重)人次之和; 同一就诊跨多个原始费别组会重复计(极少, 与原宽表口径一致)
            item.setPersonCnt(item.getPersonCnt() + (r.getPersonCnt() == null ? 0 : r.getPersonCnt()));
            item.setInvoicePrintedTotal(item.getInvoicePrintedTotal()
                    + (r.getInvoicePrintedTotal() == null ? 0 : r.getInvoicePrintedTotal()));
        }
        return new ArrayList<>(merged.values());
    }

    /** 同上，合并键含操作员 */
    static List<PersonCountItem> mergeByOperator(List<PersonCountItem> raw) {
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
                it.setPersonCnt(0);
                it.setInvoicePrintedTotal(0);
                return it;
            });
            item.setCnt(item.getCnt() + (r.getCnt() == null ? 0 : r.getCnt()));
            item.setPersonCnt(item.getPersonCnt() + (r.getPersonCnt() == null ? 0 : r.getPersonCnt()));
            item.setInvoicePrintedTotal(item.getInvoicePrintedTotal()
                    + (r.getInvoicePrintedTotal() == null ? 0 : r.getInvoicePrintedTotal()));
        }
        return new ArrayList<>(merged.values());
    }

    /** 仅普通患者：过滤军队医改行，payType转码，feeType置普通患者 */
    static List<PersonCountItem> filterPayType(List<PersonCountItem> raw) {
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
            item.setPersonCnt(r.getPersonCnt());
            item.setInvoicePrintedTotal(r.getInvoicePrintedTotal());
            result.add(item);
        }
        return result;
    }
}
