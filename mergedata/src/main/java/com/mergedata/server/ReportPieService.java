package com.mergedata.server;

import com.mergedata.model.dto.InpReportRequestBody;
import com.mergedata.model.dto.OutpReportPieRequestBody;
import com.mergedata.model.dto.OutpReportRequestBody;
import com.mergedata.model.entity.InpCashMainEntity;
import com.mergedata.model.vo.OutpReportMainVO;
import com.mergedata.model.vo.pie.OutReportPieDTO;

public interface ReportPieService {

    /**
     * 得到门诊饼状图内容
     */
    OutReportPieDTO queryOutpReportPie(OutpReportPieRequestBody body)  ;

}
