package com.mergedata.controller;

import com.mergedata.model.dto.ApiRequest;
import com.mergedata.model.dto.OutpReportRequestBody;
import com.mergedata.model.vo.ApiResponse;
import com.mergedata.model.vo.OutpReportMainVO;
import com.mergedata.server.OutpReportService;
import com.mergedata.util.AddGroup;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * @author Mine
 * @version 1.0
 * 描述:
 * @date 2026/4/9 22:58
 */
@RestController
@RequestMapping("api/outp_financial")
@CrossOrigin(origins = "*")
@Tag(name = "门诊财务报表", description = "用于门诊财务报表相关的接口")
public class OutpFinancialController {

    @Autowired
    OutpReportService report;
    @Operation(summary = "根据参数查询门诊财务报表", description = "返回门诊财务报表饼状数据")
    @PostMapping("/query")
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
}
