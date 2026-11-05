package com.reports.service;

import com.reports.dto.request.MiReconRequest;
import com.reports.dto.response.cash.mi.MiReconCompareItem;
import com.reports.dto.response.cash.mi.MiReconFileItem;
import com.reports.dto.response.cash.mi.MiReconTableTotalItem;
import com.reports.dto.response.cash.mi.MiReconZpDiffItem;
import com.reports.entity.MiReconDetailEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 医保对账每日对比服务
 */
public interface MiReconService {

    /**
     * 对账对比: 医保上传数据 vs 医疗机构数据, 同类型同结算日期金额比对
     */
    List<MiReconCompareItem> queryCompare(MiReconRequest request);

    /**
     * 账平差异明细(某日): HIS业务表 vs 医保支付表, 双边账平按setl_id比对
     */
    List<MiReconZpDiffItem> queryZpDiff(String insurType, String settleDate);

    /**
     * 某日两表全量合计(不限zp_flag, 账平差异弹窗顶部对照)
     */
    List<MiReconTableTotalItem> queryDayTotals(String insurType, String settleDate);

    /**
     * 上传记录(最近100条)
     */
    List<MiReconFileItem> queryRecords();

    /**
     * 修改上传记录类型(级联明细)
     */
    int updateType(Long id, String insurType);

    /**
     * 上传医保对账文件: 解析落库, 同类型同结算日期以最新上传为准
     */
    Map<String, Object> upload(MultipartFile file, String insurType, String dataDate);
}
