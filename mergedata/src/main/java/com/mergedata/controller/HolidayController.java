package com.mergedata.controller;

import com.mergedata.model.dto.ApiRequest;
import com.mergedata.model.dto.ApiRequestList;
import com.mergedata.model.dto.HolidayRequestBody;
import com.mergedata.model.vo.ApiResponse;
import com.mergedata.model.entity.YQHolidayEntity;
import com.mergedata.model.vo.ApiResponseBodyList;
 import com.mergedata.model.vo.YQHolidayCalendarVO;
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
@RequestMapping("api")
@CrossOrigin(origins = "*")
@Tag(name = "节假日管理", description = "用于节假日相关的接口")
public class HolidayController {

    @Autowired
    @Qualifier("holidayServiceImpl")
    YQHolidayService holiday;


    @Operation(summary = "查询所有节假日数据", description = "返回节假日的列表数据")
    @PostMapping("findall")
    public ApiResponse<ApiResponseBodyList<YQHolidayEntity>> findALl(@Valid @RequestBody ApiRequest<Void> res)  {

        // 2. 避免重复调用服务，并使用转换后的 LocalDate
        List<YQHolidayEntity> resultList = holiday.findAll();

        // 4. 返回结果
        return ApiResponse.successList(resultList,"查询节假日列表成功");
    }


    @Operation(summary = "通过日期查询节假日数据", description = "返回节假日的数据")
    @PostMapping("findbydate")
    public ApiResponse<ApiResponseBodyList<YQHolidayEntity>> findByDate(@Valid  @RequestBody ApiRequest<YQHolidayEntity> res)  {
        // 2. 避免重复调用服务，并使用转换后的 LocalDate
        List<YQHolidayEntity> resultList = holiday.findByDate(res.getBody().getHolidayDate());
        // 4. 返回结果
        return ApiResponse.successList(resultList,"查询节假日成功");
    }


    @Operation(summary = "批量写入节假日数据", description = "返回操作结果")
    @PostMapping("batchinsert")
    public ApiResponse batchInsert(@Valid @RequestBody ApiRequestList<YQHolidayEntity> request)  {

        List<YQHolidayEntity> list = request.getBody().getList();
        for (YQHolidayEntity dto : list) {
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
    public ApiResponse singleInsert(@Valid @RequestBody ApiRequest<YQHolidayEntity> request)  {

        YQHolidayEntity list = request.getBody();

        // 2. 避免重复调用服务，并使用转换后的 LocalDate
        Boolean b = holiday.insert(list);
        if (!b){
            return ApiResponse.failure("节假日插入失败");
        }

        return ApiResponse.success("节假日插入成功");
    }


    @Operation(summary = "作废删除节假日数据", description = "返回操作结果")
    @PostMapping("update")
    public ApiResponse update(@Valid @RequestBody ApiRequest<YQHolidayEntity> request)  {

        YQHolidayEntity list = request.getBody();

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

        YQHolidayCalendarVO yqHolidayCalendarVO = new YQHolidayCalendarVO();
        yqHolidayCalendarVO.setHolidayDate(request.getBody().getReportDate());
        yqHolidayCalendarVO.setQueryType(request.getBody().getQueryType());
        yqHolidayCalendarVO.setHolidayType(holiday.queryDateType(request.getBody().getReportDate(), request.getBody().getQueryType()));

        return ApiResponse.successObj(yqHolidayCalendarVO,"查询节假日类型成功");
    }

}
