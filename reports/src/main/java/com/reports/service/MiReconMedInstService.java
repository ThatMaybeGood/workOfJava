package com.reports.service;

import com.reports.dto.response.cash.mi.MiReconCompareItem;

import java.util.List;

/**
 * 医疗机构侧对账数据服务(占坑: SQL待补全)
 * <p>
 * 按医保类型+结算日期范围返回医疗机构HIS侧的数量/医疗总费用,
 * 与医保上传数据(tr_mi_recon_detail)做同类型同日期的金额对比。
 */
public interface MiReconMedInstService {

    /**
     * 医疗机构侧汇总: 结算日期/数量/医疗总费用三字段, 类型代码310/390/3101/3901
     */
    List<MiReconCompareItem> queryOrgData(String insurType, String startDate, String endDate);
}
