package com.mergedata.controller;

import com.mergedata.model.dto.*;
import com.mergedata.model.vo.*;
import com.mergedata.util.AddGroup;
import com.mergedata.server.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;


/**
 * @author Mine
 * @version 1.0
 * 描述:
 * @date 2025/11/10 17:03
 */

@RestController
@RequestMapping("api/reports")
@CrossOrigin(origins = "*")
@Tag(name = "报表管理", description = "用于报表查询的接口")
public class ReportController {

    @Qualifier("reportServiceImpl")
    @Autowired
    ReportService report;

    @Operation(summary = "根据日期查询门诊报表数据", description = "返回门诊报表数据")
    @PostMapping("/findbydate")
    public ApiResponse<ApiResponseBodyList<OutpReportVO>> getOutpReport(@Valid @RequestBody ApiRequest<OutpReportRequestBody> request)  {

        // 2. 避免重复调用服务，并使用转换后的 LocalDate
        List<OutpReportVO> resultList = report.getOutpReport(request.getBody());

        // 4. 返回结果
        return ApiResponse.successList(resultList,"查询报表列表成功！");
    }

    @Operation(summary = "批量插入门诊报表数据", description = "返回对应结果")
    @PostMapping("/insert")
    public ApiResponse insertOutpReport(@Validated(AddGroup.class) @RequestBody ApiRequestList<OutpReportVO> request)  {

        List<OutpReportVO> list = request.getBody().getList();

        // 2. 避免重复调用服务，并使用转换后的 LocalDate
        Boolean b = report.insertOutpReport(list);
        if (b == false) {
            return ApiResponse.failure("报表写入失败！");
        }
        return ApiResponse.success("报表写入成功！");
    }

    @Operation(summary = "根据日期查询对应住院报表数据", description = "返回对应的住院报表数据")
    @PostMapping("/inp_findbydate")
    public ApiResponse<InpReportVO> getInpReport(@Valid @RequestBody ApiRequest<InpReportRequestBody> request)  {

        // 2. 避免重复调用服务，并使用转换后的 LocalDate
        InpReportVO resultList = report.getInpReport(request.getBody());

        // 4. 返回结果
        return ApiResponse.successObj(resultList,"查询报表列表成功！");
    }


    @Operation(summary = "批量插住院表数据", description = "返回对应结果")
    @PostMapping("/inp_insert")
    public ApiResponse insertInpReport(@RequestBody ApiRequest<InpReportVO> request)  {

        // 2. 避免重复调用服务，并使用转换后的 LocalDate
        Integer result = report.insertInpReport(request.getBody());

        if (result == 1) {
            return ApiResponse.success("住院报表明细写入成功！");
        }

        return ApiResponse.failure("住院报表写入失败！");
    }
}
