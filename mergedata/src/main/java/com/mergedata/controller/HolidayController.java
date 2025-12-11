package com.mergedata.controller;

import com.mergedata.dto.ApiRequest;
import com.mergedata.dto.ApiRequestList;
import com.mergedata.dto.ApiResponse;
import com.mergedata.model.YQHolidayCalendar;
import com.mergedata.server.YQHolidayService;
import com.mergedata.util.PrimaryKeyGenerator;
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
@RequestMapping("api/holidays")
@CrossOrigin(origins = "*")
@Tag(name = "节假日管理", description = "用于节假日相关的接口")
public class HolidayController {

    @Autowired
    @Qualifier("holidayServiceImpl")
    YQHolidayService holiday;


    @Operation(summary = "查询所有节假日数据", description = "返回节假日的列表数据")
    @PostMapping("findall")
    public ApiResponse<YQHolidayCalendar> findALl(@Valid @RequestBody ApiRequest<Void> res)  {

        // 2. 避免重复调用服务，并使用转换后的 LocalDate
        List<YQHolidayCalendar> resultList = holiday.findAll();

        // 4. 返回结果
        return ApiResponse.success(resultList,"查询节假日列表成功");
    }


    @Operation(summary = "通过日期查询节假日数据", description = "返回节假日的数据")
    @PostMapping("findbydate")
    public ApiResponse<YQHolidayCalendar> findByDate(@Valid  @RequestBody ApiRequest<YQHolidayCalendar> res)  {
        // 2. 避免重复调用服务，并使用转换后的 LocalDate
        List<YQHolidayCalendar> resultList = holiday.findByDate(res.getBody().getHolidayDate());
        // 4. 返回结果
        return ApiResponse.success(resultList,"查询节假日成功");
    }


    @Operation(summary = "批量写入节假日数据", description = "返回操作结果")
    @PostMapping("batchinsert")
    public ApiResponse batchInsert(@Valid @RequestBody ApiRequestList<YQHolidayCalendar> request)  {

        List<YQHolidayCalendar> list = request.getBody().getList();
        for (YQHolidayCalendar dto : list) {
            PrimaryKeyGenerator pk   = new PrimaryKeyGenerator();
            dto.setSerialNo(pk.generateKey());
            dto.setValidStatus("1");
        }


        // 2. 避免重复调用服务，并使用转换后的 LocalDate
        Boolean b = holiday.batchInsertList(list);
        if (!b) {
            return ApiResponse.failure("批量插入失败");
        }
        return ApiResponse.success(list,"批量插入成功");
    }

    @Operation(summary = "写入节假日数据", description = "返回操作结果")
    @PostMapping("insert")
    public ApiResponse singleInsert(@Valid @RequestBody ApiRequest<YQHolidayCalendar> request)  {

        YQHolidayCalendar list = request.getBody();

        // 2. 避免重复调用服务，并使用转换后的 LocalDate
        Boolean b = holiday.insert(list);
        if (!b){
            return ApiResponse.failure("节假日插入失败");
        }

        return ApiResponse.success("节假日插入成功");
    }


    @Operation(summary = "作废删除节假日数据", description = "返回操作结果")
    @PostMapping("update")
    public ApiResponse update(@Valid @RequestBody ApiRequest<YQHolidayCalendar> request)  {

        YQHolidayCalendar list = request.getBody();

        // 2. 避免重复调用服务，并使用转换后的 LocalDate
        Boolean b = holiday.update(list);
        if (!b){
            return ApiResponse.failure("节假日作废失败");
        }

        return ApiResponse.success("节假日作废成功");
    }

    @GetMapping("/")
    public Result<String> getHolidayApiInfo() {
        return Result.success("API服务已启动，可用端点：/R /data, /insert");
    }

}
