package com.mergedata.controller;

import com.mergedata.dto.ApiRequest;
import com.mergedata.dto.ApiRequestList;
import com.mergedata.dto.ApiResponse;
import com.mergedata.model.YQOperator;
import com.mergedata.server.YQOperatorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
@RequestMapping("api/operators")
@CrossOrigin(origins = "*")
public class OperatorController {
 
    @Qualifier("operatorServiceImpl")
    @Autowired
    YQOperatorService operator; 



    @PostMapping("findall")
    public ApiResponse<YQOperator> findALl(@Valid @RequestBody  ApiRequest<Void> res)  {

        // 2. 避免重复调用服务，并使用转换后的 LocalDate
        List<YQOperator> resultList = operator.findAll();

        if (resultList.isEmpty()) {
            return ApiResponse.failure("查询操作人员错误");
        }

        // 4. 返回结果
        return ApiResponse.success(resultList,"查询操作员列表成功！");
    }

    @PostMapping("findbyid")
    public ApiResponse<YQOperator> findByDate(@Valid @RequestBody ApiRequest<YQOperator> res)  {
        // 2. 避免重复调用服务，并使用转换后的 LocalDate
        List<YQOperator> resultList = operator.findByID(res.getBody());
        // 4. 返回结果
        return ApiResponse.success(resultList,"查询操作员信息成功");
    }

    @PostMapping("batchinsert")
    public ApiResponse batchInsert(@Valid @RequestBody ApiRequestList<YQOperator> request)  {

        List<YQOperator> list = request.getBody().getList();

        // 2. 避免重复调用服务，并使用转换后的 LocalDate
        Boolean b = operator.batchInsert(list);
        if (!b) {
            return ApiResponse.failure("批量插入操作员信息失败");
        }
        return ApiResponse.success(null,"批量插入操作员信息成功");
    }


    @PostMapping("insert")
    public ApiResponse singleInsert(@Valid @RequestBody ApiRequest<YQOperator> request)  {

        YQOperator list = request.getBody();

        // 2. 避免重复调用服务，并使用转换后的 LocalDate
        Boolean b = operator.insert(list);
        if (!b) {
            return ApiResponse.failure("插入操作员信息失败");
        }
        return ApiResponse.success(null,"插入操作员信息成功");
    }


    @PostMapping("update")
    public ApiResponse update(@Valid @RequestBody ApiRequest<YQOperator> request)  {

        YQOperator list = request.getBody();

        // 2. 避免重复调用服务，并使用转换后的 LocalDate
        Boolean b = operator.update(list);
        if (!b) {
            return ApiResponse.failure("更新操作员信息失败");
        }
        return ApiResponse.success("更新操作员信息成功");
    }

    @GetMapping("/")
    public Result<String> getHolidayApiInfo() {
        return Result.success("API服务已启动，可用端点：/all /data, /insert");
    }

}
