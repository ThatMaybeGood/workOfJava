package com.mergedata.controller;

import com.mergedata.dto.ApiRequest;
import com.mergedata.dto.ApiRequestList;
import com.mergedata.dto.ApiResponse;
import com.mergedata.model.YQOperator;
import com.mergedata.server.YQOperatorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Null;
import java.util.List;


/**
 * @author Mine
 * @version 1.0
 * 描述:
 * @date 2025/11/10 17:03
 */

@RestController
@RequestMapping("api/operators")
@CrossOrigin(origins = "*")
public class OperatorController {
 
    @Qualifier("operatorServiceImpl")
    @Autowired
    YQOperatorService operator; 



    @PostMapping("findall")
    public ApiResponse<YQOperator> findALl(ApiRequest<Null> res)  {

        // 2. 避免重复调用服务，并使用转换后的 LocalDate
        List<YQOperator> resultList = operator.findAll();

        if (resultList.isEmpty()) {
            return ApiResponse.failure("查询操作人员错误");
        }

        // 4. 返回结果
        return ApiResponse.success(resultList);
    }

    @PostMapping("findbydate")
    public ApiResponse<YQOperator> findByDate(ApiRequest<YQOperator> res)  {
        // 2. 避免重复调用服务，并使用转换后的 LocalDate
        List<YQOperator> resultList = operator.findAll();
        // 4. 返回结果
        return ApiResponse.success(resultList);
    }

    @PostMapping("batchinsert")
    public Boolean batchInsert(@RequestBody ApiRequestList<YQOperator> request)  {

        List<YQOperator> list = request.getBody().getList();

        // 2. 避免重复调用服务，并使用转换后的 LocalDate
        Boolean b = operator.batchInsert(list);
        return b;
    }


    @PostMapping("insert")
    public Boolean singleInsert(@RequestBody ApiRequest<YQOperator> request)  {

        YQOperator list = request.getBody();

        // 2. 避免重复调用服务，并使用转换后的 LocalDate
        Boolean b = operator.insert(list); ;
        return b;
    }


    @PostMapping("update")
    public Boolean delete(@RequestBody ApiRequest<YQOperator> request)  {

        YQOperator list = request.getBody();

        // 2. 避免重复调用服务，并使用转换后的 LocalDate
        Boolean b = operator.update(list); ;
        return b;
    }

    @GetMapping("/")
    public Result<String> getHolidayApiInfo() {
        return Result.success("API服务已启动，可用端点：/R /data, /insert");
    }

}
