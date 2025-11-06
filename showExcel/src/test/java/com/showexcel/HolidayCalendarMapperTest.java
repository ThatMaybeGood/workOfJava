package com.showexcel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

import com.showexcel.mapper.HolidayCalendarMapper;
import com.showexcel.model.HolidayCalendar;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Date;

@ExtendWith(MockitoExtension.class)
class HolidayCalendarMapperTest {
    private static final Logger logger = LoggerFactory.getLogger(HolidayCalendarMapperTest.class);

    @Mock
    private HolidayCalendarMapper holidayCalendarMapper;

    @Test
    void findByDate_ShouldReturnHolidayCalendar_WhenDateExists() {
        logger.info("Starting test: findByDate_ShouldReturnHolidayCalendar_WhenDateExists");

        // Arrange
        String date = "2025-01-01";
        HolidayCalendar expectedHoliday = new HolidayCalendar();
        expectedHoliday.setHolidayDate(Date.valueOf(date));

        logger.debug("Setting up mock return value for date: {}", date);
        when(holidayCalendarMapper.findByDate(date)).thenReturn(expectedHoliday);

        // Act
        logger.debug("Executing findByDate method with date: {}", date);
        HolidayCalendar actualHoliday = holidayCalendarMapper.findByDate(date);
        logger.debug("Received result: {}", actualHoliday);
        // Assert
        logger.info("Asserting result for date: {}", date);
        assertEquals(expectedHoliday, actualHoliday);
        logger.info("Test completed successfully: findByDate_ShouldReturnHolidayCalendar_WhenDateExists");
    }

    @Test
    void findByDate_ShouldReturnNull_WhenDateDoesNotExist() {
        logger.info("Starting test: findByDate_ShouldReturnNull_WhenDateDoesNotExist");

        // Arrange
        String date = "2025-12-31";

        logger.debug("Setting up mock to return null for date: {}", date);
        when(holidayCalendarMapper.findByDate(date)).thenReturn(null);

        // Act
        logger.debug("Executing findByDate method with date: {}", date);
        HolidayCalendar actualHoliday = holidayCalendarMapper.findByDate(date);

        // Assert
        logger.info("Asserting null result for date: {}", date);
        assertNull(actualHoliday);
        logger.info("Test completed successfully: findByDate_ShouldReturnNull_WhenDateDoesNotExist");
    }

    @Test
    void findByDate_ShouldReturnNull_WhenDateIsEmpty() {
        logger.info("Starting test: findByDate_ShouldReturnNull_WhenDateIsEmpty");

        // Arrange
        String date = "";

        logger.debug("Setting up mock to return null for empty date");
        when(holidayCalendarMapper.findByDate(date)).thenReturn(null);

        // Act
        logger.debug("Executing findByDate method with empty date");
        HolidayCalendar actualHoliday = holidayCalendarMapper.findByDate(date);

        // Assert
        logger.info("Asserting null result for empty date");
        assertNull(actualHoliday);
        logger.info("Test completed successfully: findByDate_ShouldReturnNull_WhenDateIsEmpty");
    }

    @Test
    void findByDate_ShouldReturnNull_WhenDateIsNull() {
        logger.info("Starting test: findByDate_ShouldReturnNull_WhenDateIsNull");

        // Arrange
        String date = null;

        logger.debug("Setting up mock to return null for null date");
        when(holidayCalendarMapper.findByDate(date)).thenReturn(null);

        // Act
        logger.debug("Executing findByDate method with null date");
        HolidayCalendar actualHoliday = holidayCalendarMapper.findByDate(date);

        // Assert
        logger.info("Asserting null result for null date");
        assertNull(actualHoliday);
        logger.info("Test completed successfully: findByDate_ShouldReturnNull_WhenDateIsNull");
    }
}