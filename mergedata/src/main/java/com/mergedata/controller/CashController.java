package com.mergedata.controller;

import com.mergedata.dto.ApiRequest;
import com.mergedata.dto.ApiResponse;
import com.mergedata.dto.CommonRequestBody;
import com.mergedata.model.YQCashRegRecord;
import com.mergedata.server.YQCashService;
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
@RequestMapping("api/cashs")
@CrossOrigin(origins = "*")
public class CashController {

    @Autowired
    YQCashService cash;

    @PostMapping("findbydate")
    public ApiResponse<YQCashRegRecord> findALl(@Validated @RequestBody ApiRequest<CommonRequestBody> res)  {
        String reportdate = res.getBody().getReportdate();
        // 2. 避免重复调用服务，并使用转换后的 LocalDate
        List<YQCashRegRecord> resultList = cash.findByDate(reportdate);

        // 4. 返回结果
        return ApiResponse.success(resultList);
    }

    @GetMapping("/")
    public Result<String> getHolidayApiInfo() {
        return Result.success("API服务已启动，可用端点：/R /data, /insert");
    }

}
