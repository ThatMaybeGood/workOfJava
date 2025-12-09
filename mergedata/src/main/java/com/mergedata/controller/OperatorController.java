package com.mergedata.controller;

import com.mergedata.dto.ApiRequest;
import com.mergedata.dto.ApiRequestList;
import com.mergedata.dto.ApiResponse;
import com.mergedata.model.YQOperator;
import com.mergedata.server.YQOperatorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "操作员管理", description = "用于报表涉及操作员的接口")
public class OperatorController {
 
    @Qualifier("operatorServiceImpl")
    @Autowired
    YQOperatorService operator;


    @Operation(summary = "查询所有的操作员数据", description = "返回操作员列表数据")
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

    @Operation(summary = "通过操作员ID的查询数据", description = "返回改对应操作员数据")
    @PostMapping("findbyid")
    public ApiResponse<YQOperator> findByDate(@Valid @RequestBody ApiRequest<YQOperator> res)  {
        // 2. 避免重复调用服务，并使用转换后的 LocalDate
        List<YQOperator> resultList = operator.findByID(res.getBody());
        // 4. 返回结果
        return ApiResponse.success(resultList,"查询操作员信息成功");
    }

    @Operation(summary = "批量写入操作员数据", description = "返回操作结果")
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

    @Operation(summary = "写入操作员数据", description = "返回操作结果")
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

    @Operation(summary = "作废删除操作员数据", description = "返回操作结果")
    @PostMapping("delete")
    public ApiResponse delete(@Valid @RequestBody ApiRequest<YQOperator> request)  {

        YQOperator list = request.getBody();

        // 2. 避免重复调用服务，并使用转换后的 LocalDate
        Boolean b = operator.delete(list);
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
