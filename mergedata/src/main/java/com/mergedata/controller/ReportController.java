package com.mergedata.controller;

import com.mergedata.model.dto.ApiRequest;
import com.mergedata.model.dto.ApiRequestList;
import com.mergedata.model.vo.ApiResponse;
import com.mergedata.model.dto.ReportRequestBody;
import com.mergedata.util.AddGroup;
import com.mergedata.model.dto.ReportDTO;
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

    @Operation(summary = "根据日期查询对应报表数据", description = "返回对应的报表数据")
    @PostMapping("/findbydate")
    public ApiResponse<ReportDTO> findALl(@Valid @RequestBody ApiRequest<ReportRequestBody> request)  {

//        LocalDate reportdate = request.getBody().getReportdate();

        // 2. 避免重复调用服务，并使用转换后的 LocalDate
        List<ReportDTO> resultList = report.getAll(request.getBody());

        // 4. 返回结果
        return ApiResponse.success(resultList,"查询报表列表成功！");
    }

    @Operation(summary = "写入报表相关数据", description = "返回对应结果")
    @PostMapping("insert")
    public ApiResponse batchInsert(@Validated(AddGroup.class) @RequestBody ApiRequestList<ReportDTO> request)  {

        List<ReportDTO> list = request.getBody().getList();

        // 2. 避免重复调用服务，并使用转换后的 LocalDate
        Boolean b = report.batchInsert(list);
        if (b == false) {
            return ApiResponse.failure("报表写入失败！");
        }
        return ApiResponse.success("报表写入成功！");
    }



    @GetMapping("/")
    public Result<String> getHolidayApiInfo() {
        return Result.success("API服务已启动，可用端点：/R /data, /insert");
    }

}
