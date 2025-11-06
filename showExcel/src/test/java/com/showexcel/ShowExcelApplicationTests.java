package com.showexcel;

import com.showexcel.mapper.HolidayCalendarMapper;
import com.showexcel.model.HolidayCalendar;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;

@SpringBootTest
@Slf4j
class ShowExcelApplicationTests {

    @Autowired
    private HolidayCalendarMapper holidayCalendarMapper;

    @Test
    void contextLoads() {
        // Arrange
        String targetDate = "2025-10-30";

        // Act
        HolidayCalendar result = holidayCalendarMapper.findByDate(targetDate);

        // Assert
        assertNotNull(result, "查询结果不应为空");
        log.info("查询结果: {}", result);

    }

}
