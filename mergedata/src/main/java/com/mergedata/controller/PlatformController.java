package com.mergedata.controller;

import com.mergedata.model.dto.ApiRequest;
import com.mergedata.model.dto.ApiRequestList;
import com.mergedata.model.dto.CommonRequestBody;
import com.mergedata.model.dto.HolidayRequestBody;
import com.mergedata.model.entity.YQHolidayEntity;
import com.mergedata.model.entity.YQOperatorEntity;
import com.mergedata.model.vo.ApiResponse;
import com.mergedata.model.vo.ApiResponseBodyList;
import com.mergedata.model.vo.YQHolidayCalendarVO;
import com.mergedata.server.YQHolidayService;
import com.mergedata.server.YQOperatorService;
import com.mergedata.util.PrimaryKeyGenerator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * @author Mine
 * @version 1.0
 * 描述:
 * @date 2025/11/10 17:03
 */

@RestController
@RequestMapping("paltform")
@CrossOrigin(origins = "*")
@Tag(name = "节假日管理", description = "用于节假日相关的接口")
public class PlatformController {

    @Autowired
    YQHolidayService holiday;

    @Autowired
    YQOperatorService operator;

    @Operation(summary = "平台查询所有节假日数据", description = "返回节假日的列表数据")
    @PostMapping("holidays/findall")
    public ApiResponse<ApiResponseBodyList<YQHolidayEntity>> holidayFindALl(@Valid @RequestBody ApiRequest<CommonRequestBody> res)  {
        List<YQHolidayEntity> resultList = new ArrayList<>();

        if ( res.getBody().getExtendParams1() == null || res.getBody().getExtendParams1().isEmpty()) {
            // 2. 避免重复调用服务，并使用转换后的 LocalDate
            resultList = holiday.findAll();
        }else {
            resultList = holiday.findByYear(Integer.valueOf(res.getBody().getExtendParams1()));
        }
        // 4. 返回结果
        return ApiResponse.successList(resultList,"查询节假日列表成功");
    }


    @Operation(summary = "写入节假日数据", description = "返回操作结果")
    @PostMapping("holidays/insert")
    public ApiResponse holidaySingleInsert(@Valid @RequestBody ApiRequest<YQHolidayEntity> request)  {

        YQHolidayEntity list = request.getBody();

        // 2. 避免重复调用服务，并使用转换后的 LocalDate
        Boolean b = holiday.insert(list);
        if (!b){
            return ApiResponse.failure("节假日插入失败");
        }

        return ApiResponse.success("节假日插入成功");
    }


    @Operation(summary = "作废删除节假日数据", description = "返回操作结果")
    @PostMapping("holidays/update")
    public ApiResponse holidayUpdate(@Valid @RequestBody ApiRequest<YQHolidayEntity> request)  {

        YQHolidayEntity list = request.getBody();

        // 2. 避免重复调用服务，并使用转换后的 LocalDate
        Boolean b = holiday.update(list);
        if (!b){
            return ApiResponse.failure("节假日作废失败");
        }

        return ApiResponse.success("节假日作废成功");
    }



    @Operation(summary = "查询所有的操作员数据", description = "返回操作员列表数据")
    @PostMapping("operators/findall")
    public ApiResponse<ApiResponseBodyList<YQOperatorEntity>> operatorFindALl(@Valid @RequestBody  ApiRequest<CommonRequestBody> res)  {
        List<YQOperatorEntity> resultList = new ArrayList<>();

        String category = res.getBody().getExtendParams1();

        if(category== null || category.isEmpty()) {
            // 2. 避免重复调用服务，并使用转换后的 LocalDate
             resultList = operator.findAll();
        }else {
             resultList = operator.findByCategory(category);
        }


        // 4. 返回结果
        return ApiResponse.successList(resultList,"查询操作员列表成功！");
    }


    @Operation(summary = "写入操作员数据", description = "返回操作结果")
    @PostMapping("operators/insert")
    public ApiResponse operatorSingleInsert(@Valid @RequestBody ApiRequest<YQOperatorEntity> request)  {

        YQOperatorEntity list = request.getBody();

        // 2. 避免重复调用服务，并使用转换后的 LocalDate
        Boolean b = operator.insert(list);
        if (!b) {
            return ApiResponse.failure("插入操作员信息失败");
        }
        return ApiResponse.successList(null,"插入操作员信息成功");
    }

    @Operation(summary = "作废删除操作员数据", description = "返回操作结果")
    @PostMapping("operators/delete")
    public ApiResponse operatorDelete(@Valid @RequestBody ApiRequest<YQOperatorEntity> request)  {

        YQOperatorEntity list = request.getBody();

        // 2. 避免重复调用服务，并使用转换后的 LocalDate
        Boolean b = operator.delete(list);
        if (!b) {
            return ApiResponse.failure("更新操作员信息失败");
        }
        return ApiResponse.success("更新操作员信息成功");
    }

}
