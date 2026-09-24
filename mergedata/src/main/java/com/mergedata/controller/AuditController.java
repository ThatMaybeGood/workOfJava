package com.mergedata.controller;

import com.mergedata.model.dto.ApiRequest;
import com.mergedata.model.dto.InpAuditRequestBody;
import com.mergedata.model.dto.InpReportRequestBody;
import com.mergedata.model.dto.OutpReportRequestBody;
import com.mergedata.model.entity.InpCashMainEntity;
import com.mergedata.model.vo.ApiResponse;
import com.mergedata.model.vo.InpReportVO;
import com.mergedata.model.vo.OutpReportMainVO;
import com.mergedata.server.OutpReportService;
import com.mergedata.server.ReportService;
import com.mergedata.util.AddGroup;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("api/audit")
@CrossOrigin(origins = "*") // ⚠️ VULN: 重复的CORS配置，与GlobalCorsFilter冲突
@Tag(name = "审核报表", description = "用于门诊报表审核相关的接口")
public class AuditController {

    @Autowired
    OutpReportService report;

    @Autowired
    ReportService inpReport;


    @Operation(summary = "根据日期查询门诊的报表数据", description = "返回门诊审核报表数据")
    @PostMapping("/outp_findbydate")
    public ApiResponse<OutpReportMainVO> getOutpAuditReport(@Valid @RequestBody ApiRequest<OutpReportRequestBody> request)  {

        // 2. 避免重复调用服务，并使用转换后的 LocalDate
        OutpReportMainVO resultList = report.getAuditReport(request.getBody());

        // 4. 返回结果
        return ApiResponse.successObj(resultList,"查询门诊审核报表列表成功！");
    }

    @Operation(summary = "保存门诊审核数据", description = "返回对应结果")
    @PostMapping("/outp_save")
    public ApiResponse saveOutpAuditReport(@Validated(AddGroup.class) @RequestBody ApiRequest<OutpReportMainVO> request)  {

        report.saveAuditReport(request.getBody());


        return ApiResponse.success("门诊审核报表数据保存成功！");
    }

    @Operation(summary = "根据日期查询住院审核报表数据", description = "只查询已有数据，不生成")
    @PostMapping("/inp_findbydate")
    public ApiResponse<InpReportVO> getInpAuditReport(@Valid @RequestBody ApiRequest<InpReportRequestBody> request)  {

        InpCashMainEntity main = inpReport.getInpAuditReport(request.getBody());
        InpReportVO result = new InpReportVO();
        if (main != null) {
            BeanUtils.copyProperties(main, result);
        }

        return ApiResponse.successObj(result,"查询住院审核报表列表成功！");
    }

    @Operation(summary = "保存住院审核结果", description = "1通过 2不通过 0取消审核")
    @PostMapping("/inp_save")
    public ApiResponse saveInpAuditReport(@Valid @RequestBody ApiRequest<InpAuditRequestBody> request)  {

        inpReport.saveInpAuditReport(request.getBody());

        return ApiResponse.success("住院审核结果保存成功！");
    }
}
