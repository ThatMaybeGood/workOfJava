package com.showexcel.controller;

import com.showexcel.model.HolidayCalendar;
import com.showexcel.model.HolidayType;
import com.showexcel.server.HolidayService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/holiday")
@Slf4j
public class HolidayController {

    @Autowired
    private HolidayService holidayService;

    /**
     * 获取指定年份的节假日日历
     *
     * @param year 要查询的年份
     * @return 包含节假日日历列表的Result对象，成功时包含数据，失败时包含错误信息
     */
    @GetMapping("/calendar")
    public Result<List<HolidayCalendar>> getCalendar(@RequestParam Integer year) {
        try {
            List<HolidayCalendar> calendar = holidayService.getHolidayCalendar(year);
            return Result.success(calendar);
        } catch (Exception e) {
            log.error("获取节假日日历失败", e);
            return Result.error("获取节假日日历失败");
        }
    }

    /**
     * 批量更新节假日信息
     *
     * @param holidays 节假日日历列表，包含需要更新的节假日信息
     * @return 更新结果，成功返回true，失败返回false及错误信息
     */
    @PostMapping("/batch-update")
    public Result<Boolean> batchUpdate(@RequestBody List<HolidayCalendar> holidays) {
        try {
            log.info("接收到节假日批量更新请求，参数数量: {}", holidays.size());

            Boolean result = holidayService.batchUpdateHolidays(holidays);
            return result ? Result.success(true) : Result.error("更新失败");
        } catch (Exception e) {
            log.error("批量更新节假日失败", e);
            return Result.error("批量更新失败");
        }
    }

    /**
     * 标记节假日状态
     *
     * @param request 包含标记节假日所需参数的请求对象
     * @return 返回操作结果，成功返回true，失败返回错误信息
     * @throws Exception 当标记过程中发生异常时抛出
     */
    @PostMapping("/mark")
    public Result<Boolean> markHoliday(@RequestBody MarkHolidayRequest request) {
        try {
            Boolean result = holidayService.markHoliday(
                    request.getDate(),
                    request.getHolidayName(),
                    request.getIsHoliday(),
                    request.getHolidayType()
            );
            return result ? Result.success(true) : Result.error("标记失败");
        } catch (Exception e) {
            log.error("标记节假日失败", e);
            return Result.error("标记失败");
        }
    }

    @GetMapping("/types")
    public Result<List<HolidayType>> getHolidayTypes() {
        try {
            List<HolidayType> types = holidayService.getHolidayTypes();
            return Result.success(types);
        } catch (Exception e) {
            log.error("获取节假日类型失败", e);
            return Result.error("获取节假日类型失败");
        }
    }

    @GetMapping("/")
    public Result<String> getHolidayApiInfo() {
        return Result.success("节假日API服务已启动，可用端点：/calendar, /batch-update, /mark, /types");
    }

    @GetMapping("")
    public String redirectToSlash() {
        return "redirect:/api/holiday/";
    }
}
