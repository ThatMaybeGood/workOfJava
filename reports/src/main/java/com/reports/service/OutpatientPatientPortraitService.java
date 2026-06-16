package com.reports.service;

import com.reports.dto.request.OutpatientPatientPortraitRequest;
import com.reports.dto.response.outpatient.patient.portrait.*;

import java.util.List;

/**
 * 患者画像服务
 */
public interface OutpatientPatientPortraitService {

    /**
     * 查询年龄分析
     */
    AgeAnalysis queryAgeAnalysis(OutpatientPatientPortraitRequest request);

    /**
     * 查询医保分析
     */
    List<AnalysisItem> queryInsuranceAnalysis(OutpatientPatientPortraitRequest request);

    /**
     * 查询身份分析
     */
    List<AnalysisItem> queryIdentityAnalysis(OutpatientPatientPortraitRequest request);

    /**
     * 查询挂号来源分析
     */
    List<AnalysisItem> queryRegisterOriginAnalysis(OutpatientPatientPortraitRequest request);

    /**
     * 查询建档来源分析
     */
    List<AnalysisItem> queryArchiveOriginAnalysis(OutpatientPatientPortraitRequest request);

}
