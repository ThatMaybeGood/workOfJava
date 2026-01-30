package com.mergedata.controller;

import com.mergedata.model.dto.ApiRequest;
import com.mergedata.model.dto.ApiRequestList;
import com.mergedata.model.dto.HolidayRequestBody;
import com.mergedata.model.vo.ApiResponse;
import com.mergedata.model.entity.YQHolidayCalendarEntity;
import com.mergedata.model.vo.ApiResponseBodyList;
 import com.mergedata.model.vo.YQHolidayCalendarVO;
import com.mergedata.server.YQHolidayService;
import com.mergedata.util.PrimaryKeyGenerator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.List;


/**
 * @author Mine
 * @version 1.0
 * 描述:
 * @date 2025/11/10 17:03
 */

@RestController
@RequestMapping("api")
@CrossOrigin(origins = "*")
@Tag(name = "节假日管理", description = "用于节假日相关的接口")
public class HolidayController {

    @Autowired
    @Qualifier("holidayServiceImpl")
    YQHolidayService holiday;


    @Operation(summary = "查询所有节假日数据", description = "返回节假日的列表数据")
    @PostMapping("findall")
    public ApiResponse<ApiResponseBodyList<YQHolidayCalendarEntity>> findALl(@Valid @RequestBody ApiRequest<Void> res)  {

        // 2. 避免重复调用服务，并使用转换后的 LocalDate
        List<YQHolidayCalendarEntity> resultList = holiday.findAll();

        // 4. 返回结果
        return ApiResponse.successList(resultList,"查询节假日列表成功");
    }


    @Operation(summary = "通过日期查询节假日数据", description = "返回节假日的数据")
    @PostMapping("findbydate")
    public ApiResponse<ApiResponseBodyList<YQHolidayCalendarEntity>> findByDate(@Valid  @RequestBody ApiRequest<YQHolidayCalendarEntity> res)  {
        // 2. 避免重复调用服务，并使用转换后的 LocalDate
        List<YQHolidayCalendarEntity> resultList = holiday.findByDate(res.getBody().getHolidayDate());
        // 4. 返回结果
        return ApiResponse.successList(resultList,"查询节假日成功");
    }


    @Operation(summary = "批量写入节假日数据", description = "返回操作结果")
    @PostMapping("batchinsert")
    public ApiResponse batchInsert(@Valid @RequestBody ApiRequestList<YQHolidayCalendarEntity> request)  {

        List<YQHolidayCalendarEntity> list = request.getBody().getList();
        for (YQHolidayCalendarEntity dto : list) {
            PrimaryKeyGenerator pk   = new PrimaryKeyGenerator();
            dto.setSerialNo(pk.generateKey());
            dto.setValidStatus("1");
        }


        // 2. 避免重复调用服务，并使用转换后的 LocalDate
        Boolean b = holiday.batchInsertList(list);
        if (!b) {
            return ApiResponse.failure("批量插入失败");
        }
        return ApiResponse.successList(list,"批量插入成功");
    }

    @Operation(summary = "写入节假日数据", description = "返回操作结果")
    @PostMapping("insert")
    public ApiResponse singleInsert(@Valid @RequestBody ApiRequest<YQHolidayCalendarEntity> request)  {

        YQHolidayCalendarEntity list = request.getBody();

        // 2. 避免重复调用服务，并使用转换后的 LocalDate
        Boolean b = holiday.insert(list);
        if (!b){
            return ApiResponse.failure("节假日插入失败");
        }

        return ApiResponse.success("节假日插入成功");
    }


    @Operation(summary = "作废删除节假日数据", description = "返回操作结果")
    @PostMapping("update")
    public ApiResponse update(@Valid @RequestBody ApiRequest<YQHolidayCalendarEntity> request)  {

        YQHolidayCalendarEntity list = request.getBody();

        // 2. 避免重复调用服务，并使用转换后的 LocalDate
        Boolean b = holiday.update(list);
        if (!b){
            return ApiResponse.failure("节假日作废失败");
        }

        return ApiResponse.success("节假日作废成功");
    }
    // 根据对应日期 和请求类型 判断对应日期结果
    @Operation(summary = "查询日期的类型", description = "返回操作结果")
    @PostMapping("querydatetype/holidayDate")
    public ApiResponse<YQHolidayCalendarVO> queryType(@Valid @RequestBody ApiRequest<HolidayRequestBody> request) {

        return ApiResponse.successObj(holiday.queryDateType(request.getBody()),"查询节假日类型成功");
    }




    //使用路径变量
    @Operation(summary = "查询日期的类型", description = "返回操作结果")
    @GetMapping("/querydatetype/{holidayDate}")
    public String queryDateType(
            @Parameter(description = "查询日期，格式：yyyy-MM-dd", example = "2026-01-01")
            @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate holidayDate) {

        if (holidayDate == null) {
            holidayDate = LocalDate.now();
        }
        return holiday.getDateType(holidayDate);
    }
//  使用 @RequestParam
//    @Operation(summary = "查询日期的类型", description = "返回操作结果")
//    @GetMapping("/querydatetype")
//    public Integer queryDateType(
//            @Parameter(description = "查询日期，格式：yyyy-MM-dd", example = "2024-01-19")
//            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate holidayDate) {
//        return holiday.queryDateType(holidayDate);
//    }

}
