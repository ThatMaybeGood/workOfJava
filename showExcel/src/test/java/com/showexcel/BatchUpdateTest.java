package com.showexcel;

import com.showexcel.model.HolidayCalendar;
import com.showexcel.service.impl.HolidayServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@SpringBootTest
class BatchUpdateTest {

    @Autowired
    private HolidayServiceImpl holidayService;

    @Test
    void testBatchUpdateWithExistingDate() throws Exception {
        // 创建一个与数据库中已存在日期相同的节假日对象
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date existingDate = sdf.parse("2024-02-09"); // 这个日期在数据库中已存在

        HolidayCalendar holiday = new HolidayCalendar();
        holiday.setHolidayDate(existingDate);
        holiday.setHolidayName("测试节假日");
        holiday.setIsHoliday(true);
        holiday.setHolidayType("TEST");
        holiday.setYear(2024);
        holiday.setDescription("测试描述");

        List<HolidayCalendar> holidays = Arrays.asList(holiday);

        // 执行批量更新
        boolean result = holidayService.batchUpdateHolidays(holidays);

        System.out.println("批量更新结果: " + result);
        System.out.println("如果返回true但数据库未更新，说明查询未匹配到已存在的记录");
    }
}