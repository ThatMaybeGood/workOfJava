package com.reports.dto.response.outpatient.patient.portrait;

import lombok.Data;
import java.util.List;

/**
 * 患者画像 - 年龄分析
 */
@Data
public class AgeAnalysis {

    private List<String> categories;
    private List<Integer> archiveData;
    private List<Integer> outpatientData;

}
