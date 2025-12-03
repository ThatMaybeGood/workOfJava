package com.mergedata.controller;

import com.mergedata.dto.ApiRequest;
import com.mergedata.dto.ApiRequestList;
import com.mergedata.dto.ApiResponse;
import com.mergedata.dto.CommonRequestBody;
import com.mergedata.model.AddGroup;
import com.mergedata.model.Report;
import com.mergedata.server.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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
public class ReportController {

    @Autowired
    ReportService report;

    @PostMapping("/all")
    public ApiResponse<Report> findALl(@Validated @RequestBody ApiRequest<CommonRequestBody> request)  {

        String reportdate = request.getBody().getReportdate();

        // 2. 避免重复调用服务，并使用转换后的 LocalDate
        List<Report> resultList = report.getAll(reportdate);

        // 4. 返回结果
        return ApiResponse.success(resultList);
    }


    @PostMapping("batchinsert")
    public Boolean batchInsert(@Validated(AddGroup.class) @RequestBody ApiRequestList<Report> request)  {

        List<Report> list = request.getBody().getList();

        // 2. 避免重复调用服务，并使用转换后的 LocalDate
        Boolean b = report.batchInsert(list); ;
        return b;
    }



    @GetMapping("/")
    public Result<String> getHolidayApiInfo() {
        return Result.success("API服务已启动，可用端点：/R /data, /insert");
    }

}
