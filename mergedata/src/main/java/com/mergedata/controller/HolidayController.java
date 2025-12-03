package com.mergedata.controller;

import com.mergedata.dto.ApiRequest;
import com.mergedata.dto.ApiRequestList;
import com.mergedata.dto.ApiResponse;
import com.mergedata.model.YQHolidayCalendar;
import com.mergedata.server.YQHolidayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
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
@RequestMapping("api/holidays")
@CrossOrigin(origins = "*")
public class HolidayController {

    @Autowired
    YQHolidayService holiday;



    @PostMapping("findall")
    public ApiResponse<YQHolidayCalendar> findALl(ApiRequest<Null> res)  {

        // 2. 避免重复调用服务，并使用转换后的 LocalDate
        List<YQHolidayCalendar> resultList = holiday.findAll();

        // 4. 返回结果
        return ApiResponse.success(resultList);
    }

    @PostMapping("findbydate")
    public ApiResponse<YQHolidayCalendar> findByDate(@Validated  @RequestBody ApiRequest<YQHolidayCalendar> res)  {
        // 2. 避免重复调用服务，并使用转换后的 LocalDate
        List<YQHolidayCalendar> resultList = holiday.findAll();
        // 4. 返回结果
        return ApiResponse.success(resultList);
    }

    @PostMapping("batchinsert")
    public Boolean batchInsert(@Validated @RequestBody ApiRequestList<YQHolidayCalendar> request)  {

        List<YQHolidayCalendar> list = request.getBody().getList();

        // 2. 避免重复调用服务，并使用转换后的 LocalDate
        Boolean b = holiday.batchInsertList(list); ;
        return b;
    }


    @PostMapping("singleinsert")
    public Boolean singleInsert(@Validated @RequestBody ApiRequest<YQHolidayCalendar> request)  {

        YQHolidayCalendar list = request.getBody();

        // 2. 避免重复调用服务，并使用转换后的 LocalDate
        Boolean b = holiday.batchInsert(list); ;
        return b;
    }



    @PostMapping("delete")
    public Boolean delete(@Validated @RequestBody ApiRequest<YQHolidayCalendar> request)  {

        YQHolidayCalendar list = request.getBody();

        // 2. 避免重复调用服务，并使用转换后的 LocalDate
        Boolean b = holiday.batchInsert(list);
        return b;
    }

    @GetMapping("/")
    public Result<String> getHolidayApiInfo() {
        return Result.success("API服务已启动，可用端点：/R /data, /insert");
    }

}
