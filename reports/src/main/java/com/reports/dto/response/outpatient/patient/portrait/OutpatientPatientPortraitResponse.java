package com.reports.dto.response.outpatient.patient.portrait;

import lombok.Data;
import java.util.List;

/**
 * 患者画像 - 响应体
 */
@Data
public class OutpatientPatientPortraitResponse {

    private static final long serialVersionUID = 1L;

    private AgeAnalysis ageAnalysis;
    private List<AnalysisItem> insuranceAnalysis;
    private List<AnalysisItem> identityAnalysis;
    private List<AnalysisItem> registerOriginAnalysis;
    private List<AnalysisItem> archiveOriginAnalysis;

}
