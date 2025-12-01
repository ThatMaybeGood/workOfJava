package com.mergedata.controller;

import com.mergedata.dto.ApiRequest;
import com.mergedata.dto.ApiResponse;
import com.mergedata.dto.CommonRequestBody;
import com.mergedata.model.ReportDTO;
import com.mergedata.server.ReportService;
import com.mergedata.server.YQOperatorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * @author Mine
 * @version 1.0
 * 描述:
 * @date 2025/11/10 17:03
 */

@RestController
@RequestMapping()
@CrossOrigin(origins = "*")
public class ReportController {

    @Autowired
    private ReportService reportService;
    @Autowired
    YQOperatorService operatorService;


    @PostMapping("/data")
    public ApiResponse<ReportDTO> getData(@RequestBody ApiRequest<CommonRequestBody> request)  {

        String reportdate = request.getBody().getReportdate();
        // 2. 避免重复调用服务，并使用转换后的 LocalDate
        List<ReportDTO> resultList = reportService.getAll(reportdate);

        // 4. 返回结果
        return ApiResponse.success(resultList);
    }


//
//
//     @PostMapping("/oper/batch")
//    public ApiResponseALL<String> operBatchInsert(@RequestBody List<YQOperator> oper) {
//
//        boolean success = operatorService.batchInsert(oper);
//
//        if (success) {
//            // 写入成功，返回 200 OK
//            return ApiResponse.success("写入成功。");
//        } else {
//            // 写入失败，返回 500 Internal Server Error 或 400 Bad Request
//            // 具体取决于失败原因，这里用简单的失败提示
//            // Response.setStatus(500);
//            return ApiResponse.error("写入失败，请查看系统日志获取详细错误信息。");
//        }







    @GetMapping("/")
    public Result<String> getHolidayApiInfo() {
        return Result.success("API服务已启动，可用端点：/R /data, /insert");
    }

}
