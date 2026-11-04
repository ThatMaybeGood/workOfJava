package com.reports.service;

import com.reports.dto.response.cash.mi.MiReconOrgDtlItem;

import java.util.List;

/**
 * 医疗机构侧对账数据服务(占坑: SQL在 MiReconMedInstMapper.xml 补全)
 * <p>
 * HIS侧出原始行(6字段, 不聚合), 按医保类型+日期范围返回,
 * 与医保上传数据(tr_mi_recon_detail)做同类型同日期的金额对比, 聚合在调用方代码里做。
 */
public interface MiReconMedInstService {

    /**
     * 医疗机构侧原始行: 日期/数量/医疗总金额/类型代码/类型名称/来源类型
     * insurTypeName与insurType配套传入, SQL直出
     */
    List<MiReconOrgDtlItem> queryOrgData(String insurType, String insurTypeName, String startDate, String endDate);
}
