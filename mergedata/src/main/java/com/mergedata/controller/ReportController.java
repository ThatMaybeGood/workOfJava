package com.mergedata.controller;

import com.mergedata.dto.HisIncomeDTO;
import com.mergedata.server.impl.ExternalApiRequestService;
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
    private ExternalApiRequestService reportService;

    @GetMapping("/data")
    public Result<List<HisIncomeDTO>> getData(@RequestParam String reportDate) {

        // 2. 避免重复调用服务，并使用转换后的 LocalDate
        List<HisIncomeDTO> resultList = reportService.getHisIncomeList(reportDate);

        // 3. 检查结果
        if (resultList == null || resultList.isEmpty()) {
            return Result.error("查询失败或无数据");
        }

        // 4. 返回结果
        return Result.success(resultList);
    }


    @GetMapping("/")
    public Result<String> getHolidayApiInfo() {
        return Result.success("API服务已启动，可用端点：/R /data, /insert");
    }

}
