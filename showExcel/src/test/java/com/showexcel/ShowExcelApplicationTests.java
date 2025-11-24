package com.showexcel;

import com.showexcel.mapper.HolidayCalendarMapper;
import com.showexcel.model.HolidayCalendar;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class ShowExcelApplicationTests {

    private static final Logger log = LoggerFactory.getLogger(ShowExcelApplicationTests.class);
    @Autowired
    private HolidayCalendarMapper holidayCalendarMapper;

    @Test
    void contextLoads() {
        // Arrange
        String targetDate = "2025-10-30";

        // Act
        HolidayCalendar result = holidayCalendarMapper.getHolidayCalendarByDate(targetDate);

        // Assert
        assertNotNull(result, "查询结果不应为空");
        log.info("info查询结果: {}", result);

    }

}
