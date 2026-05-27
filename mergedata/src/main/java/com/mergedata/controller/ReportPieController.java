package com.mergedata.controller;

import com.mergedata.model.dto.ApiRequest;
import com.mergedata.model.dto.OutpReportPieRequestBody;
import com.mergedata.model.vo.ApiResponse;
import com.mergedata.model.vo.OutpReportMainVO;
import com.mergedata.model.vo.pie.OutReportPieDTO;
import com.mergedata.server.ReportPieService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * @author Mine
 * @version 1.0
 * 描述:
 * @date 2025/11/10 17:03
 */

@RestController
@RequestMapping("pie/reports")
@CrossOrigin(origins = "*")
@Tag(name = "报表饼状图", description = "用于报表饼状图等接口")
public class ReportPieController {

    @Autowired
    private ReportPieService pie;

    @Operation(summary = "根据日期查询门诊报表总计饼状图数据", description = "返回门诊总计饼状图报表数据")
    @PostMapping("/outp-reports")
    public ApiResponse<OutReportPieDTO> getOutpReportPieTotal(@Valid @RequestBody ApiRequest<OutpReportPieRequestBody> request)  {
        OutReportPieDTO resultList = pie.queryOutpReportPie(request.getBody());
        return ApiResponse.successObj(resultList,"查询门诊报表列表成功！");
    }

}
